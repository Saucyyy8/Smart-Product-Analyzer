package com.project.Smart_Product_Analyzer.AuthService;


import com.project.Smart_Product_Analyzer.entity.User;
import com.project.Smart_Product_Analyzer.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service containing business logic for user registration and authentication.
 */
@Service
public class UserService {

    // Repository for user data access
    private final UserRepository userRepository;
    // Password encoder for hashing passwords
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    // Authentication manager for verifying credentials
    private final AuthenticationManager authenticationManager;
    // Service for JWT operations
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new user by encoding the password and saving to the database.
     */
    public User register(User user) {
        User existingUser = userRepository.findByUsername(user.getUsername());

        if (existingUser != null) {
            // User exists. Check if it's an OAuth user without a password.
            if (existingUser.getPassword() == null) {
                // This is an OAuth user. Let's set their password.
                existingUser.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
                return userRepository.save(existingUser);
            } else {
                // This is a regular user who already has a password.
                throw new BadCredentialsException("Username is already taken.");
            }
        }
        // New user, hash their password and save.
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Authenticates the user and returns a JWT token if successful.
     */
    public String verify(User user) {
        Authentication authenticate
                = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(), user.getPassword()
                )
        );

        if(authenticate.isAuthenticated())
            return jwtService.generateToken(user.getUsername());
        return "failure";
    }
}
