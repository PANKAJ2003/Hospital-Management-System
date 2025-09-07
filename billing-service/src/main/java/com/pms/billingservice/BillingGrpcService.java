package com.pms.billingservice;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);

    @Override
    public void createBillingAccount(BillingRequest request,
            StreamObserver<BillingResponse> responseObserver) {

        log.info("Received request for creating billing account {}", request.toString());

        BillingResponse response = BillingResponse.newBuilder()
                                                  .setAccountId(UUID.randomUUID().toString())
                                                  .setStatus("ACTIVE")
                                                  .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }
}
