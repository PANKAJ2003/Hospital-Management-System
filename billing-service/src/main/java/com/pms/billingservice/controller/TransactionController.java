package com.pms.billingservice.controller;

import com.pms.billingservice.dto.TransactionCreateRequest;
import com.pms.billingservice.dto.TransactionResponseDTO;
import com.pms.billingservice.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/billing/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(@Valid @RequestBody TransactionCreateRequest request) {
        return ResponseEntity
                .ok(transactionService.processTransaction(request.getTransaction(), request.getPaymentDetails()));
    }
}
