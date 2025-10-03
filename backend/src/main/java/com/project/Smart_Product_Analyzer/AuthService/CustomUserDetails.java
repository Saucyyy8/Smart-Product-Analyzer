package com.project.Smart_Product_Analyzer.AuthService;


import com.project.Smart_Product_Analyzer.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom implementation of UserDetails to integrate your User entity
 * with Spring Security's authentication system.
 */
public class CustomUserDetails implements UserDetails {

    // The application's User entity
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * Returns the authorities granted to the user. Here, every user has 'USER' role.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("USER"));
    }

    /**
     * Returns the user's password.
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Returns the username used to authenticate the user.
     */
    @Override
    public String getUsername() {
        return user.getUserName();
    }

    // The following methods indicate the account is always valid and enabled.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
