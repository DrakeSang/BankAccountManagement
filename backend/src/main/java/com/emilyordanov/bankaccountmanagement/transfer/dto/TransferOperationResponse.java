package com.emilyordanov.bankaccountmanagement.transfer.dto;

/**
 * Response returned after creating one business transfer operation.
 * <p>
 * One business transfer creates two transfer rows:
 * - debitTransfer for the source account
 * - creditTransfer for the beneficiary account
 * <p>
 * Both rows share the same referenceId.
 */
public record TransferOperationResponse(
        String referenceId,
        TransferResponse debitTransfer,
        TransferResponse creditTransfer
) {
}
