package com.harsh.propertymanagementsystem.authentication.exception;

import jakarta.validation.constraints.Email;

public class EmailAlreadyExistsException extends Exception {
    public EmailAlreadyExistsException(){
        super("Email Already Exists");
    }
}
