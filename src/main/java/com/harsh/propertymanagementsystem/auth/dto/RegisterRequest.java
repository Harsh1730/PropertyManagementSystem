package com.harsh.propertymanagementsystem.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.harsh.propertymanagementsystem.auth.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @JsonAlias({"username", "user", "emailId"})
    private String email;

    @NotBlank(message = "Phone number is required")
    @JsonAlias({"phone", "phoneNo", "mobile", "mobileNumber"})
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 4, message = "Password must be at least 4 characters long")
    private String password;

    private Role role;

    public String getEmail() {
        return email != null ? email.trim() : null;
    }

    public String getPhoneNumber() {
        return phoneNumber != null ? phoneNumber.trim() : null;
    }
}
