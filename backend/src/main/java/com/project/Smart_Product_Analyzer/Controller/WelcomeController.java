package com.project.Smart_Product_Analyzer.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple controller for welcome message and CSRF token endpoint.
 */
@RestController
public class WelcomeController {

    /**
     * Returns a welcome message for the root endpoint.
     */
    @GetMapping("")
    public String welcome() {
        return "Welcome to daily Code Buffer!!";
    }

    /**
     * Returns the CSRF token for the current session.
     */
    @GetMapping("/csrf")
    public CsrfToken getToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute("_csrf");
    }

    /**
     * Endpoint used as a redirect target after successful OAuth2 login.
     * It simply returns the JWT token passed as a query parameter.
     */
    @GetMapping("/oauth2/success")
    public String oauth2Success(@RequestParam("token") String token) {
        return token;
    }
}
