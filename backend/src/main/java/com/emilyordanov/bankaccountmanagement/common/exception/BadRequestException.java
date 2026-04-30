package com.emilyordanov.bankaccountmanagement.common.exception;

/**
 * Exception used when the request is syntactically correct,
 * but the business operation is not allowed.
 * <p>
 * Examples:
 * - trying to freeze an already frozen account
 * - trying to unfreeze an already active account
 * - later: trying to transfer from a frozen account
 * - later: insufficient funds
 * <p>
 * GlobalExceptionHandler converts this exception to HTTP 400.
 */

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
