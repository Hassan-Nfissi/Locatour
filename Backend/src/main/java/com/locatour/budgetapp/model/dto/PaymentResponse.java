package com.locatour.budgetapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String status;      // e.g., "success", "failed", "redirect"
    private String redirectUrl; // URL to redirect the user for PayPal approval
    private String paymentId;   // PayPal payment ID
    private String message;     // Optional message
}
