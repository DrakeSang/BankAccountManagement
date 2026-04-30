package com.emilyordanov.bankaccountmanagement.account;

/**
 * Represents the current status of a bank account.
 * <p>
 * ACTIVE  - account can be edited and used for transfers.
 * FROZEN  - account is blocked from transfer operations.
 * <p>
 * We store this enum as STRING in the database through @Enumerated(EnumType.STRING),
 * so the database will contain values like "ACTIVE" and "FROZEN".
 */
public enum AccountStatus {
    ACTIVE,
    FROZEN
}
