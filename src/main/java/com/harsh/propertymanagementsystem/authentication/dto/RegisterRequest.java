package com.harsh.propertymanagementsystem.authentication.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    String password;
}
