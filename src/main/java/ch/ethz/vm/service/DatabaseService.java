package ch.ethz.vm.service;

import ch.ethz.vm.entity.AAMutation;
import ch.ethz.vm.entity.Sample;
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

}
