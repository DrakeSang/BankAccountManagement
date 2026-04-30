package com.emilyordanov.bankaccountmanagement.account.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO used when updating an existing account.
 *
 * We allow updating:
 * - name
 * - IBAN
 * - availableAmount
 *
 * We intentionally do not allow changing status here.
 *
 * Status is changed only through dedicated business endpoints:
 * PATCH /api/accounts/{id}/freeze
 * PATCH /api/accounts/{id}/unfreeze
 *
 * This makes the API clearer because freezing/unfreezing is a business action,
 * not just a generic field update.
 */
public record AccountUpdateRequest(
        @NotBlank(message = "Account name is required")
        @Size(max = 100, message = "Account name must be at most 100 characters")
        String name,

        @NotBlank(message = "IBAN is required")
        @Size(max = 34, message = "IBAN must be at most 34 characters")
        String iban,

        @NotNull(message = "Available amount is required")
        @DecimalMin(value = "0.00", message = "Available amount cannot be negative")
        @Digits(integer = 17, fraction = 2, message = "Available amount must have at most 17 digits and 2 decimal places")
        BigDecimal availableAmount
) {
}
