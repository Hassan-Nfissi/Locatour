package com.locatour.budgetapp.controller;

import com.locatour.budgetapp.model.User;
import com.locatour.budgetapp.model.dto.SimulationRequest;
import com.locatour.budgetapp.model.dto.SimulationResponse;
import com.locatour.budgetapp.service.AuthService;
import com.locatour.budgetapp.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/budget")
@CrossOrigin(origins = "*")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private AuthService authService;

    @PostMapping("/simulate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> simulateBudget(@RequestBody SimulationRequest request) {
        try {
            // Vérification explicite du statut de l'utilisateur
            User currentUser = authService.getCurrentUser();
            System.out.println("Controller - Utilisateur actuel: " + 
                (currentUser != null ? currentUser.getUsername() : "null") +
                ", Premium: " + (currentUser != null ? currentUser.isSubscribed() : "null"));

            // Si l'utilisateur n'est pas authentifié ou n'est pas premium, forcer la limite
            if (currentUser == null || !currentUser.isSubscribed()) {
                request.setForceLimiteDays(5); // Ajouter ce champ dans SimulationRequest
                System.out.println("Controller - Forçage de la limite à 5 jours pour utilisateur non premium");
            }

            SimulationResponse response = budgetService.simulateBudget(request);
            
            // Double vérification après la simulation
            if (!response.isPremium() && response.getNombreJours() > 5) {
                System.out.println("Controller - Correction du nombre de jours qui dépasse la limite");
                response.setNombreJours(5);
                response.setBudgetParJour(request.getBudgetGlobal() / 5);
            }

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Une erreur est survenue lors de la simulation : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getBudgetOverview() {
        // Temporary mock data
        Map<String, Object> budget = new HashMap<>();
        budget.put("total", 1000.0);
        budget.put("spent", 450.0);
        budget.put("remaining", 550.0);
        budget.put("currency", "EUR");
        
        // Add mock transactions
        List<Map<String, Object>> transactions = new ArrayList<>();
        Map<String, Object> transaction1 = new HashMap<>();
        transaction1.put("id", "1");
        transaction1.put("amount", 150.0);
        transaction1.put("description", "Hôtel Paris");
        transaction1.put("category", "Hébergement");
        transaction1.put("date", "2024-05-25");
        
        Map<String, Object> transaction2 = new HashMap<>();
        transaction2.put("id", "2");
        transaction2.put("amount", 300.0);
        transaction2.put("description", "Billets d'avion");
        transaction2.put("category", "Transport");
        transaction2.put("date", "2024-05-24");
        
        transactions.add(transaction1);
        transactions.add(transaction2);
        budget.put("transactions", transactions);
        
        return ResponseEntity.ok(budget);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Map<String, Object>>> getTransactions() {
        // Temporary mock data
        List<Map<String, Object>> transactions = new ArrayList<>();
        
        Map<String, Object> transaction1 = new HashMap<>();
        transaction1.put("id", "1");
        transaction1.put("amount", 150.0);
        transaction1.put("description", "Hôtel Paris");
        transaction1.put("category", "Hébergement");
        transaction1.put("date", "2024-05-25");
        
        Map<String, Object> transaction2 = new HashMap<>();
        transaction2.put("id", "2");
        transaction2.put("amount", 300.0);
        transaction2.put("description", "Billets d'avion");
        transaction2.put("category", "Transport");
        transaction2.put("date", "2024-05-24");
        
        transactions.add(transaction1);
        transactions.add(transaction2);
        
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/transactions")
    public ResponseEntity<Map<String, Object>> addTransaction(@RequestBody Map<String, Object> transaction) {
        // Temporary mock response
        transaction.put("id", UUID.randomUUID().toString());
        return ResponseEntity.ok(transaction);
    }
}