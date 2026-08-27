package com.harsh.propertymanagementsystem;

import com.harsh.propertymanagementsystem.auth.dto.RegisterRequest;
import com.harsh.propertymanagementsystem.auth.dto.RegisterResponce;
import com.harsh.propertymanagementsystem.auth.entity.Role;
import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.exception.EmailAlreadyExistsException;
import com.harsh.propertymanagementsystem.auth.exception.PhoneAlreadyExistsException;
import com.harsh.propertymanagementsystem.auth.repository.UserRepository;
import com.harsh.propertymanagementsystem.auth.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .phoneNumber("1234567890")
                .password("password123")
                .role(Role.OWNER)
                .build();

        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("1234567890")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        RegisterResponce response = userService.register(request);

        assertNotNull(response);
        assertEquals("User Registration Success", response.getMsg());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_DuplicateEmail_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("duplicate@test.com")
                .phoneNumber("1234567890")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("duplicate@test.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.register(request));
    }

    @Test
    void testRegister_DuplicatePhone_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .phoneNumber("9999999999")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("9999999999")).thenReturn(true);

        assertThrows(PhoneAlreadyExistsException.class, () -> userService.register(request));
    }
}
