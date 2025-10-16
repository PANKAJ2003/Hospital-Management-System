package com.pms.billingservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class TransactionListResponseDTO {

    private List<TransactionDetailDTO> transactions;

    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean lastPage;

    public TransactionListResponseDTO() {}

    public TransactionListResponseDTO(List<TransactionDetailDTO> transactions, int pageNumber, int pageSize,
                                      long totalElements, int totalPages, boolean lastPage) {
        this.transactions = transactions;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.lastPage = lastPage;
    }

}
