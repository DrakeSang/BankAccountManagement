package com.emilyordanov.bankaccountmanagement.common.exception;

/**
 * Exception used when the user tries to create or update a resource
 * with a value that must be unique.
 * <p>
 * Examples:
 * - duplicate account name
 * - duplicate IBAN
 * <p>
 * GlobalExceptionHandler converts this exception to HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
