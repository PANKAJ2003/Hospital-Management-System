package com.pms.billingservice.service.payment;

import com.pms.billingservice.dto.TransactionResponseDTO;
import com.pms.billingservice.dto.VerifyPaymentRequestDTO;
import com.pms.billingservice.enums.PaymentMethod;
import com.pms.billingservice.exception.PaymentProcessingException;
import com.pms.billingservice.model.Transaction;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class RazorpayPaymentProcessor implements PaymentProcessor {

    private static final Logger log = LoggerFactory.getLogger(RazorpayPaymentProcessor.class);

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() {
        try {
            razorpayClient = new RazorpayClient(keyId, keySecret);
            log.info("Razorpay client initialized successfully");
        } catch (RazorpayException e) {
            log.error("Failed to initialize Razorpay client", e);
            throw new PaymentProcessingException("Razorpay initialization failed", e);
        }
    }

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.UPI ||
                method == PaymentMethod.DEBIT_CARD ||
                method == PaymentMethod.CREDIT_CARD;
    }

    @Override
    public TransactionResponseDTO processPayment(Transaction transaction) {
        try {
            JSONObject orderRequest = new JSONObject();
            int amountInPaisa = transaction.getAmount().multiply(BigDecimal.valueOf(100)).intValue();
            orderRequest.put("amount", amountInPaisa);
            orderRequest.put("currency", "INR");

            if (transaction.getDescription() != null) {
                JSONObject notes = new JSONObject();
                notes.put("description", transaction.getDescription());
                orderRequest.put("notes", notes);
            }

            Order order = razorpayClient.orders.create(orderRequest);
            log.info("Razorpay order created: {}", (Object) order.get("id"));

            TransactionResponseDTO response = new TransactionResponseDTO();
            response.setTransactionId(transaction.getId());
            response.setAmount(transaction.getAmount());
            response.setPaymentMethod(transaction.getPaymentMethod());
            response.setStatus(transaction.getPaymentStatus());
            response.setPaymentGateway(transaction.getPaymentGateway());
            response.setBillingAccount(transaction.getBillingAccount().getId());

            Map<String, Object> orderDetails = new HashMap<>();
            orderDetails.put("amount", order.get("amount"));
            orderDetails.put("currency", order.get("currency"));
            orderDetails.put("order_id", order.get("id"));
            orderDetails.put("key", keyId);

            try {
                JSONObject orderNotes = order.get("notes");
                Map<String, Object> notesMap = new HashMap<>();
                for (String key : orderNotes.keySet()) {
                    notesMap.put(key, orderNotes.get(key));
                }
                orderDetails.put("notes", notesMap);
            } catch (Exception e) {
                log.debug("No notes in order");
            }

            response.setGatewayOrderDetails(orderDetails);
            return response;

        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order for transaction: {}", transaction.getId(), e);
            throw new PaymentProcessingException("Failed to create Razorpay order", e);
        }
    }

    @Override
    public boolean verifyPayment(VerifyPaymentRequestDTO request) {
        try {
            String data = request.getOrderId() + "|" + request.getPaymentId();
            boolean isValid = Utils.verifySignature(data, request.getSignature(), keySecret);

            log.info("Payment verification for order {}: {}", request.getOrderId(), isValid ? "VALID" : "INVALID");

            if (!isValid) {
                throw new PaymentProcessingException("Invalid payment signature for orderId: " + request.getOrderId());
            }

            return true;
        } catch (RazorpayException e) {
            log.error("Error verifying payment for orderId: {}", request.getOrderId(), e);
            throw new PaymentProcessingException("Payment verification failed", e);
        }
    }

    @Override
    public String getGatewayName() {
        return "RAZORPAY";
    }

    @Override
    public String handleWebhook(String payload, Map<String, String> headers) {
        try {
            String signature = headers.get("x-razorpay-signature");
            if (!Utils.verifyWebhookSignature(payload, signature, webhookSecret)) {
                log.warn("Invalid Razorpay webhook signature");
                return null;
            }

            JSONObject webhookData = new JSONObject(payload);
            String event = webhookData.getString("event");
            JSONObject paymentEntity = webhookData.getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");
            String orderId = paymentEntity.getString("order_id");

            log.info("Processing webhook event: {} for orderId: {}", event, orderId);

            if ("payment.captured".equals(event) || "order.paid".equals(event)) {
                return orderId + ":SUCCESS";
            } else if ("payment.failed".equals(event)) {
                return orderId + ":FAILED";
            }

            return null;
        } catch (Exception e) {
            log.error("Error processing Razorpay webhook", e);
            return null;
        }
    }
}