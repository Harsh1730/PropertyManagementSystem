package com.harsh.propertymanagementsystem.auth.exception;

public class EmailAlreadyExistsException extends Exception {
    public EmailAlreadyExistsException(){
        super("Email Already Exists");
    }
}
