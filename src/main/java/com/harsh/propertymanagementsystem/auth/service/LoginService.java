package com.harsh.propertymanagementsystem.auth.service;

import com.harsh.propertymanagementsystem.auth.dto.LoginRequest;
import com.harsh.propertymanagementsystem.auth.dto.LoginResponce;
import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.security.CustomUserDetails;
import com.harsh.propertymanagementsystem.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LoginService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponce login(LoginRequest request) {
        log.info("Attempting authentication for email: {}", request.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        Long userId = null;
        String name = null;
        com.harsh.propertymanagementsystem.auth.entity.Role role = null;

        if (userDetails instanceof CustomUserDetails customUserDetails) {
            User user = customUserDetails.getUser();
            if (user != null) {
                userId = user.getId();
                name = user.getName();
                role = user.getRole();
            }
        }

        log.info("Authentication successful for email: {}", request.getEmail());
        return LoginResponce.builder()
                .token(token)
                .type("Bearer")
                .userId(userId)
                .email(userDetails.getUsername())
                .name(name)
                .role(role)
                .msg("Login successful")
                .build();
    }
}
