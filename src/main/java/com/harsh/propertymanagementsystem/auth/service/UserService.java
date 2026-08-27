package com.harsh.propertymanagementsystem.auth.service;

import com.harsh.propertymanagementsystem.auth.dto.RegisterRequest;
import com.harsh.propertymanagementsystem.auth.dto.RegisterResponce;
import com.harsh.propertymanagementsystem.auth.entity.Role;
import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.exception.EmailAlreadyExistsException;
import com.harsh.propertymanagementsystem.auth.exception.PhoneAlreadyExistsException;
import com.harsh.propertymanagementsystem.auth.repository.UserRepository;
import com.harsh.propertymanagementsystem.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public User findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User findByEmail(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No User Found With Email: " + email));
    }

    public User findByPhoneNumber(String phoneNumber) {
        return repo.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("No User Found with Phone: " + phoneNumber));
    }

    public RegisterResponce register(RegisterRequest request) throws EmailAlreadyExistsException, PhoneAlreadyExistsException {
        if (repo.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists: {}", request.getEmail());
            throw new EmailAlreadyExistsException();
        }

        if (repo.existsByPhoneNumber(request.getPhoneNumber())) {
            log.warn("Registration failed: Phone number already exists: {}", request.getPhoneNumber());
            throw new PhoneAlreadyExistsException();
        }

        Role userRole = request.getRole() != null ? request.getRole() : Role.TENANT;

        User user = User.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(encoder.encode(request.getPassword()))
                .role(userRole)
                .accountLocked(false)
                .enabled(true)
                .build();

        repo.save(user);
        log.info("Successfully registered new user: {} with role: {}", user.getEmail(), user.getRole());
        return new RegisterResponce("User Registration Success");
    }
}
