package ch.ethz.covspectrum.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*
import java.util.function.Function


@Service
class JwtTokenService {
    private val secret = System.getenv("COV_SPECTRUM_JWT_SECRET");
    private val tokenLifetimeSeconds = System.getenv("COV_SPECTRUM_JWT_TOKEN_LIFETIME_SECONDS").toLong();


    fun getUsername(token: String): String? {
        return getClaimFromToken(token) { obj: Claims -> obj.subject }
    }


    fun generateToken(userDetails: UserDetails): String {
        return generateToken(userDetails, tokenLifetimeSeconds)
    }


    fun generateToken(userDetails: UserDetails, tokenLifetimeSeconds: Long): String {
        return generateToken(userDetails, tokenLifetimeSeconds, HashMap<String, String>())
    }


    fun generateToken(userDetails: UserDetails, tokenLifetimeSeconds: Long, claims: Map<String, String>): String {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + tokenLifetimeSeconds * 1000))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact()
    }


    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        val username = getUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }


    fun getRestrictionEndpoint(token: String): String? {
        val restrictionEndpoint = getAllClaimsFromToken(token)["restriction_endpoint"] ?: return null
        return restrictionEndpoint as String
    }


    private fun getExpirationDate(token: String): Date {
        return getClaimFromToken(token) { obj: Claims -> obj.expiration }
    }


    private fun <T> getClaimFromToken(token: String, claimsResolver: Function<Claims, T>): T {
        val claims = getAllClaimsFromToken(token)
        return claimsResolver.apply(claims)
    }


    private fun getAllClaimsFromToken(token: String): Claims {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).body
    }


    private fun isTokenExpired(token: String): Boolean {
        val expiration: Date = getExpirationDate(token)
        return expiration.before(Date())
    }

}
