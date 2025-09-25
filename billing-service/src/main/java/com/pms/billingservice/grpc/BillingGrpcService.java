package com.pms.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import com.pms.billingservice.model.BillingAccount;
import com.pms.billingservice.service.BillingService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);
    private final BillingService billingService;

    public BillingGrpcService(BillingService billingService) {
        this.billingService = billingService;
    }

    @Override
    public void createBillingAccount(BillingRequest request,
                                     StreamObserver<BillingResponse> responseObserver) {
        try {
            if (request == null || request.getPatientId().isEmpty()) {
                throw new IllegalArgumentException("Patient ID cannot be null");
            }
            log.info("Received request for creating billing account {}", request);

            BillingAccount billingAccount = billingService.createBillingAccount(
                    UUID.fromString(request.getPatientId()));
            log.info("Created billing account {}", billingAccount.toString());
            log.info("Billing account created successfully with ID: {}", billingAccount.getId());

            BillingResponse response = BillingResponse.newBuilder()
                    .setAccountId(billingAccount.getId().toString())
                    .setStatus(billingAccount.getAccountStatus().name())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to create billing account", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Unable to create billing account")
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}
