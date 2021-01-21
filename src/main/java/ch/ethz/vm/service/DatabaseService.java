package ch.ethz.vm.service;

import ch.ethz.vm.entity.*;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.javatuples.Pair;
import org.springframework.stereotype.Service;
import org.threeten.extra.YearWeek;

import java.beans.PropertyVetoException;
import java.sql.*;
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
            pool.setJdbcUrl("jdbc:postgresql://" + System.getenv("VM_DB_HOST") + ":" +
                    System.getenv("VM_DB_PORT") + "/" + System.getenv("VM_DB_NAME"));
            pool.setUser(System.getenv("VM_DB_USERNAME"));
            pool.setPassword(System.getenv("VM_DB_PASSWORD"));
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


    public List<Pair<AAMutation, Set<Sample>>> getMutations(YearWeek week, String country) throws SQLException {
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
                List<Pair<AAMutation, Set<Sample>>> results = new ArrayList<>();
                while (rs.next()) {
                    AAMutation mutation = new AAMutation(rs.getString("mutation"));
                    Set<Sample> strains = Arrays.stream(rs.getString("strains").split(","))
                            .map(Sample::new).collect(Collectors.toSet());
                    results.add(new Pair<>(mutation, strains));
                }
                return results;
            }
        }
    }


    public List<DistributionByWeek> getTimeDistribution(Variant variant, String country, float matchPercentage)
            throws SQLException {
        List<String> mutations = variant.getMutations().stream()
                .map(AAMutation::getMutationCode)
                .collect(Collectors.toList());
        String sql = """
            select
              extract(isoyear from x.date) as year,
              extract(week from x.date) as week,
              count(*) as count,
              count(*) * 1.0 / y.count as proportion
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
                List<DistributionByWeek> result = new ArrayList<>();
                while (rs.next()) {
                    DistributionByWeek d = new DistributionByWeek(
                            YearWeek.of(rs.getInt("year"), rs.getInt("week")),
                            rs.getInt("count"), rs.getDouble("proportion"));
                    result.add(d);
                }
                return result;
            }
        }
    }


    public List<DistributionByAgeGroup> getAgeDistribution(Variant variant, String country, float matchPercentage)
            throws SQLException {
        List<String> mutations = variant.getMutations().stream()
                .map(AAMutation::getMutationCode)
                .collect(Collectors.toList());
        String sql = """
            select
              x.age_group,
              count(*) as count,
              count(*) * 1.0 / y.count as proportion
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
                List<DistributionByAgeGroup> result = new ArrayList<>();
                while (rs.next()) {
                    DistributionByAgeGroup d = new DistributionByAgeGroup(
                            rs.getString("age_group"),
                            rs.getInt("count"), rs.getDouble("proportion"));
                    result.add(d);
                }
                return result;
            }
        }
    }


    public List<DistributionByWeekAndCountry> getInternationalTimeDistribution(Variant variant, float matchPercentage) throws SQLException {
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
                List<DistributionByWeekAndCountry> result = new ArrayList<>();
                while (rs.next()) {
                    DistributionByWeekAndCountry d = new DistributionByWeekAndCountry(
                            YearWeek.of(rs.getInt("year"), rs.getInt("week")),
                            rs.getString("country"),
                            rs.getInt("count"), rs.getInt("total"));
                    result.add(d);
                }
                return result;
            }
        }
    }
}
