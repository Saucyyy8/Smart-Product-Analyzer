package com.project.Smart_Product_Analyzer.Config;


import com.project.Smart_Product_Analyzer.oauth2.OAuth2AuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures Spring Security for the application, including JWT filter and endpoint security.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    // Injected UserDetailsService for loading user data
    private final UserDetailsService userDetailsService;
    // Injected JWT authentication filter
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    public WebSecurityConfig(UserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter, OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
    }

    /**
     * Configures the security filter chain, disables CSRF, sets endpoint permissions, and adds JWT filter.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(
                        request -> request
                                .requestMatchers("/register","/login", 
                                        "/oauth2/success",
                                        "/oauth2/**",
                                        "/login/oauth2/**").permitAll()
                                .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .oauth2Login(oauth -> oauth.successHandler(oAuth2AuthenticationSuccessHandler)) // Enables OAuth2 login and issues JWT on success
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }


    //@Bean
    public UserDetailsService userDetailsService() {
        UserDetails shabbir
                = User.withUsername("shabbir")
                .password("{noop}password")
                .roles("USER")
                .build();

        UserDetails nikhil
                = User.withUsername("nikhil")
                .password("{noop}nikhil")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(shabbir,nikhil);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(14);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider
                = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        //provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
        provider.setPasswordEncoder(bCryptPasswordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
