package com.harsh.propertymanagementsystem.auth.service;

import com.harsh.propertymanagementsystem.auth.dto.LoginResponce;
import com.harsh.propertymanagementsystem.auth.dto.OAuthGoogleRequest;
import com.harsh.propertymanagementsystem.auth.entity.Role;
import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.repository.UserRepository;
import com.harsh.propertymanagementsystem.auth.security.CustomUserDetails;
import com.harsh.propertymanagementsystem.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class GoogleOAuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GOOGLE_TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    @Transactional
    public LoginResponce authenticateWithGoogle(OAuthGoogleRequest request) {
        log.info("Processing Google OAuth login request");

        Map<String, Object> tokenInfo = verifyGoogleIdToken(request.getIdToken());
        if (tokenInfo == null || !tokenInfo.containsKey("email")) {
            throw new IllegalArgumentException("Invalid or unverified Google ID token");
        }

        String email = (String) tokenInfo.get("email");
        String givenName = (String) tokenInfo.get("given_name");
        String familyName = (String) tokenInfo.get("family_name");
        String fullName = (String) tokenInfo.get("name");

        if (givenName == null || givenName.isBlank()) {
            if (fullName != null && !fullName.isBlank()) {
                String[] parts = fullName.trim().split("\\s+", 2);
                givenName = parts[0];
                familyName = parts.length > 1 ? parts[1] : "User";
            } else {
                givenName = "Google";
                familyName = "User";
            }
        }
        if (familyName == null || familyName.isBlank()) {
            familyName = "User";
        }

        Optional<User> existingUserOpt = userRepository.findByEmail(email);
        User user;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            log.info("Google OAuth login for existing user: {}", email);
        } else {
            Role userRole = request.getRole() != null ? request.getRole() : Role.TENANT;
            log.info("Creating new user via Google OAuth: email={}, role={}", email, userRole);

            user = User.builder()
                    .email(email)
                    .firstName(givenName)
                    .lastName(familyName)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .phoneNumber(null)
                    .role(userRole)
                    .authProvider("GOOGLE")
                    .enabled(true)
                    .accountLocked(false)
                    .build();

            user = userRepository.save(user);
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String jwtToken = jwtService.generateToken(userDetails);

        return LoginResponce.builder()
                .token(jwtToken)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .msg("Google authentication successful")
                .build();
    }

    private Map<String, Object> verifyGoogleIdToken(String idToken) {
        try {
            String url = GOOGLE_TOKENINFO_URL + idToken;
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Google ID token verification failed: {}", e.getMessage());
        }
        return null;
    }
}
