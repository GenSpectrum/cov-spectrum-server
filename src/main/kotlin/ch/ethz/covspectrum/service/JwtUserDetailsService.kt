package ch.ethz.covspectrum.service

import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service


@Service
class JwtUserDetailsService(
    private val databaseService: DatabaseService
): UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails {
        val sql = """
            select username, password_hash
            from spectrum_account
            where username = lower(?);
        """.trimIndent()
        databaseService.getConnection().use { conn ->
            conn.prepareStatement(sql).use { statement ->
                statement.setString(1, username)
                statement.executeQuery().use { rs ->
                    val userFound = rs.next()
                    if (!userFound) {
                        throw UsernameNotFoundException("User not found")
                    }
                    return User(
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        emptyList()
                    )
                }
            }
        }
    }
}
