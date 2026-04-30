package com.emilyordanov.bankaccountmanagement.transfer.dto;

import com.emilyordanov.bankaccountmanagement.transfer.Transfer;
import com.emilyordanov.bankaccountmanagement.transfer.TransferType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for a single transfer row.
 * <p>
 * This response represents one account statement entry.
 * <p>
 * Important:
 * <p>
 * accountId/accountName:
 * - the account for which this transfer row is created
 * <p>
 * counterpartyAccountId/counterpartyAccountName:
 * - the other account involved in the transfer
 * <p>
 * The database column is called beneficiary_account_id because this was part of
 * the assignment requirements. However, in transfer history this value is easier
 * to understand as "counterparty".
 * <p>
 * Example 1:
 * <p>
 * Main Account sends 100.00 to Savings Account.
 * <p>
 * For Main Account history:
 * type = DEBIT
 * accountName = Main Account
 * counterpartyAccountName = Savings Account
 * <p>
 * UI meaning:
 * Main Account sent 100.00 to Savings Account.
 * <p>
 * Example 2:
 * <p>
 * For Savings Account history:
 * type = CREDIT
 * accountName = Savings Account
 * counterpartyAccountName = Main Account
 * <p>
 * UI meaning:
 * Savings Account received 100.00 from Main Account.
 */
public record TransferResponse(
        Long id,
        String referenceId,
        Long accountId,
        String accountName,
        Long counterpartyAccountId,
        String counterpartyAccountName,
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
