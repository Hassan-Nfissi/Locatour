package com.locatour.budgetapp.controller;

import com.locatour.budgetapp.model.User;
import com.locatour.budgetapp.service.AuthService;
import com.locatour.budgetapp.service.PayPalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/paypal")
public class PayPalController {

    @Autowired
    private PayPalService payPalService;

    @Autowired
    private AuthService authService;

    @PostMapping("/create")
    public ResponseEntity<?> createPayment() {
        try {
            String approvalUrl = payPalService.createPayment();
            Map<String, String> response = new HashMap<>();
            response.put("approvalUrl", approvalUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/success")
    public ResponseEntity<?> completePayment(@RequestParam("token") String token) {
        try {
            User user = authService.getAuthenticatedUser();
            boolean success = payPalService.executePayment(token);
            
            if (success) {
                user.setSubscribed(true);
                authService.updateUser(user);
                
                Map<String, String> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Payment completed successfully");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Payment execution failed");
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/cancel")
    public ResponseEntity<?> cancelPayment() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "cancelled");
        response.put("message", "Payment was cancelled");
        return ResponseEntity.ok(response);
    }
}
