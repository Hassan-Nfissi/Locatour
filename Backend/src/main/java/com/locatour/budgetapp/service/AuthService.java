package com.locatour.budgetapp.service;

import com.locatour.budgetapp.model.User;
import com.locatour.budgetapp.model.dto.AuthResponse; // Import AuthResponse
import com.locatour.budgetapp.model.dto.LoginRequest; // Import LoginRequest
import com.locatour.budgetapp.model.dto.RegisterRequest;
import com.locatour.budgetapp.repository.UserRepository;
import com.locatour.budgetapp.security.jwt.JwtTokenProvider; // Import JwtTokenProvider
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager; // Import AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager; // Autowire AuthenticationManager

    @Autowired
    private JwtTokenProvider jwtTokenProvider; // Autowire JwtTokenProvider

    @Value("${app.freeTriesLimit}")
    private int freeTriesLimit;

    // Modified registerUser method to return AuthResponse
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists!");
        }
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists!");
        }

        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setFreeTriesLeft(freeTriesLimit); // Set initial free tries
        newUser.setSubscribed(false); // New users are not subscribed initially

        User savedUser = userRepository.save(newUser);

        // For registration, we typically don't return a JWT token immediately
        // The frontend would call /login after successful registration.
        // But for consistency with AuthResponse, we can populate relevant fields.
        return new AuthResponse(
                null, // No token on registration unless auto-logging in
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.isSubscribed(),
                savedUser.getFreeTriesLeft()
        );
    }

    // Add this method for user login, returning AuthResponse
    public AuthResponse loginUser(LoginRequest loginRequest) {
        try {
            // Vérifier si c'est le compte dev-user
            if ("dev-user".equals(loginRequest.getUsername())) {
                User devUser = userRepository.findByUsername("dev-user")
                    .orElseGet(() -> {
                        User newDevUser = new User();
                        newDevUser.setUsername("dev-user");
                        newDevUser.setEmail("dev@example.com");
                        newDevUser.setPassword(passwordEncoder.encode("dev-password"));
                        newDevUser.setSubscribed(true); // Toujours premium
                        newDevUser.setFreeTriesLeft(-1); // -1 pour les utilisateurs premium (essais illimités)
                        return userRepository.save(newDevUser);
                    });

                // Vérifier le mot de passe pour dev-user
                if (!passwordEncoder.matches(loginRequest.getPassword(), devUser.getPassword())) {
                    throw new BadCredentialsException("Mot de passe incorrect pour dev-user");
                }
                
                // Créer une authentification pour dev-user
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    devUser.getUsername(),
                    devUser.getPassword(),
                    devUser.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // Générer un token JWT valide pour dev-user
                String jwt = jwtTokenProvider.generateToken(authentication);
                
                return new AuthResponse(
                    jwt,
                    devUser.getUsername(),
                    devUser.getEmail(),
                    true, // Toujours premium pour dev-user
                    -1 // -1 pour les utilisateurs premium (essais illimités)
                );
            }
            
            // Pour les autres utilisateurs, continuer avec la logique normale
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken(authentication);
            
            User user = getAuthenticatedUser();
            return new AuthResponse(
                jwt,
                user.getUsername(),
                user.getEmail(),
                user.isSubscribed(),
                user.getFreeTriesLeft()
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username/password");
        }
    }

    private User createDevUser() {
        User devUser = new User();
        devUser.setUsername("dev-user");
        devUser.setEmail("dev@example.com");
        devUser.setSubscribed(true); // Toujours premium
        devUser.setFreeTriesLeft(-1); // -1 pour les utilisateurs premium (essais illimités)
        devUser.setPassword(passwordEncoder.encode("dev-password"));
        return userRepository.save(devUser);
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated.");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional
    public void updateUser(User user) {
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Récupère l'utilisateur actuellement connecté.
     * @return L'utilisateur connecté ou null si aucun utilisateur n'est connecté
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return createTemporaryUser();
        }

        String username = authentication.getName();
        if ("anonymousUser".equals(username)) {
            return createTemporaryUser();
        }

        return userRepository.findByUsername(username)
                .orElseGet(this::createTemporaryUser);
    }

    private User createTemporaryUser() {
        // Vérifier si l'utilisateur temporaire existe déjà
        return userRepository.findByUsername("dev-user")
                .orElseGet(() -> {
                    User tempUser = new User();
                    tempUser.setUsername("dev-user");
                    tempUser.setEmail("dev@example.com");
                    tempUser.setSubscribed(true); // Toujours premium
                    tempUser.setFreeTriesLeft(-1); // -1 pour les utilisateurs premium (essais illimités)
                    tempUser.setPassword(passwordEncoder.encode("dev-password"));
                    return userRepository.save(tempUser);
                });
    }

    /**
     * Vérifie si l'utilisateur actuel est premium.
     * @return true si l'utilisateur est premium, false sinon
     */
    public boolean isCurrentUserPremium() {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.isSubscribed();
    }

    /**
     * Met à jour le statut premium d'un utilisateur.
     * @param username Le nom d'utilisateur
     * @param isPremium Le nouveau statut premium
     * @return L'utilisateur mis à jour
     */
    public User updatePremiumStatus(String username, boolean isPremium) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setSubscribed(isPremium);
        if (isPremium) {
            user.setFreeTriesLeft(-1); // -1 pour indiquer un nombre illimité d'essais
        }
        return userRepository.save(user);
    }
}