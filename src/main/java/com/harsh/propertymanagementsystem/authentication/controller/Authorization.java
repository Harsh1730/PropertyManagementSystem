package com.harsh.propertymanagementsystem.authentication.controller;

import com.harsh.propertymanagementsystem.authentication.dto.LoginRequest;
import com.harsh.propertymanagementsystem.authentication.dto.LoginResponce;
import com.harsh.propertymanagementsystem.authentication.dto.RegisterRequest;
import com.harsh.propertymanagementsystem.authentication.dto.RegisterResponce;
import com.harsh.propertymanagementsystem.authentication.exception.EmailAlreadyExistsException;
import com.harsh.propertymanagementsystem.authentication.exception.PhoneAlreadyExistsException;
import com.harsh.propertymanagementsystem.authentication.service.LoginService;
import com.harsh.propertymanagementsystem.authentication.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class Authorization {
    private final UserService userService;
    private final LoginService loginService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponce> register(
            @RequestBody @Valid RegisterRequest request)
            throws PhoneAlreadyExistsException, EmailAlreadyExistsException {
        return ResponseEntity.ok(userService.register(request));

    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        System.out.println("login requested ");
        loginService.login(request);
        return "Login Successful";
    }
}
