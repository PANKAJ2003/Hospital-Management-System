package com.pms.appointmentservice.grpc;

import billing.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BillingServiceGrpcClient {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(BillingServiceGrpcClient.class);
    private final BillingServiceGrpc.BillingServiceBlockingStub blockingStub;

    public BillingServiceGrpcClient(
            @Value("${billing.service.address:localhost}") String serverAddress,
            @Value("${billing.service.grpc.port:9001}") int serverPort
    ) {
        log.info("Connecting to Billing Service GRPC service at {}:{}", serverAddress, serverPort);
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(serverAddress, serverPort)
                .usePlaintext()
                .build();

        blockingStub = BillingServiceGrpc.newBlockingStub(channel);
    }

    public String findBillingAccountIdByPatientId(String patientId) {
        FindBillingAccountIdRequest request = FindBillingAccountIdRequest.newBuilder()
                .setPatientId(patientId)
                .build();

        try {
            FindBillingAccountIdResponse response = blockingStub.findBillingAccountIdByPatientId(request);
            log.info("Response from findBillingAccountIdByPatientId: {}", response.toString());
            return response.getBillingAccountId();
        } catch (Exception e) {
            log.error("Error in findBillingAccountIdByPatientId", e);
            return null;
        }
    }

    public Optional<UpdateAmountResponse> updateAmountToBillingAccount(String billingAccountId,
                                                                       long amount,
                                                                       long currencyScale,
                                                                       TransactionType transactionType) {
        UpdateAmountRequest request = UpdateAmountRequest.newBuilder()
                .setAccountId(billingAccountId)
                .setAmount(amount)
                .setCurrencyScale(currencyScale)
                .setType(transactionType)
                .build();

        try {
            UpdateAmountResponse response = blockingStub.updateBillingAccountAmount(request);
            log.info("Received response from Billing Service: {}", response.toString());
            return Optional.of(response);
        } catch (Exception e) {
            log.error("Error while adding amount to billing account: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
