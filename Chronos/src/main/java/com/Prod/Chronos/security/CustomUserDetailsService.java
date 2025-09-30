package com.Prod.Chronos.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    // In a real application, this would be loaded from a database
    private final Map<String, UserDetails> users = new HashMap<>();

    public CustomUserDetailsService() {
        // Initialize with some default users for demo purposes
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        users.put("admin", User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build());
        
        users.put("user", User.builder()
                .username("user")
                .password(encoder.encode("user123"))
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build());
        
        users.put("scheduler", User.builder()
                .username("scheduler")
                .password(encoder.encode("scheduler123"))
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_SCHEDULER")))
                .build());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = users.get(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return user;
    }
}
