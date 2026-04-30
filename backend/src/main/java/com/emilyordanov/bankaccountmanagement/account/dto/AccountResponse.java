package com.emilyordanov.bankaccountmanagement.account.dto;

import com.emilyordanov.bankaccountmanagement.account.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO returned by the API.
 *
 * This is what the frontend/client receives.
 *
 * We return all important account information:
 * - id
 * - name
 * - IBAN
 * - status
 * - balance
 * - timestamps
 *
 * Returning DTOs instead of entities keeps the API contract cleaner
 * and avoids exposing internal JPA details.
 */
public record AccountResponse(
        Long id,
        String name,
        String iban,
        AccountStatus status,
        BigDecimal availableAmount,
        Instant createdOn,
        Instant modifiedOn
) {
}
