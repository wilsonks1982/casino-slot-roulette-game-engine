package com.wildace.roulette.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(SpaController.class);

    private final UserDetailsService userDetailsService;

    public AuthController(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        log.info("Login attempt for user: {} with password: {}", username, password);

        try {
            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Verify password (simplified - use AuthenticationManager in production)
            if (!userDetails.getPassword().equals("{noop}" + password)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid credentials"));
            }

            // Extract roles
            var roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replace("ROLE_", "")) // Remove ROLE_ prefix
                    .collect(Collectors.toList());

            Map<String, Object> response = Map.of(
                    "user", Map.of(
                            "id", 1,
                            "username", username,
                            "email", username + "@wildace.com",
                            "roles", roles  // ← Include roles
                    ),
                    "token", "jwt-token-for-" + username
            );

            log.info("User {} logged in successfully with roles: {}", username, roles);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Login failed for user: {}", username, e);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("Logout request received");
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}