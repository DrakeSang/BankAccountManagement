package com.emilyordanov.bankaccountmanagement.account.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO used when creating a new account.
 * <p>
 * We use DTOs instead of exposing the entity directly through the API.
 * This gives us control over:
 * - what the client can send
 * - what fields are validated
 * - what fields are hidden from direct modification
 * <p>
 * For example:
 * The client does not send id, status, createdOn or modifiedOn.
 * The backend controls those fields.
 */
public record AccountCreateRequest(
        /**
         * Account name is required.
         *
         * @NotBlank checks:
         * - not null
         * - not empty
         * - not only whitespace
         */
        @NotBlank(message = "Account name is required")
        @Size(max = 100, message = "Account name must be at most 100 characters")
        String name,

        /**
         * IBAN is required.
         *
         * We validate maximum length here.
         * More advanced IBAN format validation can be added later if needed.
         */
        @NotBlank(message = "IBAN is required")
        @Size(min = 15, max = 34, message = "IBAN must be between 15 and 34 characters")
        String iban,

        /**
         * Initial available amount.
         *
         * @NotNull is needed because BigDecimal can be null.
         * @DecimalMin prevents negative starting balance.
         * @Digits keeps the value compatible with DECIMAL(19, 2).
         */
        @NotNull(message = "Available amount is required")
        @DecimalMin(value = "0.00", message = "Available amount cannot be negative")
        @Digits(integer = 17, fraction = 2, message = "Available amount must have at most 17 digits and 2 decimal places")
        BigDecimal availableAmount
) {
}
