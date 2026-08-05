package com.harsh.propertymanagementsystem.auth.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    String password;
}
