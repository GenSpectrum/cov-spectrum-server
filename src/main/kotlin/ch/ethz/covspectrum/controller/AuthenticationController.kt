package ch.ethz.covspectrum.controller

import ch.ethz.covspectrum.entity.req.JwtRequest
import ch.ethz.covspectrum.entity.res.JwtResponse
import ch.ethz.covspectrum.service.JwtTokenService
import ch.ethz.covspectrum.service.JwtUserDetailsService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.security.Principal


@RestController
@RequestMapping("/internal")
class AuthenticationController(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenService: JwtTokenService,
    private val userDetailsService: JwtUserDetailsService
) {

    @RequestMapping(value = ["/login"], method = [RequestMethod.POST])
    fun login(@RequestBody authenticationRequest: JwtRequest): JwtResponse? {
        authenticate(authenticationRequest.username, authenticationRequest.password)
        val userDetails: UserDetails = userDetailsService.loadUserByUsername(authenticationRequest.username)
        val token = jwtTokenService.generateToken(userDetails)
        return JwtResponse(token)
    }


    /**
     * Creates a JWT token for the current user that lasts 3 minutes and only allows access to a defined endpoint (not
     * exact path, i.e., that query params are ignored).
     * If the endpoint is called by a not-logged-in user, null will be returned.
     */
    @RequestMapping(value = ["/create-temporary-jwt"], method = [RequestMethod.POST])
    fun createTemporaryJwt(
        @RequestParam restrictionEndpoint: String,
        principal: Principal?
    ): JwtResponse? {
        principal ?: return null
        val userDetails = userDetailsService.loadUserByUsername(principal.name)
        val claims = mapOf("restriction_endpoint" to restrictionEndpoint)
        val token = jwtTokenService.generateToken(userDetails, 3L * 60, claims)
        return JwtResponse(token)
    }


    private fun authenticate(username: String, password: String) {
        // TODO Improve error handling (e.g., wrong credentials)
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, password))
    }

}
