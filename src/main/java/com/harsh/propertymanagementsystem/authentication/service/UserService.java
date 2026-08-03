package com.harsh.propertymanagementsystem.authentication.service;

import com.harsh.propertymanagementsystem.authentication.dto.RegisterRequest;
import com.harsh.propertymanagementsystem.authentication.dto.RegisterResponce;
import com.harsh.propertymanagementsystem.authentication.entity.Role;
import com.harsh.propertymanagementsystem.authentication.entity.User;
import com.harsh.propertymanagementsystem.authentication.exception.EmailAlreadyExistsException;
import com.harsh.propertymanagementsystem.authentication.exception.PhoneAlreadyExistsException;
import com.harsh.propertymanagementsystem.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;

    public User findByEmail(String email) {
        return repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("No User Found With This Email"));
    }

    public User findByPhoneNumber(String phoneNumber) {
        return repo.findByPhoneNumber(phoneNumber).orElseThrow(() -> new UsernameNotFoundException("No User Found with This Phone"));
    }

    public RegisterResponce register(RegisterRequest request) throws EmailAlreadyExistsException, PhoneAlreadyExistsException {

        if (repo.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        if (repo.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new PhoneAlreadyExistsException();
        }

        User user = User.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(request.getPassword())
                .role(Role.TENANT)
                .accountLocked(false)
                .enabled(true)
                .build();
        repo.save(user);
        return new RegisterResponce("User Registeration Success");
    }

}
