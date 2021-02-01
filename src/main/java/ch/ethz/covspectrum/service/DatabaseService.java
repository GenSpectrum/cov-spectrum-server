package ch.ethz.covspectrum.service;

import ch.ethz.covspectrum.entity.api.CountAndProportionWithCI;
import ch.ethz.covspectrum.entity.api.Distribution;
import ch.ethz.covspectrum.entity.api.WeekAndCountry;
import ch.ethz.covspectrum.entity.core.AAMutation;
import ch.ethz.covspectrum.entity.core.SampleFull;
import ch.ethz.covspectrum.entity.core.SampleName;
import ch.ethz.covspectrum.entity.core.Variant;
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
                    from gisaid_sequence
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
                    from gisaid_sequence
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
                      string_agg(cs.gisaid_epi_isl, ',') as strains
                    from
                      gisaid_sequence cs
                      join gisaid_sequence_nextclade_mutation_aa m on cs.strain = m.strain
                    where
                      extract(isoyear from date) = ?
                      and extract(week from cs.date) = ?
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


    public List<Distribution<YearWeek, CountAndProportionWithCI>> getTimeDistribution(
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
                  gs.strain,
                  gs.date,
                  gs.age,
                  gs.division
                from
                  gisaid_sequence gs
                  join gisaid_sequence_nextclade_mutation_aa m on gs.strain = m.strain
                where m.aa_mutation = any(?) and country = ?
                group by
                  gs.strain
                having count(*) >= ?
              ) x
              join (
                select
                  extract(isoyear from gs.date) as year,
                  extract(week from gs.date) as week,
                  count(*) as count
                from gisaid_sequence gs
                where country = ?
                group by
                  extract(isoyear from gs.date),
                  extract(week from gs.date)
              ) y on extract(year from x.date) = y.year and extract(week from x.date) = y.week
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
            float matchPercentage
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
                  gs.strain,
                  gs.date,
                  (case
                    when gs.age < 10 then '0-9'
                    when gs.age between 10 and 19 then '10-19'
                    when gs.age between 20 and 29 then '20-29'
                    when gs.age between 30 and 39 then '30-39'
                    when gs.age between 40 and 49 then '40-49'
                    when gs.age between 50 and 59 then '50-59'
                    when gs.age between 60 and 69 then '60-69'
                    when gs.age between 70 and 79 then '70-79'
                    when gs.age >= 80 then '80+'
                  end) as age_group,
                  gs.division
                from
                  gisaid_sequence gs
                  join gisaid_sequence_nextclade_mutation_aa m on gs.strain = m.strain
                where m.aa_mutation = any(?) and country = ?
                group by
                  gs.strain
                having count(*) >= ?
              ) x
              join (
                select
                  (case
                    when gs.age < 10 then '0-9'
                    when gs.age between 10 and 19 then '10-19'
                    when gs.age between 20 and 29 then '20-29'
                    when gs.age between 30 and 39 then '30-39'
                    when gs.age between 40 and 49 then '40-49'
                    when gs.age between 50 and 59 then '50-59'
                    when gs.age between 60 and 69 then '60-69'
                    when gs.age between 70 and 79 then '70-79'
                    when gs.age >= 80 then '80+'
                  end) as age_group,
                  count(*) as count
                from gisaid_sequence gs
                where country = ?
                group by
                  (case
                    when gs.age < 10 then '0-9'
                    when gs.age between 10 and 19 then '10-19'
                    when gs.age between 20 and 29 then '20-29'
                    when gs.age between 30 and 39 then '30-39'
                    when gs.age between 40 and 49 then '40-49'
                    when gs.age between 50 and 59 then '50-59'
                    when gs.age between 60 and 69 then '60-69'
                    when gs.age between 70 and 79 then '70-79'
                    when gs.age >= 80 then '80+'
                  end)
              ) y on x.age_group = y.age_group
            group by
              x.age_group,
              y.count;
        """;
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
                  gs.country,
                  gs.strain,
                  gs.date
                from
                  gisaid_sequence gs
                  join gisaid_sequence_nextclade_mutation_aa m on gs.strain = m.strain
                where m.aa_mutation = any(?::text[])
                group by
                  gs.strain
                having count(*) >= ?
              ) x
              join (
                select
                  gs.country,
                  extract(isoyear from gs.date) as year,
                  extract(week from gs.date) as week,
                  count(*) as count
                from gisaid_sequence gs
                group by
                  gs.country,
                  extract(isoyear from gs.date),
                  extract(week from gs.date)
              ) y on x.country = y.country
                       and extract(year from x.date) = y.year
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


    public List<SampleFull> getSamples(Variant variant, float matchPercentage) throws SQLException {
        List<String> mutations = variant.getMutations().stream()
                .map(AAMutation::getMutationCode)
                .collect(Collectors.toList());
        String sql = """
            select
              gs.strain,
              gs.gisaid_epi_isl,
              gs.country,
              gs.date,
              x.mutations
            from
              (
                select
                  gs.strain,
                  string_agg(m.aa_mutation, ',') as mutations
                from
                  (
                    select m.strain
                    from gisaid_sequence_nextclade_mutation_aa m
                    where m.aa_mutation = any(?::text[])
                    group by m.strain
                    having count(*) >= ?
                  ) gs
                  join gisaid_sequence_nextclade_mutation_aa m on gs.strain = m.strain
                group by gs.strain
              ) x
              join gisaid_sequence gs on x.strain = gs.strain;
        """;
        try (Connection conn = getDatabaseConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setArray(1, conn.createArrayOf("text", mutations.toArray()));
            statement.setFloat(2, mutations.size() * matchPercentage);
            try (ResultSet rs = statement.executeQuery()) {
                List<SampleFull> result = new ArrayList<>();
                while (rs.next()) {
                    List<AAMutation> ms = Arrays.stream(rs.getString("mutations").split(","))
                            .map(AAMutation::new).collect(Collectors.toList());
                    SampleFull s = new SampleFull(
                            rs.getString("gisaid_epi_isl"), rs.getString("country"),
                            rs.getObject("date", LocalDate.class), ms
                    );
                    result.add(s);
                }
                return result;
            }
        }
    }

}
