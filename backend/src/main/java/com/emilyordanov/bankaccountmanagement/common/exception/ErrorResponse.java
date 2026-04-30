package com.emilyordanov.bankaccountmanagement.common.exception;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error response returned by the API.
 * <p>
 * Instead of returning random error formats,
 * we return a consistent structure for all handled exceptions.
 * <p>
 * validationErrors is used only for field validation errors.
 * For normal business errors it is null.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> validationErrors
) {
    /**
     * Factory method for normal errors.
     * <p>
     * Example:
     * {
     * "timestamp": "...",
     * "status": 404,
     * "error": "Not Found",
     * "message": "Account with id 99 was not found",
     * "validationErrors": null
     * }
     */
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(
                Instant.now(),
                status,
                error,
                message,
                null
        );
    }

    /**
     * Factory method for validation errors.
     * <p>
     * Example:
     * {
     * "timestamp": "...",
     * "status": 400,
     * "error": "Bad Request",
     * "message": "Validation failed",
     * "validationErrors": {
     * "name": "Account name is required",
     * "availableAmount": "Available amount cannot be negative"
     * }
     * }
     */
    public static ErrorResponse ofValidationErrors(
            int status,
            String error,
            String message,
            Map<String, String> validationErrors
    ) {
        return new ErrorResponse(
                Instant.now(),
                status,
                error,
                message,
                validationErrors
        );
    }
}
