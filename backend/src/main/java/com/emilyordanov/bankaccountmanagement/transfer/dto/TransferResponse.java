package com.emilyordanov.bankaccountmanagement.transfer.dto;

import com.emilyordanov.bankaccountmanagement.transfer.Transfer;
import com.emilyordanov.bankaccountmanagement.transfer.TransferType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for a single transfer row.
 * <p>
 * It contains both ids and names because this will be useful for the frontend.
 */
public record TransferResponse(
        Long id,
        String referenceId,
        Long accountId,
        String accountName,
        Long beneficiaryAccountId,
        String beneficiaryAccountName,
        TransferType type,
        BigDecimal amount,
        LocalDateTime createdOn,
        LocalDateTime modifiedOn
) {
    public static TransferResponse fromEntity(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getReferenceId(),
                transfer.getAccount().getId(),
                transfer.getAccount().getName(),
                transfer.getBeneficiaryAccount().getId(),
                transfer.getBeneficiaryAccount().getName(),
                transfer.getType(),
                transfer.getAmount(),
                transfer.getCreatedOn(),
                transfer.getModifiedOn()
        );
    }
}
