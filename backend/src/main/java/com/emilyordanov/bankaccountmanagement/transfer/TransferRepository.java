package com.emilyordanov.bankaccountmanagement.transfer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Transfer entities.
 */
public interface TransferRepository extends JpaRepository<Transfer, Long> {
    /**
     * Returns all transfer rows for a specific account.
     * <p>
     * Because every transfer row belongs to one account_id,
     * this gives us the account statement/history for that account.
     * <p>
     * Ordered by newest first.
     */
    List<Transfer> findByAccount_IdOrderByCreatedOnDesc(Long accountId);

    /**
     * Optional helper if later we want to trace both rows
     * from the same business transfer operation.
     */
    List<Transfer> findByReferenceIdOrderByCreatedOnAsc(String referenceId);
}
