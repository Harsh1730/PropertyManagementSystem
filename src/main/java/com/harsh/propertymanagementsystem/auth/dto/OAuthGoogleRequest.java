package com.harsh.propertymanagementsystem.auth.dto;

import com.harsh.propertymanagementsystem.auth.entity.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthGoogleRequest {
    @NotBlank(message = "Google ID token is required")
    private String idToken;

    private Role role;
}
