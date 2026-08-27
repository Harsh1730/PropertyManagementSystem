package com.harsh.propertymanagementsystem;

import com.harsh.propertymanagementsystem.auth.dto.LoginRequest;
import com.harsh.propertymanagementsystem.auth.dto.LoginResponce;
import com.harsh.propertymanagementsystem.auth.entity.Role;
import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.security.CustomUserDetails;
import com.harsh.propertymanagementsystem.auth.security.CustomUserDetailsService;
import com.harsh.propertymanagementsystem.auth.security.JwtService;
import com.harsh.propertymanagementsystem.auth.service.LoginService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthIntegrationTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @InjectMocks
    private LoginService loginService;

    @Test
    void testLoginSuccess() {
        User testUser = User.builder()
                .id(1L)
                .email("user@test.com")
                .firstName("Alice")
                .lastName("Smith")
                .role(Role.TENANT)
                .password("encoded")
                .enabled(true)
                .accountLocked(false)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(testUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("mocked.jwt.token");

        LoginRequest request = LoginRequest.builder()
                .email("user@test.com")
                .password("password123")
                .build();

        LoginResponce response = loginService.login(request);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(1L, response.getUserId());
        assertEquals("user@test.com", response.getEmail());
        assertEquals("Alice Smith", response.getName());
        assertEquals(Role.TENANT, response.getRole());
    }

    @Test
    void testLogin_BadCredentials_ThrowsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequest request = LoginRequest.builder()
                .email("user@test.com")
                .password("wrongpassword")
                .build();

        assertThrows(BadCredentialsException.class, () -> loginService.login(request));
    }
}
