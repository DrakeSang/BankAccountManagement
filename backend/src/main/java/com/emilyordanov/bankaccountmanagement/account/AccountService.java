package com.emilyordanov.bankaccountmanagement.account;

import com.emilyordanov.bankaccountmanagement.account.dto.AccountCreateRequest;
import com.emilyordanov.bankaccountmanagement.account.dto.AccountResponse;
import com.emilyordanov.bankaccountmanagement.account.dto.AccountUpdateRequest;
import com.emilyordanov.bankaccountmanagement.common.exception.BadRequestException;
import com.emilyordanov.bankaccountmanagement.common.exception.DuplicateResourceException;
import com.emilyordanov.bankaccountmanagement.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Service layer for account business logic.
 * <p>
 * The controller only receives HTTP requests and returns HTTP responses.
 * The service contains the actual business rules:
 * - create account
 * - update account
 * - freeze account
 * - unfreeze account
 * - validate uniqueness
 * - normalize input data
 */
@Service
public class AccountService {
    private static final int MIN_IBAN_LENGTH = 15;
    private static final int MAX_IBAN_LENGTH = 34;
    private static final int BULGARIAN_IBAN_LENGTH = 22;

    private static final Pattern BASIC_IBAN_PATTERN = Pattern.compile("^[A-Z]{2}\\d{2}[A-Z0-9]+$");

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Returns all accounts.
     *
     * @Transactional (readOnly = true) is used because this method only reads data.
     * It can help performance and makes the intention clear.
     */
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Returns one account by id.
     * <p>
     * If the account does not exist, we throw ResourceNotFoundException.
     * The GlobalExceptionHandler converts it to HTTP 404.
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        Account account = findAccountById(id);
        return toResponse(account);
    }

    /**
     * Creates a new bank account.
     * <p>
     * Rules:
     * - name must be unique
     * - IBAN must be unique
     * - initial amount cannot be negative
     * - new accounts are ACTIVE by default
     * <p>
     * Validation annotations in AccountCreateRequest handle basic validation.
     * This service handles business validation such as uniqueness.
     */
    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request) {
        String name = normalizeName(request.name());
        String iban = normalizeAndValidateIban(request.iban());

        validateUniqueName(name);
        validateUniqueIban(iban);

        Account account = new Account(
                name,
                iban,
                AccountStatus.ACTIVE,
                request.availableAmount()
        );

        /**
         * save() persists the new entity.
         *
         * Because this is a new entity, Hibernate will trigger @PrePersist,
         * which sets createdOn automatically.
         */
        Account savedAccount = accountRepository.save(account);

        return toResponse(savedAccount);
    }

    /**
     * Updates an existing account.
     * <p>
     * Rules:
     * - account must exist
     * - new name must not be used by another account
     * - new IBAN must not be used by another account
     * - availableAmount cannot be negative
     * <p>
     * Status is not updated here.
     * Status changes are handled by freeze/unfreeze endpoints.
     */
    @Transactional
    public AccountResponse updateAccount(Long id, AccountUpdateRequest request) {
        Account account = findAccountById(id);

        String name = normalizeName(request.name());
        String iban = normalizeAndValidateIban(request.iban());

        validateUniqueNameForUpdate(name, id);
        validateUniqueIbanForUpdate(iban, id);

        account.setName(name);
        account.setIban(iban);
        account.setAvailableAmount(request.availableAmount());

        /**
         * saveAndFlush() saves the entity and immediately flushes changes to the database.
         *
         * Why not only save()?
         *
         * @PreUpdate is executed when Hibernate flushes the update.
         * If we use only save(), the flush may happen later, usually at transaction commit.
         *
         * But we want modifiedOn to be updated before we convert the entity to response.
         *
         * saveAndFlush() forces Hibernate to execute the update now,
         * so @PreUpdate runs and modifiedOn is available in the returned response.
         */
        Account updatedAccount = accountRepository.saveAndFlush(account);

        return toResponse(updatedAccount);
    }

    /**
     * Freezes an account.
     * <p>
     * Frozen accounts should not be allowed to participate in transfers.
     * We will enforce that later in TransferService.
     */
    @Transactional
    public AccountResponse freezeAccount(Long id) {
        Account account = findAccountById(id);

        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new BadRequestException("Account is already frozen");
        }

        account.setStatus(AccountStatus.FROZEN);

        /**
         * saveAndFlush() is used here so @PreUpdate updates modifiedOn
         * before we return the response.
         */
        Account updatedAccount = accountRepository.saveAndFlush(account);

        return toResponse(updatedAccount);
    }

    /**
     * Unfreezes an account.
     * <p>
     * After this operation the account becomes ACTIVE again.
     */
    @Transactional
    public AccountResponse unfreezeAccount(Long id) {
        Account account = findAccountById(id);

        if (account.getStatus() == AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is already active");
        }

        account.setStatus(AccountStatus.ACTIVE);

        Account updatedAccount = accountRepository.saveAndFlush(account);

        return toResponse(updatedAccount);
    }

    /**
     * Helper method for loading an account or throwing a clear exception.
     * <p>
     * This avoids repeating the same findById().orElseThrow(...) logic
     * in multiple service methods.
     */
    private Account findAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account with id " + id + " was not found"));
    }

    /**
     * Validates name uniqueness during create.
     */
    private void validateUniqueName(String name) {
        if (accountRepository.existsByName(name)) {
            throw new DuplicateResourceException("Account name already exists");
        }
    }

    /**
     * Validates IBAN uniqueness during create.
     */
    private void validateUniqueIban(String iban) {
        if (accountRepository.existsByIban(iban)) {
            throw new DuplicateResourceException("IBAN already exists");
        }
    }

    /**
     * Validates name uniqueness during update.
     * <p>
     * We exclude the current account id from the check.
     * This allows the account to keep its own name.
     */
    private void validateUniqueNameForUpdate(String name, Long accountId) {
        if (accountRepository.existsByNameAndIdNot(name, accountId)) {
            throw new DuplicateResourceException("Account name already exists");
        }
    }

    /**
     * Validates IBAN uniqueness during update.
     * <p>
     * We exclude the current account id from the check.
     * This allows the account to keep its own IBAN.
     */
    private void validateUniqueIbanForUpdate(String iban, Long accountId) {
        if (accountRepository.existsByIbanAndIdNot(iban, accountId)) {
            throw new DuplicateResourceException("IBAN already exists");
        }
    }

    /**
     * Normalizes account name before saving.
     * <p>
     * Example:
     * "  Main Account  " becomes "Main Account".
     */
    private String normalizeName(String name) {
        return name.trim();
    }

    /**
     * Normalizes and validates IBAN before saving.
     * <p>
     * First we remove spaces and convert to uppercase.
     * Then we validate the normalized value.
     */
    private String normalizeAndValidateIban(String iban) {
        String normalizedIban = normalizeIban(iban);
        validateIbanFormat(normalizedIban);

        return normalizedIban;
    }

    /**
     * Normalizes IBAN before saving.
     * <p>
     * Example:
     * "bg18 rzbb 9155 0123 4567 89"
     * becomes:
     * "BG18RZBB91550123456789"
     * <p>
     * This helps avoid duplicate IBANs written with different spacing or casing.
     */
    private String normalizeIban(String iban) {
        return iban.replaceAll("\\s+", "").toUpperCase();
    }

    /**
     * Basic IBAN validation.
     * <p>
     * This is not a full IBAN checksum validation.
     * Full IBAN validation would include the official mod-97 checksum algorithm.
     * <p>
     * For this assignment, we validate:
     * - length between 15 and 34 characters
     * - starts with 2 country letters
     * - followed by 2 check digits
     * - contains only uppercase letters and digits after normalization
     * - if it starts with BG, it must be exactly 22 characters
     */
    private void validateIbanFormat(String iban) {
        if (iban.length() < MIN_IBAN_LENGTH || iban.length() > MAX_IBAN_LENGTH) {
            throw new BadRequestException("IBAN must be between 15 and 34 characters");
        }

        if (!BASIC_IBAN_PATTERN.matcher(iban).matches()) {
            throw new BadRequestException("IBAN format is invalid");
        }

        if (iban.startsWith("BG") && iban.length() != BULGARIAN_IBAN_LENGTH) {
            throw new BadRequestException("Bulgarian IBAN must be exactly 22 characters");
        }
    }

    /**
     * Maps Account entity to AccountResponse DTO.
     * <p>
     * This keeps mapping logic in one place.
     */
    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getIban(),
                account.getStatus(),
                account.getAvailableAmount(),
                account.getCreatedOn(),
                account.getModifiedOn()
        );
    }
}
