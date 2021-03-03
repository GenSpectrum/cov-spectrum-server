package ch.ethz.covspectrum.service;

import ch.ethz.covspectrum.entity.api.*;
import ch.ethz.covspectrum.entity.core.*;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.javatuples.Pair;
import org.springframework.stereotype.Service;
import org.threeten.extra.YearWeek;

import java.beans.PropertyVetoException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class DatabaseService {

    private static final ComboPooledDataSource pool = new ComboPooledDataSource();

    static {
        try {
            pool.setDriverClass("org.postgresql.Driver");
            pool.setJdbcUrl("jdbc:postgresql://" + System.getenv("COV_SPECTRUM_HOST") + ":" +
                    System.getenv("COV_SPECTRUM_PORT") + "/" + System.getenv("COV_SPECTRUM_NAME"));
            pool.setUser(System.getenv("COV_SPECTRUM_USERNAME"));
            pool.setPassword(System.getenv("COV_SPECTRUM_PASSWORD"));
        } catch (PropertyVetoException e) {
            throw new RuntimeException(e);
        }
    }


    public Connection getDatabaseConnection() throws SQLException {
        return pool.getConnection();
    }


    public List<String> getCountryNames() throws SQLException {
        String sql = """
                    select distinct country
                    from spectrum_sequence_public_meta
                    order by country;
                """;
        try (Connection conn = getDatabaseConnection();
             Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery(sql)) {
                List<String> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(rs.getString("country"));
                }
                return result;
            }
        }
    }


    public List<Variant> getKnownVariants() throws SQLException {
        String sql = """
                    select variant_name, string_agg(aa_mutation, ',') as mutations
                    from variant_mutation_aa
                    group by variant_name;
                """;
        try (Connection conn = getDatabaseConnection();
             Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery(sql)) {
                List<Variant> result = new ArrayList<>();
                while (rs.next()) {
                    Set<AAMutation> mutations = Arrays.stream(rs.getString("mutations").split(","))
                            .map(AAMutation::new)
                            .collect(Collectors.toSet());
                    result.add(new Variant(rs.getString("variant_name"), mutations));
                }
                return result;
            }
        }
    }


    public int getNumberSequences(YearWeek week, String country) throws SQLException {
        String sql = """
                    select count(*) as count
                    from spectrum_sequence_public_meta
                    where
                      extract(isoyear from date) = ?
                      and extract(week from date) = ?
                      and country = ?;
                """;
        try (Connection conn = getDatabaseConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, week.getYear());
            statement.setInt(2, week.getWeek());
            statement.setString(3, country);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt("count");
            }
        }
    }


    public List<Pair<AAMutation, Set<SampleName>>> getMutations(YearWeek week, String country) throws SQLException {
        int MINIMAL_NUMBER_OF_SAMPLES = 5;
        String sql = """
                    select
                      m.aa_mutation as mutation,
                      string_agg(s.sequence_name, ',') as strains
                    from
                      spectrum_sequence_public_meta s
                      join spectrum_sequence_public_mutation_aa m on s.sequence_name = m.sequence_name
                    where
                      extract(isoyear from date) = ?
                      and extract(week from s.date) = ?
                      and country = ?
                    group by m.aa_mutation
                    having count(*) >= ?;
                """;
        try (Connection conn = getDatabaseConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, week.getYear());
            statement.setInt(2, week.getWeek());
            statement.setString(3, country);
            statement.setInt(4, MINIMAL_NUMBER_OF_SAMPLES);
            try (ResultSet rs = statement.executeQuery()) {
                List<Pair<AAMutation, Set<SampleName>>> results = new ArrayList<>();
                while (rs.next()) {
                    AAMutation mutation = new AAMutation(rs.getString("mutation"));
                    Set<SampleName> strains = Arrays.stream(rs.getString("strains").split(","))
                            .map(SampleName::new).collect(Collectors.toSet());
                    results.add(new Pair<>(mutation, strains));
                }
                return results;
            }
        }
    }


    public List<Distribution<LocalDate, CountAndProportionWithCI>> getDailyTimeDistribution(
            Variant variant,
            String country,
            float matchPercentage
    ) throws SQLException {
        List<String> mutations = variant.getMutations().stream()
                .map(AAMutation::getMutationCode)
                .collect(Collectors.toList());
        String sql = """
            select
              y.date as date,
              sum(case when sequence_name is not null then 1 else 0 end) as count,
              y.count as total
            from
              (
                select
                  s.sequence_name,
                  s.date,
                  s.age,
                  s.division
                from
                  spectrum_sequence_public_meta s
                  join spectrum_sequence_public_mutation_aa m on s.sequence_name = m.sequence_name
                where m.aa_mutation = any(?) and country = ?
                group by
                  s.sequence_name, s.date, s.age, s.division
                having count(*) >= ?
              ) x
              right join (
                select
                  gs.date as date,
                  count(*) as count
                from spectrum_sequence_public_meta gs
                where country = ? and gs.date is not null
                group by gs.date
              ) y on x.date = y.date
            group by
              y.date,
              y.count;
        """;
        try (Connection conn = getDatabaseConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setArray(1, conn.createArrayOf("text", mutations.toArray()));
            statement.setString(2, country);
            statement.setFloat(3, mutations.size() * matchPercentage);
            statement.setString(4, country);
            try (ResultSet rs = statement.executeQuery()) {
                List<Distribution<LocalDate, CountAndProportionWithCI>> result = new ArrayList<>();
                while (rs.next()) {
                    Distribution<LocalDate, CountAndProportionWithCI> d = new Distribution<>(
                            rs.getDate("date").toLocalDate(),
                            CountAndProportionWithCI.fromWilsonCI(
                                    rs.getInt("count"), rs.getInt("total"))
                    );
                    result.add(d);
                }
                return result;
            }
        }
    }


    public List<Distribution<YearWeek, CountAndProportionWithCI>> getWeeklyTimeDistribution(
            Variant variant,
            String country,
            float matchPercentage
    ) throws SQLException {
        List<String> mutations = variant.getMutations().stream()
                .map(AAMutation::getMutationCode)
                .collect(Collectors.toList());
        String sql = """
            select
              extract(isoyear from x.date) as year,
              extract(week from x.date) as week,
              count(*) as count,
              y.count as total
            from
              (
                select
                  s.sequence_name,
                  s.date,
                  s.age,
                  s.division
                from
                  spectrum_sequence_public_meta s
                  join spectrum_sequence_public_mutation_aa m on s.sequence_name = m.sequence_name
                where m.aa_mutation = any(?) and country = ?
                group by
                  s.sequence_name, s.date, s.age, s.division
                having count(*) >= ?
              ) x
              join (
                select
                  extract(isoyear from gs.date) as year,
                  extract(week from gs.date) as week,
                  count(*) as count
                from spectrum_sequence_public_meta gs
                where country = ?
                group by
                  extract(isoyear from gs.date),
                  extract(week from gs.date)
              ) y on extract(isoyear from x.date) = y.year and extract(week from x.date) = y.week
            group by
              extract(isoyear from x.date),
              extract(week from x.date),
              y.count;
        """;
        try (Connection conn = getDatabaseConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setArray(1, conn.createArrayOf("text", mutations.toArray()));
            statement.setString(2, country);
            statement.setFloat(3, mutations.size() * matchPercentage);
            statement.setString(4, country);
            try (ResultSet rs = statement.executeQuery()) {
                List<Distribution<YearWeek, CountAndProportionWithCI>> result = new ArrayList<>();
                while (rs.next()) {
                    Distribution<YearWeek, CountAndProportionWithCI> d = new Distribution<>(
                            YearWeek.of(rs.getInt("year"), rs.getInt("week")),
                            CountAndProportionWithCI.fromWilsonCI(
                                    rs.getInt("count"), rs.getInt("total"))
                    );
                    result.add(d);
                }
                return result;
            }
        }
    }


    public List<Distribution<String, CountAndProportionWithCI>> getAgeDistribution(
            Variant variant,
            String country,
            float matchPercentage,
            boolean usePrivateVersion
    ) throws SQLException {
        List<String> mutations = variant.getMutations().stream()
                .map(AAMutation::getMutationCode)
                .collect(Collectors.toList());
        String sql = """
            select
              x.age_group,
              count(*) as count,
              y.count as total
            from
              (
                select
                  s.sequence_name,
                  s.date,
                  (case
                    when s.age < 10 then '0-9'
                    when s.age between 10 and 19 then '10-19'
                    when s.age between 20 and 29 then '20-29'
                    when s.age between 30 and 39 then '30-39'
                    when s.age between 40 and 49 then '40-49'
                    when s.age between 50 and 59 then '50-59'
                    when s.age between 60 and 69 then '60-69'
                    when s.age between 70 and 79 then '70-79'
                    when s.age >= 80 then '80+'
                  end) as age_group,
                  s.division
                from
                  spectrum_sequence_public_meta s
                  join spectrum_sequence_public_mutation_aa m on s.sequence_name = m.sequence_name
                where m.aa_mutation = any(?) and country = ?
                group by
                  s.sequence_name, s.date, s.age, s.division
                having count(*) >= ?
              ) x
              join (
                select
                  (case
                    when s.age < 10 then '0-9'
                    when s.age between 10 and 19 then '10-19'
                    when s.age between 20 and 29 then '20-29'
                    when s.age between 30 and 39 then '30-39'
                    when s.age between 40 and 49 then '40-49'
                    when s.age between 50 and 59 then '50-59'
                    when s.age between 60 and 69 then '60-69'
                    when s.age between 70 and 79 then '70-79'
                    when s.age >= 80 then '80+'
                  end) as age_group,
                  count(*) as count
                from spectrum_sequence_public_meta s
                where country = ?
                group by
                  (case
                    when s.age < 10 then '0-9'
                    when s.age between 10 and 19 then '10-19'
                    when s.age between 20 and 29 then '20-29'
                    when s.age between 30 and 39 then '30-39'
                    when s.age between 40 and 49 then '40-49'
                    when s.age between 50 and 59 then '50-59'
                    when s.age between 60 and 69 then '60-69'
                    when s.age between 70 and 79 then '70-79'
                    when s.age >= 80 then '80+'
                  end)
              ) y on x.age_group = y.age_group
            group by
              x.age_group,
              y.count;
        """;
        if (usePrivateVersion) {
            // TODO
            sql = sql.replace("public", "private");
        }
        try (Connection conn = getDatabaseConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setArray(1, conn.createArrayOf("text", mutations.toArray()));
            statement.setString(2, country);
            statement.setFloat(3, mutations.size() * matchPercentage);
            statement.setString(4, country);
            try (ResultSet rs = statement.executeQuery()) {
                List<Distribution<String, CountAndProportionWithCI>> result = new ArrayList<>();
                while (rs.next()) {
                    Distribution<String, CountAndProportionWithCI> d = new Distribution<>(
                            rs.getString("age_group"),
                            CountAndProportionWithCI.fromWilsonCI(
                                    rs.getInt("count"), rs.getInt("total"))
                    );
                    result.add(d);
                }
                return result;
            }
        }
    }


    public List<Distribution<WeekAndCountry, CountAndProportionWithCI>> getInternationalTimeDistribution(
            Variant variant,
            float matchPercentage
    ) throws SQLException {
        List<String> mutations = variant.getMutations().stream()
                .map(AAMutation::getMutationCode)
                .collect(Collectors.toList());
        String sql = """
            select
              x.country,
              extract(isoyear from x.date) as year,
              extract(week from x.date) as week,
              count(*) as count,
              y.count as total
            from
              (
                select
                  s.country,
                  s.sequence_name,
                  s.date
                from
                  spectrum_sequence_public_meta s
                  join spectrum_sequence_public_mutation_aa m on s.sequence_name = m.sequence_name
                where m.aa_mutation = any(?::text[])
                group by
                  s.sequence_name, s.country, s.date
                having count(*) >= ?
              ) x
              join (
                select
                  s.country,
                  extract(isoyear from s.date) as year,
                  extract(week from s.date) as week,
                  count(*) as count
                from spectrum_sequence_public_meta s
                group by
                  s.country,
                  extract(isoyear from s.date),
                  extract(week from s.date)
              ) y on x.country = y.country
                       and extract(isoyear from x.date) = y.year
                       and extract(week from x.date) = y.week
            group by
              x.country,
              extract(isoyear from x.date),
              extract(week from x.date),
              y.count;
        """;
        try (Connection conn = getDatabaseConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setArray(1, conn.createArrayOf("text", mutations.toArray()));
            statement.setFloat(2, mutations.size() * matchPercentage);
            try (ResultSet rs = statement.executeQuery()) {
                List<Distribution<WeekAndCountry, CountAndProportionWithCI>> result = new ArrayList<>();
                while (rs.next()) {
                    Distribution<WeekAndCountry, CountAndProportionWithCI> d = new Distribution<>(
                            new WeekAndCountry(
                                    YearWeek.of(rs.getInt("year"), rs.getInt("week")),
                                    rs.getString("country")
                            ),
                            CountAndProportionWithCI.fromWilsonCI(rs.getInt("count"), rs.getInt("total"))
                    );
                    result.add(d);
                }
                return result;
            }
        }
    }


    public List<SampleFull> getSamples(
            Variant variant,
            float matchPercentage,
            boolean usePrivateVersion
    ) throws SQLException {
        List<String> mutations = variant.getMutations().stream()
                .map(AAMutation::getMutationCode)
                .collect(Collectors.toList());
        String sql = """
            select
              s.sequence_name,
              s.country,
              s.date,
              s.division,
              s.location,
              s.zip_code,
              s.host,
              s.age,
              s.sex,
              s.submitting_lab,
              s.originating_lab,
              x.mutations
            from
              (
                select
                  s.sequence_name,
                  string_agg(m.aa_mutation, ',') as mutations
                from
                  (
                    select m.sequence_name
                    from spectrum_sequence_public_mutation_aa m
                    where m.aa_mutation = any(?::text[])
                    group by m.sequence_name
                    having count(*) >= ?
                  ) s
                  join spectrum_sequence_public_mutation_aa m on s.sequence_name = m.sequence_name
                group by s.sequence_name
              ) x
              join spectrum_sequence_public_meta s on x.sequence_name = s.sequence_name;
        """;
        if (usePrivateVersion) {
            // TODO
            sql = sql.replace("public", "private");
        }
        try (Connection conn = getDatabaseConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setArray(1, conn.createArrayOf("text", mutations.toArray()));
            statement.setFloat(2, mutations.size() * matchPercentage);
            try (ResultSet rs = statement.executeQuery()) {
                List<SampleFull> result = new ArrayList<>();
                while (rs.next()) {
                    List<AAMutation> ms = Arrays.stream(rs.getString("mutations").split(","))
                            .map(AAMutation::new).collect(Collectors.toList());
                    SamplePrivateMetadata privateMetadata = null;
                    if (usePrivateVersion) {
                        privateMetadata = new SamplePrivateMetadata(
                                rs.getString("country"),
                                rs.getString("division"),
                                rs.getString("location"),
                                rs.getString("zip_code"),
                                rs.getString("host"),
                                rs.getInt("age"),
                                rs.getString("sex"),
                                rs.getString("submitting_lab"),
                                rs.getString("originating_lab")
                        );
                    }
                    SampleFull s = new SampleFull(
                            rs.getString("sequence_name"), rs.getString("country"),
                            rs.getObject("date", LocalDate.class), ms, privateMetadata
                    );
                    result.add(s);
                }
                return result;
            }
        }
    }


    public List<SampleSequence> getSampleSequences(
            List<SampleName> sampleNames,
            boolean usePrivateVersion
    ) throws SQLException {
        // If the sample name begins with "UNRELEASED_ETHZ_", the sequence has to be looked up in consensus_sequence,
        // otherwise, it is in gisaid_sequence.
        List<Integer> ethids = new ArrayList<>();
        List<String> gisaid_epi_isls = new ArrayList<>();
        for (SampleName sampleName : sampleNames) {
            String s = sampleName.getName();
            if (s.startsWith("UNRELEASED_ETHZ_")) {
                ethids.add(Integer.parseInt(s.substring(16)));
            } else {
                gisaid_epi_isls.add(s);
            }
        }

        String sql;
        if (usePrivateVersion) {
            sql = """
            select
              'UNRELEASED_ETHZ_' || cs.ethid as sample_name,
              cs.seq as sequence
            from consensus_sequence cs
            where ethid = any(?::int[])
            union all
            select
              gs.gisaid_epi_isl as sample_name,
              gs.original_seq as sequence
            from gisaid_sequence gs
            where gisaid_epi_isl = any(?::text[]);
        """;
        } else {
            sql = """
            select
              'UNRELEASED_ETHZ_' || cs.ethid as sample_name,
              cs.seq as sequence
            from consensus_sequence cs
            where ethid = any(?::int[])
            union all
            select
              gs.gisaid_epi_isl as sample_name,
              gs.original_seq as sequence
            from gisaid_sequence gs
            where
                gisaid_epi_isl = any(?::text[])
                and submitting_lab = 'Department of Biosystems Science and Engineering, ETH Zürich';
        """;
        }
        try (Connection conn = getDatabaseConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setArray(1, conn.createArrayOf("int", ethids.toArray()));
            statement.setArray(2, conn.createArrayOf("text", gisaid_epi_isls.toArray()));
            try (ResultSet rs = statement.executeQuery()) {
                List<SampleSequence> result = new ArrayList<>();
                while (rs.next()) {
                    SampleSequence s = new SampleSequence(
                            rs.getString("sample_name"),
                            rs.getString("sequence")
                    );
                    result.add(s);
                }
                return result;
            }
        }
    }


    public List<Distribution<WeekAndZipCode, Count>> getPrivateTimeZipCodeDistributionOfCH(
            Variant variant,
            float matchPercentage
    ) throws SQLException {
        List<String> mutations = variant.getMutations().stream()
                .map(AAMutation::getMutationCode)
                .collect(Collectors.toList());
        String sql = """
            select
              extract(isoyear from x.date) as year,
              extract(week from x.date) as week,
              x.zip_code,
              count(*) as count
            from
              (
                select
                  s.sequence_name,
                  s.zip_code,
                  s.date
                from
                  spectrum_sequence_private_meta s
                  join spectrum_sequence_private_mutation_aa m on s.sequence_name = m.sequence_name
                where
                    m.aa_mutation = any(?::text[])
                    and s.country = 'Switzerland'
                    and s.zip_code is not null
                group by
                  s.sequence_name, s.zip_code, s.date
                having count(*) >= ?
              ) x
            group by
              x.zip_code,
              extract(isoyear from x.date),
              extract(week from x.date);
        """;
        try (Connection conn = getDatabaseConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setArray(1, conn.createArrayOf("text", mutations.toArray()));
            statement.setFloat(2, mutations.size() * matchPercentage);
            try (ResultSet rs = statement.executeQuery()) {
                List<Distribution<WeekAndZipCode, Count>> result = new ArrayList<>();
                while (rs.next()) {
                    Distribution<WeekAndZipCode, Count> d = new Distribution<>(
                            new WeekAndZipCode(
                                    YearWeek.of(rs.getInt("year"), rs.getInt("week")),
                                    rs.getString("zip_code")
                            ),
                            new Count(rs.getInt("count"))
                    );
                    result.add(d);
                }
                return result;
            }
        }
    }
}
