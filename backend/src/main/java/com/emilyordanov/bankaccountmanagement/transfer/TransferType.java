package com.emilyordanov.bankaccountmanagement.transfer;

/**
 * TransferType represents the direction of money movement
 * from the perspective of the account stored in transfers.account_id.
 * <p>
 * This is very important:
 * <p>
 * The type is NOT global for the whole transfer operation.
 * The type is always interpreted from the point of view of the current account row.
 * <p>
 * Example business operation:
 * <p>
 * Main Account sends 100.00 to Savings Account.
 * <p>
 * In the database we create TWO transfer rows with the same referenceId:
 * <p>
 * Row 1 - from the perspective of Main Account:
 * <p>
 * account_id = Main Account
 * beneficiary_account_id = Savings Account
 * type = DEBIT
 * amount = 100.00
 * <p>
 * Meaning:
 * Main Account loses money.
 * Money leaves Main Account and goes to Savings Account.
 * <p>
 * Row 2 - from the perspective of Savings Account:
 * <p>
 * account_id = Savings Account
 * beneficiary_account_id = Main Account
 * type = CREDIT
 * amount = 100.00
 * <p>
 * Meaning:
 * Savings Account receives money.
 * Money enters Savings Account from Main Account.
 * <p>
 * Because of this:
 * <p>
 * DEBIT means:
 * - money leaves the account stored in account_id
 * - in the UI we can display this as "To {counterparty account}"
 * <p>
 * CREDIT means:
 * - money enters the account stored in account_id
 * - in the UI we can display this as "From {counterparty account}"
 * <p>
 * Note about beneficiary_account_id:
 * <p>
 * In the DEBIT row, beneficiary_account_id is really the beneficiary/receiver.
 * In the CREDIT row, beneficiary_account_id represents the source/sender.
 * <p>
 * That is why, when displaying transfer history, it is clearer to think of
 * beneficiary_account_id as the "counterparty account", meaning the other account
 * involved in the transfer.
 */
public enum TransferType {
    /**
     * Money enters the account stored in account_id.
     * <p>
     * Example:
     * account_id = Savings Account
     * beneficiary_account_id = Main Account
     * type = CREDIT
     * <p>
     * Meaning:
     * Savings Account received money from Main Account.
     */
    CREDIT,

    /**
     * Money leaves the account stored in account_id.
     * <p>
     * Example:
     * account_id = Main Account
     * beneficiary_account_id = Savings Account
     * type = DEBIT
     * <p>
     * Meaning:
     * Main Account sent money to Savings Account.
     */
    DEBIT
}
