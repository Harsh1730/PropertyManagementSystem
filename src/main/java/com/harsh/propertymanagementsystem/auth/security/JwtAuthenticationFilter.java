package com.harsh.propertymanagementsystem.auth.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsh.propertymanagementsystem.common.dto.ErrorResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String trimmedHeader = authHeader.trim();
        if (!trimmedHeader.regionMatches(true, 0, "Bearer", 0, 6)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token after "Bearer" (handles varying whitespace, case insensitivity)
        String jwt = trimmedHeader.substring(6).trim();

        // Handle possible nested/duplicated "Bearer " prefixes
        while (jwt.regionMatches(true, 0, "Bearer", 0, 6)) {
            jwt = jwt.substring(6).trim();
        }

        // Handle case where frontend stored the entire JSON response object as the token in localStorage
        if (jwt.startsWith("{") && jwt.endsWith("}")) {
            try {
                JsonNode jsonNode = objectMapper.readTree(jwt);
                if (jsonNode.has("token")) {
                    jwt = jsonNode.get("token").asText();
                } else if (jsonNode.has("accessToken")) {
                    jwt = jsonNode.get("accessToken").asText();
                } else if (jsonNode.has("jwt")) {
                    jwt = jsonNode.get("jwt").asText();
                }
            } catch (Exception ignored) {
            }
        }

        // Strip surrounding quotes if token was stored as a stringified JSON string
        if ((jwt.startsWith("\"") && jwt.endsWith("\"")) || (jwt.startsWith("'") && jwt.endsWith("'"))) {
            jwt = jwt.substring(1, jwt.length() - 1).trim();
        }

        // Strip all whitespace/newlines from compact JWT
        jwt = jwt.replaceAll("\\s+", "");

        // If no token or invalid placeholder, let it proceed to authentication entrypoint
        if (jwt.isEmpty() || "undefined".equalsIgnoreCase(jwt) || "null".equalsIgnoreCase(jwt) || "[objectObject]".equalsIgnoreCase(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtService.extractUsername(jwt);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        customUserDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            }

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT authentication error for URI {}: {}", request.getRequestURI(), e.getMessage());
            SecurityContextHolder.clearContext();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            ErrorResponse errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .error("Unauthorized")
                    .message("Invalid or expired token: " + e.getMessage())
                    .path(request.getRequestURI())
                    .build();

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return;
        }

        filterChain.doFilter(request, response);
    }
}