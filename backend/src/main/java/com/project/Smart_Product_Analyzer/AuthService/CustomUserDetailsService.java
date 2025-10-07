package com.project.Smart_Product_Analyzer.AuthService;

import com.project.Smart_Product_Analyzer.entity.User;
import com.project.Smart_Product_Analyzer.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Service to load user-specific data for authentication.
 * Implements Spring Security's UserDetailsService.
 */
@Component
public class CustomUserDetailsService implements UserDetailsService {

    // Repository to access user data
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username for authentication.
     * Throws UsernameNotFoundException if user is not found.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if(Objects.isNull(user)) {
            System.out.println("User not available");
            throw new UsernameNotFoundException("User not found");
        }
        return new CustomUserDetails(user);
    }
}
