package com.emilyordanov.bankaccountmanagement.transfer;

import com.emilyordanov.bankaccountmanagement.account.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transfer entity represents one account statement entry.
 * <p>
 * Important:
 * One business transfer between two accounts creates TWO rows:
 * <p>
 * 1. DEBIT row for the source account
 * 2. CREDIT row for the beneficiary account
 * <p>
 * Both rows share the same referenceId.
 * <p>
 * Example:
 * Account 1 sends 100.00 to Account 2
 * <p>
 * Row 1:
 * account_id = 1
 * beneficiary_account_id = 2
 * type = DEBIT
 * amount = 100.00
 * <p>
 * Row 2:
 * account_id = 2
 * beneficiary_account_id = 1
 * type = CREDIT
 * amount = 100.00
 * <p>
 * reference_id is the same for both rows.
 */

@Entity
@Table(
        name = "transfers",
        indexes = {
                @Index(name = "idx_transfers_account_id", columnList = "account_id"),
                @Index(name = "idx_transfers_beneficiary_account_id", columnList = "beneficiary_account_id"),
                @Index(name = "idx_transfers_reference_id", columnList = "reference_id")
        }
)
@Getter
@Setter
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Common id for the two transfer rows that belong to one business operation.
     * <p>
     * It is not unique, because one transfer operation creates:
     * - one DEBIT row
     * - one CREDIT row
     * <p>
     * Both rows must have the same referenceId.
     */
    @Column(name = "reference_id", nullable = false, length = 36)
    private String referenceId;

    /**
     * The account for which this transfer row is created.
     * <p>
     * For DEBIT row:
     * - this is the source account
     * <p>
     * For CREDIT row:
     * - this is the beneficiary account
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /**
     * The other account involved in the transfer.
     * <p>
     * For DEBIT row:
     * - this is the beneficiary account
     * <p>
     * For CREDIT row:
     * - this is the source account
     * <p>
     * In other words, this column represents the counterparty account.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beneficiary_account_id", nullable = false)
    private Account beneficiaryAccount;

    /**
     * Direction of the transfer from the perspective of account_id.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferType type;

    /**
     * Transfer amount.
     * <p>
     * BigDecimal is used for money values because double/float can introduce
     * precision problems.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Timestamp when the transfer row was created.
     */
    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn;

    /**
     * Timestamp when the transfer row was last modified.
     * <p>
     * Transfers normally should not be modified, but the column exists because
     * it is required by the assignment and keeps the table consistent with accounts.
     */
    @Column(name = "modified_on", nullable = false)
    private LocalDateTime modifiedOn;

    /**
     * Automatically sets timestamps before the entity is inserted.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdOn = now;
        this.modifiedOn = now;
    }

    /**
     * Automatically updates modifiedOn before the entity is updated.
     */
    @PreUpdate
    protected void onUpdate() {
        this.modifiedOn = LocalDateTime.now();
    }
}
