package com.pms.billingservice.grpc;

import billing.*;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import com.pms.billingservice.model.BillingAccount;
import com.pms.billingservice.service.BillingService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
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

    public void findBillingAccountIdByPatientId(FindBillingAccountIdRequest request,
                                                StreamObserver<FindBillingAccountIdResponse> responseObserver) {
        try {
            if (request.getPatientId().isEmpty()) {
                log.error("Patient ID cannot be null in billingGrpcService: findBillingAccountIdByPatientId()");
                throw new IllegalArgumentException("Patient ID cannot be null");
            }
            log.info("Received request for finding billing account ID for patient id: {}", request.getPatientId());
            String billingAccountId = billingService.findBillingAccountIdByPatientId(request.getPatientId());
            FindBillingAccountIdResponse response = FindBillingAccountIdResponse.newBuilder()
                    .setBillingAccountId(billingAccountId)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Failed to find billing account ID for patient id: {}", request.getPatientId(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Unable to find billing account ID for patient id: " + request.getPatientId())
                    .withCause(e)
                    .asRuntimeException());
        }

    }

    public void updateBillingAccountAmount(UpdateAmountRequest request,
                                           StreamObserver<UpdateAmountResponse> responseObserver) {

        try {
            if (request == null || request.getAccountId().isEmpty()) {
                throw new IllegalArgumentException("Billing account ID cannot be null");
            }
            log.info("Received request for updating amount to billing account {}", request);
            boolean isCredit = request.getType().name().equals("CHARGE");
            Optional<BillingAccount> billingAccount = billingService.updateBillingAccountAmount(request.getAccountId(),
                    request.getAmount() / request.getCurrencyScale(),
                    isCredit);

            if (billingAccount.isEmpty()) {
                log.error("Billing account not found for ID: {}", request.getAccountId());
                throw new IllegalArgumentException("Billing account not found for ID: " + request.getAccountId());
            }

            UpdateAmountResponse response = UpdateAmountResponse.newBuilder()
                    .setAccountId(billingAccount.get().getId().toString())
                    .setStatus(billingAccount.get().getAccountStatus().toString())
                    .setNewBalance(billingAccount.get().getBalance().longValue())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to update billing account amount", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Unable to update billing account amount")
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}
