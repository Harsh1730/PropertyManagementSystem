package com.harsh.propertymanagementsystem.auth.exception;

public class PhoneAlreadyExistsException extends Exception {
    public PhoneAlreadyExistsException() {
        super("Phone Already Exists");
    }
}
