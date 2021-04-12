package ch.ethz.covspectrum.service;

import ch.ethz.covspectrum.entity.core.AAMutation;
import ch.ethz.covspectrum.entity.core.SampleSelection;
import ch.ethz.covspectrum.entity.core.SampleSelectionCacheKey;
import ch.ethz.covspectrum.entity.core.WeightedSampleResultSet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class PreComputationService {

    private final int MAX_CACHE_ENTRIES = 100;

    private final Logger logger = LoggerFactory.getLogger(PreComputationService.class);

    private final DatabaseService databaseService;

    private final ObjectMapper objectMapper;


    public PreComputationService(DatabaseService databaseService, ObjectMapper objectMapper) {
        this.databaseService = databaseService;
        this.objectMapper = objectMapper;
    }


    /**
     * This function will pre-compute up to MAX_CACHE_ENTRIES results. It chooses those that were used most frequently
     * in the current and previous week. The function will not remove/replace any entry in the cache. It has to be
     * ensured that the cache gets cleared when new data arrives.
     */
    @Scheduled(fixedDelay = 1000 * 60 * 10)
    public void preComputeGetSamples() throws SQLException {
        logger.info("Start pre-computing values for getSamples().");
        try (Connection conn = databaseService.getDatabaseConnection()) {
            List<SampleSelectionCacheKey> cacheKeys = new ArrayList<>();

            // Fetch frequently used selections
            String fetchSql = """
                select
                  fields, private_version, region, country,
                  mutations, match_percentage, data_type, date_from, date_to,
                  sum(usage_count) as usage_count
                from
                  spectrum_api_usage_sample u
                where
                  ((u.isoyear = extract(isoyear from current_date) and u.isoweek = extract(week from current_date))
                  or (u.isoyear = extract(isoyear from (current_date - interval '1 week')::date)
                        and u.isoweek = extract(week from (current_date - interval '1 week')::date)))
                  and not exists(
                    select
                    from spectrum_api_cache_sample c
                    where
                      u.fields = c.fields
                      and u.private_version = c.private_version
                      and u.region = c.region
                      and u.country = c.country
                      and u.mutations = c.mutations
                      and u.match_percentage = c.match_percentage
                      and u.data_type = c.data_type
                      and u.date_from = c.date_from
                      and u.date_to = c.date_to
                  )
                group by
                  fields, private_version, region, country,
                  mutations, match_percentage, data_type, date_from, date_to
                order by usage_count desc
                limit greatest(? - (select count(*) from spectrum_api_cache_sample), 0);
            """;
            try (PreparedStatement statement = conn.prepareStatement(fetchSql)) {
                statement.setInt(1, MAX_CACHE_ENTRIES);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        SampleSelectionCacheKey cacheKey = new SampleSelectionCacheKey(
                                rs.getString("fields"),
                                rs.getBoolean("private_version"),
                                rs.getString("region"),
                                rs.getString("country"),
                                rs.getString("mutations"),
                                rs.getFloat("match_percentage"),
                                rs.getString("data_type"),
                                rs.getDate("date_from").toLocalDate(),
                                rs.getDate("date_to").toLocalDate()
                        );
                        cacheKeys.add(cacheKey);
                    }
                }
            }
            logger.info("Found " + cacheKeys.size() + " entries to pre-compute.");

            // Compute results and write them into the cache/database
            int success = 0;
            int failed = 0;
            String insertSql = """
                insert into spectrum_api_cache_sample (
                  fields,
                  private_version,
                  region,
                  country,
                  mutations,
                  match_percentage,
                  data_type,
                  date_from,
                  date_to,
                  cache
                )
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;
            try (PreparedStatement statement = conn.prepareStatement(insertSql)) {
                for (SampleSelectionCacheKey cacheKey : cacheKeys) {
                    Pair<SampleSelection, String> pair = cacheKey.toSampleSelection();
                    SampleSelection selection = pair.getValue0();
                    String fields = pair.getValue1();
                    // TODO I really need a consistent set of arguments....
                    String mutations = null;
                    if (selection.getVariant() != null) {
                        mutations = selection.getVariant().getMutations().stream().map(AAMutation::getMutationCode)
                                .collect(Collectors.joining(","));
                    }
                    WeightedSampleResultSet samples = databaseService.getSamples2(
                            fields,
                            selection.getRegion(),
                            selection.getCountry(),
                            mutations,
                            selection.getMatchPercentage(),
                            selection.getDataType(),
                            selection.getDateFrom(),
                            selection.getDateTo(),
                            selection.isUsePrivate()
                    );
                    try {
                        String json = objectMapper.writeValueAsString(samples);
                        statement.clearParameters();
                        statement.setString(1, cacheKey.getFields());
                        statement.setBoolean(2, cacheKey.isPrivateVersion());
                        statement.setString(3, cacheKey.getRegion());
                        statement.setString(4, cacheKey.getCountry());
                        statement.setString(5, cacheKey.getMutations());
                        statement.setFloat(6, cacheKey.getMatchPercentage());
                        statement.setString(7, cacheKey.getDataType());
                        statement.setDate(8, Date.valueOf(cacheKey.getDateFrom()));
                        statement.setDate(9, Date.valueOf(cacheKey.getDateTo()));
                        statement.setString(10, json);
                        statement.execute();
                        success++;
                    } catch (JsonProcessingException e) {
                        failed++;
                        e.printStackTrace();
                    }
                }
            }
            logger.info("Successfully pre-computed and cached " + success + " entries. " + failed + " failed.");
        }
    }

}
