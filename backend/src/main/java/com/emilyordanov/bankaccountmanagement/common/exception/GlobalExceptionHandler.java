package com.emilyordanov.bankaccountmanagement.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for the REST API.
 *
 * @RestControllerAdvice allows us to handle exceptions from all controllers
 * in one central place.
 * <p>
 * Without this class, Spring Boot would return default error responses.
 * With this class, we return our own consistent ErrorResponse format.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handles cases when a resource is not found.
     * <p>
     * Example:
     * Account with id 99 does not exist.
     * <p>
     * Returns HTTP 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException exception) {
        return ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getMessage()
        );
    }

    /**
     * Handles duplicate resource errors.
     * <p>
     * Example:
     * User tries to create account with already existing IBAN.
     * <p>
     * Returns HTTP 409 Conflict.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateResourceException(DuplicateResourceException exception) {
        return ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                exception.getMessage()
        );
    }

    /**
     * Handles business validation errors.
     * <p>
     * Example:
     * User tries to freeze an already frozen account.
     * <p>
     * Returns HTTP 400 Bad Request.
     */
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestException(BadRequestException exception) {
        return ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                exception.getMessage()
        );
    }

    /**
     * Handles validation errors from @Valid request DTOs.
     * <p>
     * MethodArgumentNotValidException is thrown when annotations such as:
     * - @NotBlank
     * - @NotNull
     * - @DecimalMin
     * - @Size
     * <p>
     * fail during request body validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> validationErrors = new LinkedHashMap<>();

        /**
         * We collect validation errors by field name.
         *
         * Example:
         * "name" -> "Account name is required"
         * "availableAmount" -> "Available amount cannot be negative"
         */
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ErrorResponse.ofValidationErrors(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed",
                validationErrors
        );
    }

    /**
     * Fallback handler for unexpected errors.
     * <p>
     * This prevents internal stack traces or technical details
     * from being returned directly to the client.
     * <p>
     * In a real production application, we would also log the exception.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneralException(Exception exception) {
        return ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Unexpected server error"
        );
    }
}
