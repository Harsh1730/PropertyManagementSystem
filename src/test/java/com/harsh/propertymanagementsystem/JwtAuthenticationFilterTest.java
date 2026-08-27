package com.harsh.propertymanagementsystem;

import com.harsh.propertymanagementsystem.auth.entity.Role;
import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.security.CustomUserDetails;
import com.harsh.propertymanagementsystem.auth.security.CustomUserDetailsService;
import com.harsh.propertymanagementsystem.auth.security.JwtAuthenticationFilter;
import com.harsh.propertymanagementsystem.auth.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testFilter_TokenWithExtraWhitespace_HandledGracefully() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        // Simulating "Bearer  token " with spaces
        request.addHeader("Authorization", "Bearer   valid.jwt.token  ");
        request.setRequestURI("/dashboard/owner");
        MockHttpServletResponse response = new MockHttpServletResponse();

        User user = User.builder().id(1L).email("user@test.com").role(Role.OWNER).build();
        CustomUserDetails userDetails = new CustomUserDetails(user);

        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("user@test.com");
        when(customUserDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid.jwt.token", userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user@test.com", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testFilter_TokenWithQuotes_HandledGracefully() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        // Simulating token wrapped in quotes from JSON.stringify
        request.addHeader("Authorization", "Bearer \"valid.jwt.token\"");
        request.setRequestURI("/dashboard/owner");
        MockHttpServletResponse response = new MockHttpServletResponse();

        User user = User.builder().id(1L).email("user@test.com").role(Role.OWNER).build();
        CustomUserDetails userDetails = new CustomUserDetails(user);

        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("user@test.com");
        when(customUserDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid.jwt.token", userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testFilter_TokenAsJsonObject_HandledGracefully() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        // Simulating token stored as JSON object string in localStorage
        request.addHeader("Authorization", "Bearer {\"token\":\"valid.jwt.token\",\"type\":\"Bearer\"}");
        request.setRequestURI("/dashboard/owner");
        MockHttpServletResponse response = new MockHttpServletResponse();

        User user = User.builder().id(1L).email("user@test.com").role(Role.OWNER).build();
        CustomUserDetails userDetails = new CustomUserDetails(user);

        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("user@test.com");
        when(customUserDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid.jwt.token", userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
