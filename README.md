# Bank Account Management System

A simple full-stack application for managing bank accounts and bank transfers.

This project was developed as a technical assignment for a Java / Java + React Developer position.  
The main goal is to demonstrate clean backend structure, database migrations, validation, transactional transfer logic, and a simple React frontend that consumes the backend API.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Tech Stack](#tech-stack)
- [Requirements](#requirements)
- [Project Structure](#project-structure)
- [Backend Architecture](#backend-architecture)
- [Database Design](#database-design)
- [Flyway and Hibernate Decision](#flyway-and-hibernate-decision)
- [Account Business Rules](#account-business-rules)
- [IBAN Validation](#iban-validation)
- [Transfer Business Logic](#transfer-business-logic)
- [Transfer Validation Rules](#transfer-validation-rules)
- [API Endpoints](#api-endpoints)
- [Error Handling and Validation Responses](#error-handling-and-validation-responses)
- [Frontend Overview](#frontend-overview)
- [Frontend Structure](#frontend-structure)
- [React Concepts Used](#react-concepts-used)
- [Frontend UX Decisions](#frontend-ux-decisions)
- [How to Run the Project](#how-to-run-the-project)
- [Docker Database Inspection Commands](#docker-database-inspection-commands)
- [Manual Test Scenarios](#manual-test-scenarios)
- [Account Transfer History vs Transfer Operation History vs Audit Log](#account-transfer-history-vs-transfer-operation-history-vs-audit-log)
- [Known Limitations](#known-limitations)
- [Future Improvements](#future-improvements)
- [Final Notes](#final-notes)

---

## Project Overview

The application provides basic management of bank accounts and bank transfers.

Implemented user stories:

| Feature | Status |
|---|---|
| List created accounts | Implemented |
| Create new account | Implemented |
| Enforce unique account name | Implemented |
| Enforce unique IBAN | Implemented |
| Edit existing account | Implemented |
| Freeze account | Implemented |
| Unfreeze account | Implemented |
| List transfers for selected account | Implemented |
| Create transfer between two accounts | Implemented |

The application contains:

- Java Spring Boot backend
- React frontend
- MySQL database
- Flyway database migrations
- Docker Compose setup for the database

---

## Tech Stack

### Backend

| Technology | Purpose |
|---|---|
| Java 21 | Main backend language |
| Spring Boot | Backend application framework |
| Spring Web | REST API |
| Spring Data JPA | Database access |
| Hibernate | JPA implementation |
| Bean Validation | Request validation |
| Flyway | Database migrations |
| MySQL Driver | MySQL database connection |
| Maven Wrapper | Build and run project without installing Maven globally |

### Frontend

| Technology | Purpose |
|---|---|
| React | Frontend UI |
| Vite | React project setup and development server |
| Bootstrap | Simple styling |
| Fetch API | Communication with backend |

### Database / Infrastructure

| Technology | Purpose |
|---|---|
| MySQL | Relational database |
| Docker Compose | Runs MySQL database in a container |

> Docker Compose is used for the database only.  
> The backend and frontend are started locally.

---

## Requirements

To run the project locally, install:

| Tool | Required |
|---|---|
| Java 21 | Yes |
| Node.js + npm | Yes |
| Docker Desktop | Yes |
| Git | Yes |
| Maven | No, Maven Wrapper is included |

Maven is not required globally because the project contains Maven Wrapper files:

```bash
mvnw
mvnw.cmd
```

---

## Project Structure

The project is split into backend and frontend folders:

```text
bank-account-management/
│
├── backend/
│   ├── src/main/java/...
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-local.yml
│   │   ├── application-docker.yml
│   │   └── db/migration/
│   │       └── V1__create_accounts_and_transfers_tables.sql
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── api/
│   │   │   ├── accountApi.js
│   │   │   └── transferApi.js
│   │   ├── components/
│   │   │   ├── accounts/
│   │   │   │   ├── AccountForm.jsx
│   │   │   │   └── AccountTable.jsx
│   │   │   ├── transfers/
│   │   │   │   ├── TransferSection.jsx
│   │   │   │   ├── TransferForm.jsx
│   │   │   │   └── TransferHistory.jsx
│   │   │   └── common/
│   │   │       └── AlertMessage.jsx
│   │   ├── App.jsx
│   │   ├── App.css
│   │   └── main.jsx
│   ├── package.json
│   └── vite.config.js
│
├── docker-compose.yml
└── README.md
```

> If your local folder structure is slightly different, the same responsibilities still apply.

---

## Backend Architecture

The backend follows a simple layered architecture.

| Layer | Responsibility |
|---|---|
| Controller | Exposes REST endpoints |
| Service | Contains business logic |
| Repository | Communicates with the database |
| Entity | Maps Java objects to database tables |
| DTO | Defines request and response models |
| Exception Handling | Converts exceptions into structured API errors |

### Why DTOs are used

DTOs are used instead of exposing entities directly.

Benefits:

- request and response models are separated from database entities
- validation is applied on request DTOs
- API responses can be shaped for frontend needs
- internal entity structure can change without breaking the API
- entities are not directly exposed to external clients

---

## Database Design

The database contains two main tables:

```text
accounts
transfers
```

---

### Accounts Table

| Column | Type | Description |
|---|---|---|
| `id` | `BIGINT` | Primary key |
| `name` | `VARCHAR(100)` | Human-readable account name |
| `iban` | `VARCHAR(34)` | Account IBAN |
| `status` | `VARCHAR(20)` | `ACTIVE` or `FROZEN` |
| `available_amount` | `DECIMAL(19,2)` | Current account balance |
| `created_on` | `DATETIME(6)` | Creation timestamp |
| `modified_on` | `DATETIME(6)` | Last modification timestamp |

### Account Constraints

| Constraint | Purpose |
|---|---|
| Primary key on `id` | Unique account identifier |
| Unique constraint on `name` | Account names must be unique |
| Unique constraint on `iban` | IBAN values must be unique |
| Check constraint on `available_amount >= 0` | Balance cannot be negative |

---

### Transfers Table

| Column | Type | Description |
|---|---|---|
| `id` | `BIGINT` | Primary key |
| `reference_id` | `VARCHAR(36)` | Connects debit and credit rows of one logical transfer |
| `account_id` | `BIGINT` | Account for which this transfer row is created |
| `beneficiary_account_id` | `BIGINT` | Other account involved in the transfer |
| `type` | `VARCHAR(20)` | `CREDIT` or `DEBIT` |
| `amount` | `DECIMAL(19,2)` | Transfer amount |
| `created_on` | `DATETIME(6)` | Creation timestamp |
| `modified_on` | `DATETIME(6)` | Last modification timestamp |

### Transfer Constraints

| Constraint | Purpose |
|---|---|
| Primary key on `id` | Unique transfer row identifier |
| Foreign key `account_id -> accounts.id` | Transfer row must belong to an existing account |
| Foreign key `beneficiary_account_id -> accounts.id` | Counterparty account must exist |
| Check constraint on `amount > 0` | Transfer amount must be positive |

### Transfer Indexes

| Index | Purpose |
|---|---|
| `idx_transfers_account_id` | Faster lookup of transfers for a selected account |
| `idx_transfers_beneficiary_account_id` | Faster lookup by counterparty account |
| `idx_transfers_reference_id` | Faster lookup of both rows of one logical transfer |

---

## Flyway and Hibernate Decision

Flyway is used as the source of truth for database schema creation.

Hibernate is configured with:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

This means:

| Tool | Responsibility |
|---|---|
| Flyway | Creates and updates database schema using SQL migrations |
| Hibernate | Validates that entities match the existing schema |

Hibernate does not create or update the tables automatically.

### Why Flyway was used

Flyway was chosen because it provides:

- versioned database schema changes
- predictable table creation
- easier review of SQL structure
- better production-like workflow
- protection from accidental schema changes generated by Hibernate

---

## Account Business Rules

Implemented account rules:

| Rule | Description |
|---|---|
| Name is required | Account must have a name |
| Name must be unique | Duplicate account names are not allowed |
| IBAN is required | Account must have an IBAN |
| IBAN must be unique | Duplicate IBAN values are not allowed |
| Available amount is required | Account must have a balance |
| Available amount cannot be negative | Zero is allowed, negative values are not |
| Account can be `ACTIVE` or `FROZEN` | Status controls transfer participation |

### Create vs Update uniqueness checks

During account creation, the backend uses:

```java
existsByName(...)
existsByIban(...)
```

This is enough because a new account must not duplicate any existing account.

During account update, the backend uses:

```java
existsByNameAndIdNot(...)
existsByIbanAndIdNot(...)
```

This is needed because the current account must be excluded from the duplicate check.

Example:

```text
Account 1 already has name "Main Account".

Updating Account 1 while keeping the same name should be allowed.

But updating Account 2 to "Main Account" should be rejected.
```

That is why:

```java
existsByNameAndIdNot("Main Account", 1)
```

means:

```text
Is there another account with this name, but with id different from 1?
```

---

## IBAN Validation

IBAN validation is split into two levels.

### DTO validation

DTO validation checks basic field rules:

| Validation | Purpose |
|---|---|
| `@NotBlank` | IBAN is required |
| `@Size(min = 15, max = 34)` | IBAN must have a valid general length range |

### Service validation

The service layer performs additional business validation after normalization.

Normalization means:

```text
- remove spaces
- convert to uppercase
```

Example:

```text
Input:  bg18 rzbb 9155 0123 4567 89
Stored: BG18RZBB91550123456789
```

The service validates:

| Rule | Description |
|---|---|
| Basic IBAN format | Starts with 2 letters, then 2 digits |
| Only letters and digits | No invalid symbols |
| BG IBAN length | Bulgarian IBAN must be exactly 22 characters |

This is not a full IBAN checksum validation.  
A full mod-97 checksum validation can be added in a future version.

---

## Transfer Business Logic

A single logical transfer between two accounts creates two transfer rows.

This design makes account history easier to display because every account has its own transfer entry from its own perspective.

### Example

Main Account sends `100.00` to Savings Account.

The system creates two rows:

| Account Perspective | `account_id` | `beneficiary_account_id` | Type | Amount | Meaning |
|---|---|---|---|---:|---|
| Main Account | Main Account | Savings Account | `DEBIT` | 100.00 | Money leaves Main Account |
| Savings Account | Savings Account | Main Account | `CREDIT` | 100.00 | Money enters Savings Account |

Both rows share the same:

```text
reference_id
```

because they belong to the same logical transfer operation.

---

### DEBIT and CREDIT Meaning

`DEBIT` and `CREDIT` are interpreted from the perspective of `account_id`.

| Type | Meaning |
|---|---|
| `DEBIT` | Money leaves the account stored in `account_id` |
| `CREDIT` | Money enters the account stored in `account_id` |

Example:

```text
Account A sends 100.00 to Account B.
```

Rows:

| `account_id` | `beneficiary_account_id` | `type` | `amount` | `reference_id` |
|---|---|---|---:|---|
| Account A | Account B | `DEBIT` | 100.00 | X |
| Account B | Account A | `CREDIT` | 100.00 | X |

When viewing Account A transfer history:

```text
DEBIT 100.00 To Account B
```

When viewing Account B transfer history:

```text
CREDIT 100.00 From Account A
```

---

### Why `reference_id` exists

`reference_id` connects the two transfer rows of one logical operation.

Without `reference_id`, it would be difficult to reliably identify that a DEBIT row and CREDIT row belong to the same business operation.

It is useful for:

- tracing both sides of a transfer
- debugging transfer operations
- building a future transfer operation detail view
- grouping transfer rows into one logical operation

---

## Transfer Validation Rules

Transfer creation validates both DTO-level and business-level rules.

### DTO Validation

| Rule | Description |
|---|---|
| Source account id is required | `accountId` cannot be null |
| Beneficiary account id is required | `beneficiaryAccountId` cannot be null |
| Amount is required | `amount` cannot be null |
| Amount must be greater than zero | Transfer with `0.00` is not allowed |
| Amount must have at most 2 decimal places | Money precision is limited |

### Business Validation

| Rule | Description |
|---|---|
| Source account must exist | Cannot transfer from missing account |
| Beneficiary account must exist | Cannot transfer to missing account |
| Source and beneficiary must be different | Cannot transfer to the same account |
| Source account must be `ACTIVE` | Frozen account cannot send money |
| Beneficiary account must be `ACTIVE` | Frozen account cannot receive money |
| Source must have enough funds | Balance cannot become negative |

### Transactional behavior

Transfer creation is transactional.

This means the following actions succeed together or fail together:

```text
1. Decrease source account balance
2. Increase beneficiary account balance
3. Save DEBIT transfer row
4. Save CREDIT transfer row
```

If any step fails, the whole operation is rolled back.

This prevents inconsistent states such as:

```text
source account decreased,
but beneficiary account not increased
```

---

## API Endpoints

### Account Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/accounts` | List all accounts |
| `GET` | `/api/accounts/{id}` | Get account by id |
| `POST` | `/api/accounts` | Create account |
| `PUT` | `/api/accounts/{id}` | Update account |
| `PATCH` | `/api/accounts/{id}/freeze` | Freeze account |
| `PATCH` | `/api/accounts/{id}/unfreeze` | Unfreeze account |

### Transfer Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/transfers` | Create transfer between two accounts |
| `GET` | `/api/transfers/{id}` | Get transfer row by id |
| `GET` | `/api/accounts/{accountId}/transfers` | List transfers for selected account |

---

### Create Account Request Example

```json
{
  "name": "Main Account",
  "iban": "BG18RZBB91550123456789",
  "availableAmount": 1000.00
}
```

---

### Create Transfer Request Example

```json
{
  "accountId": 1,
  "beneficiaryAccountId": 2,
  "amount": 100.00
}
```

---

### Create Transfer Response Example

```json
{
  "referenceId": "8f0c4d4e-9d4b-4d6d-9f40-2cc6b7f4e111",
  "debitTransfer": {
    "id": 1,
    "referenceId": "8f0c4d4e-9d4b-4d6d-9f40-2cc6b7f4e111",
    "accountId": 1,
    "accountName": "Main Account",
    "counterpartyAccountId": 2,
    "counterpartyAccountName": "Savings Account",
    "type": "DEBIT",
    "amount": 100.00
  },
  "creditTransfer": {
    "id": 2,
    "referenceId": "8f0c4d4e-9d4b-4d6d-9f40-2cc6b7f4e111",
    "accountId": 2,
    "accountName": "Savings Account",
    "counterpartyAccountId": 1,
    "counterpartyAccountName": "Main Account",
    "type": "CREDIT",
    "amount": 100.00
  }
}
```

---

## Error Handling and Validation Responses

The backend uses centralized exception handling through a global exception handler.

There are two main categories of errors:

| Error Type | Example |
|---|---|
| Validation errors | Missing name, invalid amount, invalid IBAN |
| Business errors | Duplicate IBAN, insufficient funds, frozen account |

---

### Validation Error Example

```json
{
  "timestamp": "2026-05-01T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "validationErrors": {
    "name": "Account name is required",
    "iban": "IBAN must be between 15 and 34 characters",
    "availableAmount": "Available amount is required"
  }
}
```

Validation errors can contain multiple field errors at the same time.

---

### Business Error Example

```json
{
  "timestamp": "2026-05-01T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient funds"
}
```

Business errors usually return one clear error message because the service stops at the first failed business rule.

---

## Frontend Overview

The frontend is intentionally simple and focused on the required user stories.

Implemented frontend features:

| Feature | Description |
|---|---|
| Account table | Shows all accounts |
| Account form | Used for both create and edit |
| Freeze / Unfreeze buttons | Shown depending on account status |
| Transfer form | Creates transfer between two active accounts |
| Transfer history | Shows transfers for selected account |
| Alerts | Shows success and error messages |
| Backend validation display | Shows validation errors from backend response |

The frontend uses React state and props to manage UI behavior.

---

## Frontend Structure

### API Files

| File | Responsibility |
|---|---|
| `accountApi.js` | Contains all account-related fetch calls |
| `transferApi.js` | Contains all transfer-related fetch calls |

Keeping API calls in separate files prevents UI components from being filled with endpoint and fetch logic.

---

### Account Components

| File | Responsibility |
|---|---|
| `AccountForm.jsx` | Create and update account form |
| `AccountTable.jsx` | Displays accounts and account actions |

`AccountForm` is reused for both create and edit mode.

The mode is controlled by:

```text
selectedAccount === null -> create mode
selectedAccount !== null -> edit mode
```

---

### Transfer Components

| File | Responsibility |
|---|---|
| `TransferSection.jsx` | Parent component for transfer functionality |
| `TransferForm.jsx` | Form for creating a new transfer |
| `TransferHistory.jsx` | Shows transfer history for selected account |

`TransferSection` keeps transfer-specific state separate from the main `App.jsx`.

---

### Common Components

| File | Responsibility |
|---|---|
| `AlertMessage.jsx` | Displays success and error alerts |

Success alerts disappear automatically after a few seconds.  
Error alerts stay visible until the user closes them manually.

Reason:

```text
Success messages are usually short confirmations.
Error messages may contain validation details that the user needs to read.
```

---

## React Concepts Used

### `useState`

Used to store data that changes over time.

Examples:

| State | Purpose |
|---|---|
| `accounts` | Stores account list from backend |
| `selectedAccount` | Controls create/edit mode |
| `formData` | Stores form input values |
| `loading` | Disables buttons during requests |
| `error` | Stores backend or frontend error |
| `successMessage` | Stores success confirmation |
| `selectedAccountId` | Selected account for transfer history |
| `transfers` | Transfer history rows |

---

### `useEffect`

Used for side effects.

Examples:

```text
When the page opens, load accounts from the backend.
When selectedAccount changes, fill or reset the account form.
```

---

### Controlled Inputs

Inputs are controlled by React state.

Example:

```text
Input value comes from formData.
When user types, handleInputChange updates formData.
```

This makes the form predictable and easy to reset after successful operations.

---

### Props

Props are used to pass data and functions from parent components to child components.

Example:

```text
App.jsx passes accounts and handlers to AccountTable.
TransferSection passes transfer handlers to TransferForm.
```

---

### Conditional Rendering

Used to show different UI depending on state.

Examples:

| Condition | UI Behavior |
|---|---|
| Account is `ACTIVE` | Show Freeze button |
| Account is `FROZEN` | Show Unfreeze button |
| `selectedAccount` exists | Show Update Account button |
| `selectedAccount` is null | Show Create Account button |
| No transfer history account selected | Show info message |
| Transfer list is empty | Show empty state message |

---

### Rendering Lists with `map`

Used to render:

```text
- account table rows
- transfer table rows
- dropdown options
- validation errors
```

---

### Fetch API

The frontend uses the browser Fetch API to call backend endpoints.

API files handle:

```text
- request creation
- response parsing
- backend error extraction
```

---

## Frontend UX Decisions

| UX Decision | Reason |
|---|---|
| Freeze button only appears for `ACTIVE` accounts | Prevents invalid user action |
| Unfreeze button only appears for `FROZEN` accounts | Prevents invalid user action |
| Transfer form shows only `ACTIVE` accounts | Frozen accounts cannot participate in transfers |
| Beneficiary dropdown excludes selected source account | Prevents transfer to the same account |
| Transfer history shows all accounts | Frozen accounts may still have old transfers |
| Create account form clears after successful create | Better user experience |
| Form does not clear after validation error | User can fix the entered data |
| Success alerts auto-hide | Success messages are short |
| Error alerts stay visible | User may need time to read validation errors |

Important note:

```text
Frontend prevents many invalid actions for better user experience,
but backend still enforces all business rules.
```

This is important because API calls can still be made outside the UI, for example through Postman.

---

## How to Run the Project

### 1. Clone the Repository

```bash
git clone <repository-url>
cd bank-account-management
```

---

### 2. Start MySQL with Docker Compose

From the project root:

```bash
docker compose up -d
```

This starts the MySQL database container.

---

### 3. Start Backend

Go to the backend folder:

```bash
cd backend
```

#### Windows

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=docker
```

#### macOS / Linux

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```

Backend runs on:

```text
http://localhost:8080
```

Flyway will automatically create:

```text
accounts
transfers
flyway_schema_history
```

---

### 4. Start Frontend

Open another terminal:

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on:

```text
http://localhost:5173
```

---

## Docker Database Inspection Commands

### Check running containers

```bash
docker ps
```

---

### Connect to MySQL container

Adjust the container name, username, or database name if your `docker-compose.yml` uses different values.

```bash
docker exec -it bank-account-mysql mysql -u bank_user -p
```

After running the command, enter the database password.

---

### Select database

```sql
USE bank_account_db;
```

---

### Show tables

```sql
SHOW TABLES;
```

Expected tables:

```text
accounts
transfers
flyway_schema_history
```

---

### Check Flyway migration history

```sql
SELECT * FROM flyway_schema_history;
```

---

### Describe table structure

```sql
DESC accounts;
DESC transfers;
```

---

### View table data

```sql
SELECT * FROM accounts;
SELECT * FROM transfers;
```

---

### Clean Docker database and recreate it

This removes the database volume and starts with a clean database.

```bash
docker compose down -v
docker compose up -d
```

Use this when you want to test Flyway migrations from a clean state.

---

## Manual Test Scenarios

### Account Tests

| Scenario | Expected Result |
|---|---|
| Create valid account | Account is created and form is cleared |
| Create account with duplicate name | Error message is shown |
| Create account with duplicate IBAN | Error message is shown |
| Create account with invalid IBAN | Error message is shown |
| Create account with negative amount | Validation error is shown |
| Edit account | Account is updated |
| Edit account with duplicate name from another account | Error message is shown |
| Freeze active account | Status becomes `FROZEN` |
| Unfreeze frozen account | Status becomes `ACTIVE` |

---

### Transfer Tests

| Scenario | Expected Result |
|---|---|
| Create valid transfer | Source balance decreases and beneficiary balance increases |
| View source account history | `DEBIT` row is displayed |
| View beneficiary account history | `CREDIT` row is displayed |
| Compare reference IDs | `DEBIT` and `CREDIT` rows have same reference ID |
| Transfer with insufficient funds | Error message is shown |
| Transfer from frozen account | Backend rejects the operation |
| Transfer to frozen account | Backend rejects the operation |
| Transfer to same account | Backend rejects the operation |
| Empty transfer form | Validation errors are shown |

---

## Account Transfer History vs Transfer Operation History vs Audit Log

The current implementation provides account-level transfer history through:

```text
GET /api/accounts/{accountId}/transfers
```

This answers the question:

```text
What happened with the money on this specific account?
```

---

### 1. Account Transfer History

Example:

```text
Account A sends 100.00 to Account B.
```

The system creates two rows with the same `referenceId`.

| Account Perspective | Type | Counterparty | Amount | Meaning |
|---|---|---|---:|---|
| Account A | `DEBIT` | Account B | -100.00 | Money left Account A and was sent to Account B |
| Account B | `CREDIT` | Account A | +100.00 | Money entered Account B from Account A |

When the user selects Account A, they see the DEBIT side of the operation.  
When the user selects Account B, they see the CREDIT side of the operation.

Both rows share the same `referenceId` because they belong to the same logical transfer operation.

---

### 2. Transfer Operation History

A possible future improvement would be to add a transfer operation history view.

This would answer a different question:

```text
Which logical transfer operations were performed in the system?
```

Instead of showing a transfer only from one account perspective, this view could group both rows by `referenceId`.

Example:

| Reference ID | From Account | To Account | Amount | Status | Created On |
|---|---|---|---:|---|---|
| `abc-123` | Account A | Account B | 100.00 | `COMPLETED` | 2026-05-01 10:00:00 |

The existing repository method:

```java
findByReferenceIdOrderByCreatedOnAsc(referenceId)
```

can be useful for a detail view of one transfer operation.

It can return both rows:

```text
DEBIT row
CREDIT row
```

without requiring the user to switch between accounts.

For a full list of all logical transfer operations, a separate query or projection grouped by `reference_id` would be more appropriate.

Reason:

```text
Each logical transfer creates two rows,
but the operation history should display it once.
```

---

### 3. Audit Log

An audit log is different from both account transfer history and transfer operation history.

It answers the question:

```text
Who performed what action, when, and what changed?
```

An audit log would not only track money movement, but also account and system actions.

Examples:

| Action | Entity Type | Entity ID | Old Value | New Value | Actor | Created On |
|---|---|---:|---|---|---|---|
| `ACCOUNT_CREATED` | `ACCOUNT` | 1 | - | Main Account | admin@example.com | 2026-05-01 |
| `ACCOUNT_FROZEN` | `ACCOUNT` | 1 | `ACTIVE` | `FROZEN` | admin@example.com | 2026-05-01 |
| `ACCOUNT_UNFROZEN` | `ACCOUNT` | 1 | `FROZEN` | `ACTIVE` | admin@example.com | 2026-05-01 |
| `TRANSFER_CREATED` | `TRANSFER` | `abc-123` | - | Account A -> Account B, 100.00 | admin@example.com | 2026-05-01 |

Possible `audit_logs` table:

| Column | Purpose |
|---|---|
| `id` | Audit row id |
| `actor` | User or system that performed the action |
| `action` | Type of action |
| `entity_type` | ACCOUNT, TRANSFER, USER, etc. |
| `entity_id` | Affected entity id |
| `old_value` | Previous value |
| `new_value` | New value |
| `description` | Human-readable description |
| `created_on` | Timestamp |
| `ip_address` | Optional source IP |

Summary:

| History Type | Main Question | Example |
|---|---|---|
| Account Transfer History | What happened with this account's money? | Account A shows DEBIT 100.00 to Account B |
| Transfer Operation History | What logical transfers happened in the system? | Reference `abc-123`: Account A -> Account B, 100.00 |
| Audit Log | Who did what and when? | User froze Account A from ACTIVE to FROZEN |

---

## Known Limitations

The project is intentionally kept simple and focused on the assignment requirements.

Known limitations:

| Limitation | Explanation |
|---|---|
| No authentication | There is no login or user identity |
| No authorization | There are no roles or permissions |
| No concurrent transfer locking | Concurrent transfer protection is not implemented |
| No full IBAN checksum validation | Only basic IBAN format validation is implemented |
| No pagination | Account and transfer lists are returned fully |
| No transfer status lifecycle | Transfers are created as completed immediately |
| No audit log | User/system actions are not audited |
| No dedicated transfer detail UI | `GET /api/transfers/{id}` exists but is not exposed as a separate UI page |
| Backend/frontend are not containerized | Docker Compose currently runs only the database |

---

## Future Improvements

Possible improvements for a next version:

### Security

- Add Spring Security
- Add JWT authentication
- Add users and roles
- Restrict account and transfer actions based on permissions

### Banking Business Rules

- Decide whether frozen accounts should be fully locked from editing
- Allow editing only selected fields for frozen accounts
- Make `availableAmount` read-only after account creation
- Change balance only through financial operations
- Add deposit, withdrawal, fee, refund, and correction operations

### Transfer Reliability

- Add optimistic locking with `@Version`
- Or use pessimistic locking when loading accounts for transfer
- Prevent race conditions when multiple transfers happen at the same time

Example problem:

```text
Account has 100.00.
Two requests try to transfer 80.00 at the same time.
Both should not be allowed to succeed.
```

### IBAN Validation

- Add full IBAN mod-97 checksum validation
- Add country-specific IBAN rules

### History and Audit

- Add transfer operation history grouped by `referenceId`
- Add transfer operation detail UI
- Use `GET /api/transfers/{id}` in a dedicated transfer details screen
- Add audit logging for account creation, updates, freeze/unfreeze, and transfer creation

### API and Frontend

- Add pagination, filtering, and sorting
- Add frontend routing
- Add account detail page
- Add transfer detail page
- Improve UI design
- Add loading spinners and better empty states

### Testing and DevOps

- Add unit tests
- Add integration tests
- Add Testcontainers for database integration testing
- Dockerize backend
- Dockerize frontend
- Add CI pipeline

---

## Final Notes

The implementation is focused on the requested assignment scope while still applying several production-oriented practices:

- database schema managed with Flyway
- Hibernate schema validation
- DTO-based API design
- centralized error handling
- service-layer business validation
- transactional transfer creation
- React frontend with reusable components
- frontend validation display using backend error responses

The project avoids unnecessary over-engineering, but documents several realistic improvements that would be important in a production banking system.