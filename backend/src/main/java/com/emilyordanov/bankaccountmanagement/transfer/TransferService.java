package com.emilyordanov.bankaccountmanagement.transfer;

import com.emilyordanov.bankaccountmanagement.account.Account;
import com.emilyordanov.bankaccountmanagement.account.AccountRepository;
import com.emilyordanov.bankaccountmanagement.account.AccountStatus;
import com.emilyordanov.bankaccountmanagement.common.exception.BadRequestException;
import com.emilyordanov.bankaccountmanagement.common.exception.ResourceNotFoundException;
import com.emilyordanov.bankaccountmanagement.transfer.dto.TransferCreateRequest;
import com.emilyordanov.bankaccountmanagement.transfer.dto.TransferOperationResponse;
import com.emilyordanov.bankaccountmanagement.transfer.dto.TransferResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service that contains the business logic for bank transfers.
 * <p>
 * Important business rule:
 * Creating one transfer operation must be atomic.
 * <p>
 * That means:
 * - source balance is decreased
 * - beneficiary balance is increased
 * - DEBIT transfer row is saved
 * - CREDIT transfer row is saved
 * <p>
 * Either all of these changes are committed,
 * or all of them are rolled back.
 */
@Service
public class TransferService {
    private final TransferRepository transferRepository;

    private final AccountRepository accountRepository;

    public TransferService(TransferRepository transferRepository, AccountRepository accountRepository) {
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Creates a bank transfer between two accounts.
     * <p>
     * Full flow:
     * <p>
     * 1. Validate that source and beneficiary are different accounts
     * 2. Load source account
     * 3. Load beneficiary account
     * 4. Validate both accounts are ACTIVE
     * 5. Validate source account has enough money
     * 6. Decrease source balance
     * 7. Increase beneficiary balance
     * 8. Generate one referenceId for the operation
     * 9. Create DEBIT row for source account
     * 10. Create CREDIT row for beneficiary account
     * 11. Save everything in one transaction
     */
    @Transactional
    public TransferOperationResponse createTransfer(TransferCreateRequest request) {
        validateDifferentAccounts(request.accountId(), request.beneficiaryAccountId());

        Account sourceAccount = getAccountOrThrow(request.accountId(), "Source account was not found");
        Account beneficiaryAccount = getAccountOrThrow(request.beneficiaryAccountId(), "Beneficiary account was not found");

        validateAccountIsActive(sourceAccount, "Source account is frozen");
        validateAccountIsActive(beneficiaryAccount, "Beneficiary account is frozen");

        BigDecimal amount = request.amount();

        validateSufficientFunds(sourceAccount, amount);

        decreaseSourceBalance(sourceAccount, amount);
        increaseBeneficiaryBalance(beneficiaryAccount, amount);

        String referenceId = UUID.randomUUID().toString();

        Transfer debitTransfer = createTransferEntry(
                referenceId,
                sourceAccount,
                beneficiaryAccount,
                TransferType.DEBIT,
                amount
        );

        Transfer creditTransfer = createTransferEntry(
                referenceId,
                beneficiaryAccount,
                sourceAccount,
                TransferType.CREDIT,
                amount
        );

        /*
         * The account entities are managed inside the current transaction.
         * Hibernate would detect balance changes automatically.
         *
         * Still, saving explicitly makes the flow easier to read:
         * first save updated balances, then save transfer rows.
         *
         * If any save fails, @Transactional rolls back everything.
         */
        accountRepository.save(sourceAccount);
        accountRepository.save(beneficiaryAccount);

        Transfer savedDebitTransfer = transferRepository.save(debitTransfer);
        Transfer savedCreditTransfer = transferRepository.save(creditTransfer);

        return new TransferOperationResponse(
                referenceId,
                TransferResponse.fromEntity(savedDebitTransfer),
                TransferResponse.fromEntity(savedCreditTransfer)
        );
    }

    /**
     * Returns one transfer row by id.
     */
    @Transactional(readOnly = true)
    public TransferResponse getTransferById(Long transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer with id " + transferId + " was not found"));

        return TransferResponse.fromEntity(transfer);
    }

    /**
     * Returns all transfers for a specific account.
     * <p>
     * First we check if the account exists.
     * <p>
     * If it exists but has no transfers, returning an empty list is correct.
     */
    @Transactional(readOnly = true)
    public List<TransferResponse> getTransfersByAccountId(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Account with id " + accountId + " was not found");
        }

        return transferRepository.findByAccount_IdOrderByCreatedOnDesc(accountId)
                .stream()
                .map(TransferResponse::fromEntity)
                .toList();
    }

    /**
     * Source and beneficiary account must be different.
     * <p>
     * We do not allow transfers from an account to itself.
     */
    private void validateDifferentAccounts(Long sourceAccountId, Long beneficiaryAccountId) {
        if (Objects.equals(sourceAccountId, beneficiaryAccountId)) {
            throw new BadRequestException("Source and beneficiary account must be different");
        }
    }

    /**
     * Loads account or throws a clear 404 error.
     */
    private Account getAccountOrThrow(Long accountId, String errorMessage) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(errorMessage));
    }

    /**
     * Frozen accounts are not allowed to participate in transfers.
     * <p>
     * Business decision:
     * - frozen account cannot send money
     * - frozen account cannot receive money
     */
    private void validateAccountIsActive(Account account, String errorMessage) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException(errorMessage);
        }
    }

    /**
     * Validates that source account has enough money.
     * <p>
     * BigDecimal should be compared with compareTo(), not equals().
     * <p>
     * Example:
     * new BigDecimal("100.0").equals(new BigDecimal("100.00")) is false
     * but compareTo() returns 0 because the numeric value is equal.
     */
    private void validateSufficientFunds(Account sourceAccount, BigDecimal amount) {
        if (sourceAccount.getAvailableAmount().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient funds");
        }
    }

    private void decreaseSourceBalance(Account sourceAccount, BigDecimal amount) {
        sourceAccount.setAvailableAmount(
                sourceAccount.getAvailableAmount().subtract(amount)
        );
    }

    private void increaseBeneficiaryBalance(Account beneficiaryAccount, BigDecimal amount) {
        beneficiaryAccount.setAvailableAmount(
                beneficiaryAccount.getAvailableAmount().add(amount)
        );
    }

    /**
     * Creates one transfer row.
     * <p>
     * account:
     * - the account for which this statement row is created
     * <p>
     * beneficiaryAccount:
     * - the other account involved in the operation
     * <p>
     * type:
     * - DEBIT or CREDIT from the perspective of account
     */
    private Transfer createTransferEntry(String referenceId,
                                         Account account,
                                         Account beneficiaryAccount,
                                         TransferType type,
                                         BigDecimal amount) {
        Transfer transfer = new Transfer();
        transfer.setReferenceId(referenceId);
        transfer.setAccount(account);
        transfer.setBeneficiaryAccount(beneficiaryAccount);
        transfer.setType(type);
        transfer.setAmount(amount);

        return transfer;
    }
}
