package com.pms.billingservice.controller;

import com.pms.billingservice.dto.TransactionRequestDTO;
import com.pms.billingservice.dto.TransactionResponseDTO;
import com.pms.billingservice.dto.VerifyPaymentRequestDTO;
import com.pms.billingservice.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/billing/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(@Valid @RequestBody TransactionRequestDTO request) {
        return ResponseEntity
                .ok(transactionService.processTransaction(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<TransactionResponseDTO> verifyTransaction(@Valid @RequestBody VerifyPaymentRequestDTO request) {
        return ResponseEntity
                .ok(transactionService.verifyTransaction(request));
    }

    @PostMapping("/webhooks/{gateway}")
    public ResponseEntity<String> handleWebhook(
            @PathVariable String gateway,
            @RequestBody String payload,
            @RequestHeader Map<String, String> headers) {

        transactionService.handleWebhook(gateway.toUpperCase(), payload, headers);
        return ResponseEntity.ok("Payment processed successfully");
    }
}
