package com.locatour.budgetapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String currency; // e.g., "USD"
    private String amount;   // e.g., "9.99"
    private String description; // e.g., "Budget App Premium Subscription"
    private String cancelUrl; // URL where PayPal redirects if payment is cancelled
    private String successUrl; // URL where PayPal redirects after successful payment
}
