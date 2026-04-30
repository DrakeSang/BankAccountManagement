package com.emilyordanov.bankaccountmanagement.account;

import com.emilyordanov.bankaccountmanagement.account.dto.AccountCreateRequest;
import com.emilyordanov.bankaccountmanagement.account.dto.AccountResponse;
import com.emilyordanov.bankaccountmanagement.account.dto.AccountUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for account endpoints.
 * <p>
 * Base URL:
 * /api/accounts
 * <p>
 * This controller is intentionally thin.
 * It only handles HTTP-related concerns:
 * - URL mappings
 * - request body
 * - path variables
 * - HTTP status codes
 * <p>
 * Business logic stays in AccountService.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * GET /api/accounts
     * <p>
     * Returns all created accounts.
     */
    @GetMapping
    public List<AccountResponse> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    /**
     * GET /api/accounts/{id}
     * <p>
     * Returns one account by id.
     */
    @GetMapping("/{id}")
    public AccountResponse getAccountById(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    /**
     * POST /api/accounts
     * <p>
     * Creates a new account.
     *
     * @Valid triggers validation annotations in AccountCreateRequest.
     * If validation fails, MethodArgumentNotValidException is thrown
     * and handled by GlobalExceptionHandler.
     * @ResponseStatus(HttpStatus.CREATED) returns HTTP 201 instead of default 200.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@Valid @RequestBody AccountCreateRequest request) {
        return accountService.createAccount(request);
    }

    /**
     * PUT /api/accounts/{id}
     * <p>
     * Updates an existing account.
     * <p>
     * We use PUT because the request updates the account's editable fields.
     */
    @PutMapping("/{id}")
    public AccountResponse updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountUpdateRequest request
    ) {
        return accountService.updateAccount(id, request);
    }

    /**
     * PATCH /api/accounts/{id}/freeze
     * <p>
     * Freezes an existing account.
     * <p>
     * We use PATCH because this is a partial state change,
     * not a full replacement of the account resource.
     */
    @PatchMapping("/{id}/freeze")
    public AccountResponse freezeAccount(@PathVariable Long id) {
        return accountService.freezeAccount(id);
    }

    /**
     * PATCH /api/accounts/{id}/unfreeze
     * <p>
     * Unfreezes an existing account.
     */
    @PatchMapping("/{id}/unfreeze")
    public AccountResponse unfreezeAccount(@PathVariable Long id) {
        return accountService.unfreezeAccount(id);
    }
}
