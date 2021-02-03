package ch.ethz.covspectrum.controller;

import ch.ethz.covspectrum.service.JwtUserDetailsService;
import ch.ethz.covspectrum.util.JwtTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;



@RestController
@CrossOrigin
@RequestMapping("/internal")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;

    private final JwtTokenService jwtTokenService;

    private final JwtUserDetailsService userDetailsService;


    public AuthenticationController(
            AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService,
            JwtUserDetailsService userDetailsService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
    }


    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public JwtResponse login(@RequestBody JwtRequest authenticationRequest) {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        String token = jwtTokenService.generateToken(userDetails);
        return new JwtResponse(token);
    }


    private void authenticate(String username, String password) {
        // TODO Improve error handling (e.g., wrong credentials)
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }
}
