package com.project.Smart_Product_Analyzer.Config;


import com.project.Smart_Product_Analyzer.AuthService.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that intercepts HTTP requests to validate JWT tokens and set authentication in the security context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Service for JWT operations (extract, validate, etc.)
    private final JwtService jwtService;
    // Loads user details from the database
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Checks for JWT in Authorization header, validates it, and sets authentication if valid.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // If no Authorization header or it doesn't start with Bearer, skip JWT processing
        if(authHeader == null || !authHeader.startsWith("Bearer")) {
            filterChain.doFilter(request,response);
            return;
        }

        // Extract JWT token from header
        final String jwt = authHeader.substring(7);
        final String userName = jwtService.extractUserName(jwt);

        // Get current authentication from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // If username is found and not already authenticated, validate token and set authentication
        if(userName != null && authentication == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userName);

            if(jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // Continue filter chain
        filterChain.doFilter(request,response);
    }
}
