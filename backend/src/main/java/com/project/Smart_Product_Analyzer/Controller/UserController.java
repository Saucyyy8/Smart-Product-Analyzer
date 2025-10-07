package com.project.Smart_Product_Analyzer.Controller;

import com.project.Smart_Product_Analyzer.AuthService.UserService;
import com.project.Smart_Product_Analyzer.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * REST controller for user registration and login endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class UserController {

    // Service for user registration and authentication logic
    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    /**
     * Registers a new user using the UserService.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.register(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * Authenticates a user and returns a JWT token if successful.
     */
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody User user) {
        String token = userService.verify(user);
        return Collections.singletonMap("token", token);
    }

}
