package ch.ethz.covspectrum.config

import ch.ethz.covspectrum.service.JwtTokenService
import ch.ethz.covspectrum.service.JwtUserDetailsService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class JwtRequestFilter(
    private val jwtUserDetailsService: JwtUserDetailsService,
    private val jwtTokenService: JwtTokenService
): OncePerRequestFilter() {

    /**
     * This function looks for the "Authorization" header and validates the JWT token if one is present.
     *
     * TODO Better handling of error cases such as expired tokens. See: JwtTokenService#validateJwtToken
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // First check if a JWT token is provided as query param
        var jwtToken = request.getParameter("jwt")

        // If not, check the Authorization header
        if (jwtToken == null || jwtToken.isBlank()) {
            val requestTokenHeader = request.getHeader("Authorization")
            if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
                jwtToken = requestTokenHeader.substring(7) // Removes "Bearer " prefix
            }
        }
        if (jwtToken != null) {
            try {
                val restrictionEndpoint = jwtTokenService.getRestrictionEndpoint(jwtToken)
                if (restrictionEndpoint != null && restrictionEndpoint != request.requestURI) {
                    response.status = 401
                    return
                }
                val username = jwtTokenService.getUsername(jwtToken)
                val userDetails = jwtUserDetailsService.loadUserByUsername(username)
                if (jwtTokenService.validateToken(jwtToken, userDetails)) {
                    val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.authorities
                    )
                    usernamePasswordAuthenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
                }
            } catch (e: Exception) {
                response.status = 401
                return
            }
        }
        filterChain.doFilter(request, response)
    }

}
