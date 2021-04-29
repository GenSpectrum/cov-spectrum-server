package ch.ethz.covspectrum.fiv;


import ch.ethz.covspectrum.controller.computed.Chen2021FitnessController;
import ch.ethz.covspectrum.entity.model.chen2021fitness.ApiResponse;
import ch.ethz.covspectrum.service.DatabaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PreComputePangolinLineages {

    private final DatabaseService databaseService;
    private final Chen2021FitnessController chen2021FitnessController;

    public PreComputePangolinLineages() {
        this.databaseService = new DatabaseService(new ObjectMapper());
        this.chen2021FitnessController = new Chen2021FitnessController(this.databaseService);
    }


    public void process(@Nullable String region, @Nullable String country) throws SQLException, IOException {
        // Check porameters
        if (country != null && region == null) {
            throw new RuntimeException("Please state the region of the country.");
        }

        // Only look into the past three months
        LocalDate startDate = LocalDate.now().minusMonths(3);
        LocalDate endDate = LocalDate.now();

        // Fetch all pangolin lineages in the world
        String fetchLineagesSql = """
                    select distinct m.pangolin_lineage
                    from spectrum_sequence_public_meta m
                    where
                      m.date >= ?
                      and m.pangolin_lineage is not null;
                """;
        List<String> lineages = new ArrayList<>();
        try (Connection conn = databaseService.getDatabaseConnection()) {
            try (PreparedStatement statement = conn.prepareStatement(fetchLineagesSql)) {
                statement.setDate(1, Date.valueOf(startDate));
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        lineages.add(rs.getString("pangolin_lineage"));
                        // Include the sub-lineages as well
                        lineages.add(rs.getString("pangolin_lineage") + "*");
                    }
                }
            }
        }

        // Compute fitness advantage values + insert results into the database
        try (Connection conn = databaseService.getDatabaseConnection()) {
            String insertSql = """
                insert into spectrum_pangolin_lineage_recent_metrics (
                  insertion_timestamp,
                  pangolin_lineage,
                  region,
                  country,
                  fitness_advantage,
                  fitness_advantage_lower,
                  fitness_advantage_upper
                )
                values (now(), ?, ?, ?, ?, ?, ?);
            """;
            try (PreparedStatement statement = conn.prepareStatement(insertSql)) {
                for (String lineage : lineages) {
                    Optional<ApiResponse> fitnessResultsOpt;
                    try {
                        fitnessResultsOpt = chen2021FitnessController.compute(
                                region,
                                country,
                                null,
                                1,
                                lineage,
                                null,
                                0.95f,
                                4.8f,
                                1,
                                startDate,
                                endDate,
                                1,
                                1
                        );
                    } catch (IOException e) {
                        continue;
                    }
                    if (fitnessResultsOpt.isPresent()) {
                        ApiResponse fitnessResults = fitnessResultsOpt.get();
                        ApiResponse.Params fitnessParams = fitnessResults.getParams();
                        statement.setString(1, lineage);
                        statement.setString(2, region);
                        statement.setString(3, country);
                        statement.setFloat(4, fitnessParams.getFd().getValue());
                        statement.setFloat(5, fitnessParams.getFd().getCiLower());
                        statement.setFloat(6, fitnessParams.getFd().getCiUpper());
                        statement.execute();
                        statement.clearParameters();
                    }
                }
            }
        }
    }

}
