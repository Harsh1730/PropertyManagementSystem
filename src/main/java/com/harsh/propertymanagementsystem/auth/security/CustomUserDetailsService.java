package com.harsh.propertymanagementsystem.auth.security;

import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            log.warn("loadUserByUsername called with null or blank username");
            throw new UsernameNotFoundException("Username/Email cannot be empty");
        }

        String lookupEmail = username.trim();
        User user = userRepository.findByEmailIgnoreCase(lookupEmail)
                .or(() -> userRepository.findByEmail(lookupEmail))
                .orElseThrow(() -> {
                    log.warn("User lookup failed for email: {}", lookupEmail);
                    return new UsernameNotFoundException("No user found with email: " + lookupEmail);
                });

        return new CustomUserDetails(user);
    }
}
