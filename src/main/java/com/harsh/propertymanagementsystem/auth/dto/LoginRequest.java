package com.harsh.propertymanagementsystem.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @JsonAlias({"username", "user", "emailId"})
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    public String getEmail() {
        return email != null ? email.trim() : null;
    }
}
