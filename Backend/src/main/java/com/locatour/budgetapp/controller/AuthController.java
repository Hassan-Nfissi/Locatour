package com.locatour.budgetapp.controller;

import com.locatour.budgetapp.model.User;
import com.locatour.budgetapp.model.dto.AuthResponse;
import com.locatour.budgetapp.model.dto.LoginRequest;
import com.locatour.budgetapp.model.dto.RegisterRequest;
import com.locatour.budgetapp.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // Log registration attempt
            System.out.println("Registration attempt for user: " + registerRequest.getUsername());
            
            // Validate request
            if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Username is required"));
            }
            if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
            }
            if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Password is required"));
            }

            AuthResponse response = authService.registerUser(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            // Log the error
            System.err.println("Registration error: " + e.getMessage());
            return ResponseEntity
                .badRequest()
                .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Registration failed"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Validation
            if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Le nom d'utilisateur est requis"));
            }
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Le mot de passe est requis"));
            }

            // Utiliser le service d'authentification pour la connexion
            try {
                AuthResponse authResponse = authService.loginUser(loginRequest);
                return ResponseEntity.ok(authResponse);
            } catch (Exception e) {
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Nom d'utilisateur ou mot de passe incorrect"));
            }
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Erreur lors de la connexion"));
        }
    }

    @GetMapping("/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = authService.getAllUsers();
        List<Map<String, Object>> userDTOs = users.stream()
            .map(user -> {
                Map<String, Object> userDTO = new HashMap<>();
                userDTO.put("id", user.getId());
                userDTO.put("username", user.getUsername());
                userDTO.put("email", user.getEmail());
                userDTO.put("isSubscribed", user.isSubscribed());
                userDTO.put("freeTriesLeft", user.getFreeTriesLeft());
                return userDTO;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }
}
