package com.pms.billingservice.controller;

import com.pms.billingservice.dto.TransactionListResponseDTO;
import com.pms.billingservice.dto.TransactionRequestDTO;
import com.pms.billingservice.dto.TransactionResponseDTO;
import com.pms.billingservice.dto.VerifyPaymentRequestDTO;
import com.pms.billingservice.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/billing/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @Valid @RequestBody TransactionRequestDTO request,
            HttpServletRequest httpRequest
    ) {

        String role = httpRequest.getHeader("X-User-Role");

        return ResponseEntity
                .ok(transactionService.processTransaction(request,role));
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

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDTO> getTransaction(@PathVariable UUID transactionId) {
        return ResponseEntity.ok(transactionService.getTransaction(transactionId));
    }

    @GetMapping("all/{patientId}")
    public ResponseEntity<TransactionListResponseDTO> getAllTransactions(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(transactionService.getAllTransactionsByPatientId(patientId, page, size));
    }
}
