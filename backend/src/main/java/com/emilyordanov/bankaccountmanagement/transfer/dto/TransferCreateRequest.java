package com.emilyordanov.bankaccountmanagement.transfer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request DTO used when the user creates a transfer.
 * <p>
 * The client should only send:
 * - source account id
 * - beneficiary account id
 * - amount
 * <p>
 * The client should NOT send:
 * - type
 * - referenceId
 * - createdOn
 * - modifiedOn
 * <p>
 * These values are controlled by the backend.
 */
public record TransferCreateRequest(
        @NotNull(message = "Source account id is required")
        Long accountId,

        @NotNull(message = "Beneficiary account id is required")
        Long beneficiaryAccountId,

        @NotNull(message = "Transfer amount is required")
        @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
        @Digits(integer = 17, fraction = 2, message = "Transfer amount must have at most 17 digits and 2 decimal places")
        BigDecimal amount
) {
}
