package ch.ethz.covspectrum.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


@Service
public class JwtUserDetailsService implements UserDetailsService {

    private final DatabaseService databaseService;


    public JwtUserDetailsService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String sql = """
            select username, password_hash
            from spectrum_account
            where username = lower(?);
        """;
        try (Connection conn = this.databaseService.getDatabaseConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                boolean userFound = rs.next();
                if (!userFound) {
                    throw new UsernameNotFoundException("User not found.");
                }
                return new User(
                        rs.getString("username"), rs.getString("password_hash"),
                        new ArrayList<>());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
