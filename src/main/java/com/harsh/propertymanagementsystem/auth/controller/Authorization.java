package com.harsh.propertymanagementsystem.auth.controller;

import com.harsh.propertymanagementsystem.auth.dto.LoginRequest;
import com.harsh.propertymanagementsystem.auth.dto.LoginResponce;
import com.harsh.propertymanagementsystem.auth.dto.RegisterRequest;
import com.harsh.propertymanagementsystem.auth.dto.RegisterResponce;
import com.harsh.propertymanagementsystem.auth.exception.EmailAlreadyExistsException;
import com.harsh.propertymanagementsystem.auth.exception.PhoneAlreadyExistsException;
import com.harsh.propertymanagementsystem.auth.service.LoginService;
import com.harsh.propertymanagementsystem.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
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
        log.info("Received registration request for email: {}", request.getEmail());
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponce> login(
            @RequestBody @Valid LoginRequest request) {
        log.info("Received login request for email: {}", request.getEmail());
        return ResponseEntity.ok(loginService.login(request));
    }

    @PostMapping("/protected")
    public ResponseEntity<Map<String, String>> protectedEndpoint() {
        return ResponseEntity.ok(Map.of("message", "ok"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteAccount() {
        log.info("Received request to delete current user account");
        userService.deleteCurrentUserAccount();
        return ResponseEntity.ok(Map.of("message", "User account deleted successfully"));
    }
}
