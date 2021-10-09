package ch.ethz.covspectrum.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebSecurityConfig(
    private val jwtAuthenticationEntryPoint: MyAuthenticationEntryPoint,
    private val jwtUserDetailsService: UserDetailsService,
    private val jwtRequestFilter: JwtRequestFilter
) : WebSecurityConfigurerAdapter() {

    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder())
    }


    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }


    @Bean
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }


    override fun configure(httpSecurity: HttpSecurity) {
        httpSecurity.csrf().disable()
            .authorizeRequests()
            .antMatchers("/authenticate").permitAll()
            .anyRequest().permitAll()
            .and().exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        httpSecurity
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
    }
}
