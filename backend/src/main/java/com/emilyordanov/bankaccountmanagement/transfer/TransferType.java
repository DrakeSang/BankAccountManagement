package com.emilyordanov.bankaccountmanagement.transfer;

/**
 * TransferType represents the direction of money movement
 * from the perspective of the account stored in account_id.
 * <p>
 * DEBIT:
 * - money leaves this account
 * <p>
 * CREDIT:
 * - money enters this account
 */
public enum TransferType {
    CREDIT,
    DEBIT
}
