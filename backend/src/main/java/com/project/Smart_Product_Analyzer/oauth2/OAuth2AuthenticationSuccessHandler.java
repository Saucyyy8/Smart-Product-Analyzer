package com.project.Smart_Product_Analyzer.oauth2;

import com.project.Smart_Product_Analyzer.AuthService.JwtService;
import com.project.Smart_Product_Analyzer.entity.User;
import com.project.Smart_Product_Analyzer.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public OAuth2AuthenticationSuccessHandler(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = attr(oAuth2User, "email");
        String login = attr(oAuth2User, "login"); // GitHub
        String name = attr(oAuth2User, "name");
        String sub = attr(oAuth2User, "sub"); // Google subject

        String username = firstNonBlank(email, login, name, sub != null ? ("oauth_" + sub) : null);
        if (username == null) {
            username = "oauth_user_" + System.currentTimeMillis();
        }

        // Create the user if not present. Store password as null for OAuth users (industry practice).
        User user = userRepository.findByUsername(username);
        if (Objects.isNull(user)) {
            user = new User();
            user.setUsername(username);
            user.setPassword(null); // password not set for OAuth accounts
            userRepository.save(user);
        }

        String token = jwtService.generateToken(username);

        // Redirect to frontend with token
        String frontendUrl = "http://localhost:3000/login?token=" +
                           URLEncoder.encode(token, StandardCharsets.UTF_8);
        response.sendRedirect(frontendUrl);
    }

    private String attr(OAuth2User user, String key) {
        Object v = user.getAttribute(key);
        return v == null ? null : String.valueOf(v);
    }

    private String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}
