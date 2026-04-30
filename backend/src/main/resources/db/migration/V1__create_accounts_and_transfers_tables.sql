-- Flyway migration file.
-- Flyway executes this file automatically when the application starts
-- and the target database does not yet have this migration in flyway_schema_history.
--
-- Naming convention:
-- V1__create_accounts_and_transfers_tables.sql
--
-- V1 means this is migration version 1.
-- The double underscore "__" is required by Flyway.
-- The rest of the name describes what the migration does.
--
-- This migration creates the initial database schema for:
-- 1. accounts
-- 2. transfers
--
-- The application uses Flyway to create the schema.
-- Hibernate uses ddl-auto=validate, so it only validates that the Java entities match these tables.

CREATE TABLE accounts
(
    -- Primary key of the account.
    -- BIGINT is used because IDs can grow over time.
    -- AUTO_INCREMENT means MySQL will generate the value automatically.
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Human-readable account name.
    -- Example: "Main Account", "Savings Account".
    -- VARCHAR(100) is enough for a simple account name.
    -- NOT NULL means every account must have a name.
    name             VARCHAR(100)   NOT NULL,

    -- IBAN of the bank account.
    -- IBAN maximum length is up to 34 characters.
    -- VARCHAR(34) is enough for valid IBAN values.
    -- NOT NULL means every account must have an IBAN.
    iban             VARCHAR(34)    NOT NULL,

    -- Account status.
    -- Stored as text instead of number because the Java enum will be mapped with EnumType.STRING.
    -- Expected values in the application: ACTIVE, FROZEN.
    -- VARCHAR(20) is enough for these enum values.
    status           VARCHAR(20)    NOT NULL,

    -- Current available balance of the account.
    -- DECIMAL is used instead of DOUBLE because money should not be stored with floating-point precision.
    -- DECIMAL(19, 2) allows large amounts with 2 digits after the decimal point.
    available_amount DECIMAL(19, 2) NOT NULL,

    -- Date and time when the account was created.
    -- DATETIME(6) stores date/time with microsecond precision.
    -- This maps well to Java Instant / LocalDateTime.
    created_on       DATETIME(6)    NOT NULL,

    -- Date and time when the account was last modified.
    -- NULL is allowed because a newly created account may not have been modified yet.
    modified_on      DATETIME(6)    NULL,

    -- Account name must be unique according to the assignment.
    CONSTRAINT uk_accounts_name UNIQUE (name),

    -- IBAN must be unique according to the assignment.
    CONSTRAINT uk_accounts_iban UNIQUE (iban),

    -- Available amount should never be negative.
    -- The service layer will also validate this, but the database constraint protects the data as well.
    CONSTRAINT chk_accounts_available_amount CHECK (available_amount >= 0)
);

CREATE TABLE transfers
(
    -- Primary key of the transfer record.
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- The account for which this transfer record is created.
    --
    -- Important:
    -- We plan to create two records for one logical bank transfer:
    -- 1. DEBIT record for the source account
    -- 2. CREDIT record for the beneficiary account
    --
    -- For the DEBIT record:
    -- account_id = source account
    --
    -- For the CREDIT record:
    -- account_id = beneficiary account
    account_id             BIGINT         NOT NULL,

    -- The opposite account involved in the transfer.
    --
    -- For the DEBIT record:
    -- beneficiary_account_id = target account
    --
    -- For the CREDIT record:
    -- beneficiary_account_id = source account
    --
    -- This makes it possible to see who the money was sent to or received from.
    beneficiary_account_id BIGINT         NOT NULL,

    -- Transfer type.
    -- Stored as text because the Java enum will be mapped with EnumType.STRING.
    -- Expected values in the application: CREDIT, DEBIT.
    type                   VARCHAR(20)    NOT NULL,

    -- Transfer amount.
    -- DECIMAL is used for money.
    -- The amount must be positive.
    amount                 DECIMAL(19, 2) NOT NULL,

    -- Reference ID connects the two records of one logical transfer.
    --
    -- Example:
    -- User transfers 100 from Account A to Account B.
    --
    -- The application creates:
    -- 1. DEBIT record for Account A
    -- 2. CREDIT record for Account B
    --
    -- Both records will have the same reference_id.
    --
    -- This helps us:
    -- - group the debit and credit side of the same operation
    -- - trace the full transfer
    -- - return or debug both sides of the operation
    -- - keep account transfer history simple with WHERE account_id = ?
    --
    -- CHAR(36) is used because UUID string representation has 36 characters.
    reference_id           CHAR(36)       NOT NULL,

    -- Date and time when the transfer record was created.
    created_on             DATETIME(6)    NOT NULL,

    -- Date and time when the transfer record was last modified.
    -- Usually transfers will not be modified, but the column is included because it is required by the assignment.
    modified_on            DATETIME(6)    NULL,

    -- Foreign key from transfers.account_id to accounts.id.
    -- This guarantees that every transfer record belongs to an existing account.
    CONSTRAINT fk_transfers_account
        FOREIGN KEY (account_id) REFERENCES accounts (id),

    -- Foreign key from transfers.beneficiary_account_id to accounts.id.
    -- This guarantees that the other account in the transfer also exists.
    CONSTRAINT fk_transfers_beneficiary_account
        FOREIGN KEY (beneficiary_account_id) REFERENCES accounts (id),

    -- Transfer amount must always be greater than zero.
    CONSTRAINT chk_transfers_amount CHECK (amount > 0),

    -- Index for faster lookup of all transfers for a specific account.
    -- This supports the user story:
    -- "As a user, you should be able to see a list of all transfers for certain account."
    INDEX idx_transfers_account_id (account_id),

    -- Index for faster lookup by reference_id.
    -- Useful if we want to find both DEBIT and CREDIT records of the same logical transfer.
    INDEX idx_transfers_reference_id (reference_id)
);