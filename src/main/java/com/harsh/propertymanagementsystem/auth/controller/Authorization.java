package com.harsh.propertymanagementsystem.auth.controller;

import com.harsh.propertymanagementsystem.auth.dto.LoginRequest;
import com.harsh.propertymanagementsystem.auth.dto.RegisterRequest;
import com.harsh.propertymanagementsystem.auth.dto.RegisterResponce;
import com.harsh.propertymanagementsystem.auth.exception.EmailAlreadyExistsException;
import com.harsh.propertymanagementsystem.auth.exception.PhoneAlreadyExistsException;
import com.harsh.propertymanagementsystem.auth.service.LoginService;
import com.harsh.propertymanagementsystem.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return loginService.login(request);
    }

    @PostMapping("/protected")
    public String protectedd() {
        return "ok";
    }
}
