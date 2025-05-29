package com.locatour.budgetapp.service;

import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PayPalService {
    private static final Logger logger = LoggerFactory.getLogger(PayPalService.class);

    private final PayPalHttpClient payPalClient;

    @Value("${app.subscription.amount}")
    private String amount;

    @Value("${app.subscription.currency}")
    private String currency;

    @Value("${app.subscription.description}")
    private String description;

    public PayPalService(PayPalHttpClient payPalClient) {
        this.payPalClient = payPalClient;
    }

    public String createPayment() {
        OrdersCreateRequest request = new OrdersCreateRequest();
        request.prefer("return=representation");
        request.requestBody(buildRequestBody());

        try {
            HttpResponse<Order> response = payPalClient.execute(request);
            Order order = response.result();
            return extractApprovalUrl(order);
        } catch (IOException e) {
            logger.error("Error creating PayPal payment: ", e);
            throw new RuntimeException("Failed to create PayPal payment", e);
        }
    }

    public boolean executePayment(String token) {
        try {
            // First, get the order details using the token
            OrdersGetRequest getRequest = new OrdersGetRequest(token);
            HttpResponse<Order> getResponse = payPalClient.execute(getRequest);
            Order order = getResponse.result();

            // Then capture the payment
            OrdersCaptureRequest captureRequest = new OrdersCaptureRequest(order.id());
            captureRequest.requestBody(new OrderRequest());

            HttpResponse<Order> captureResponse = payPalClient.execute(captureRequest);
            return "COMPLETED".equals(captureResponse.result().status());
        } catch (IOException e) {
            logger.error("Error executing PayPal payment: ", e);
            throw new RuntimeException("Failed to execute PayPal payment", e);
        }
    }

    private OrderRequest buildRequestBody() {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");

        ApplicationContext applicationContext = new ApplicationContext()
                .brandName("Locatour")
                .landingPage("BILLING")
                .userAction("PAY_NOW")
                .returnUrl("http://localhost:8080/api/paypal/success")
                .cancelUrl("http://localhost:8080/api/paypal/cancel");
        orderRequest.applicationContext(applicationContext);

        List<PurchaseUnitRequest> purchaseUnitRequests = new ArrayList<>();
        PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest()
                .description(description)
                .amountWithBreakdown(new AmountWithBreakdown().currencyCode(currency).value(amount));

        purchaseUnitRequests.add(purchaseUnitRequest);
        orderRequest.purchaseUnits(purchaseUnitRequests);
        return orderRequest;
    }

    private String extractApprovalUrl(Order order) {
        for (LinkDescription link : order.links()) {
            if ("approve".equals(link.rel())) {
                return link.href();
            }
        }
        throw new RuntimeException("No approval URL found in PayPal response");
    }
}
