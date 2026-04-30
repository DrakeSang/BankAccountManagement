package com.emilyordanov.bankaccountmanagement.common.exception;

/**
 * Exception used when a requested resource does not exist.
 *
 * Example:
 * GET /api/accounts/999
 * but account with id 999 does not exist.
 *
 * GlobalExceptionHandler converts this exception to HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
