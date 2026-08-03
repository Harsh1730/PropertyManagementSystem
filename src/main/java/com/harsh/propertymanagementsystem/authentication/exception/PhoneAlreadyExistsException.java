package com.harsh.propertymanagementsystem.authentication.exception;

public class PhoneAlreadyExistsException extends Exception {
    public PhoneAlreadyExistsException() {
        super("Phone Already Exists");
    }
}
