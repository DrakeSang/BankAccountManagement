import { useEffect, useState } from 'react';

import { accountApi } from './api/accountApi';
import AlertMessage from './components/AlertMessage';
import AccountForm from './components/AccountForm';
import AccountTable from './components/AccountTable';

/**
 * App is the main component for the account management page.
 *
 * It owns the main state:
 * - accounts
 * - selectedAccount
 * - loading
 * - error
 * - successMessage
 *
 * Child components receive data and functions through props.
 */
function App() {
  /**
   * accounts state:
   *
   * Stores the list of accounts received from backend.
   * Initially it is an empty array.
   *
   * After GET /api/accounts succeeds,
   * we update it with setAccounts(data).
   */
  const [accounts, setAccounts] = useState([]);

  /**
   * selectedAccount state:
   *
   * null means the form is in create mode.
   *
   * If user clicks Edit on an account,
   * selectedAccount becomes that account object
   * and the form switches to edit mode.
   */
  const [selectedAccount, setSelectedAccount] = useState(null);

  /**
   * loading state:
   *
   * true means an API request is currently running.
   *
   * We use it to:
   * - disable buttons
   * - disable form inputs
   * - show loading message
   */
  const [loading, setLoading] = useState(false);

  /**
   * error state:
   *
   * Stores backend or frontend error.
   *
   * Example:
   * - duplicate account name
   * - duplicate IBAN
   * - validation failed
   * - backend unavailable
   */
  const [error, setError] = useState(null);

  /**
   * successMessage state:
   *
   * Stores short message after successful operation.
   *
   * Example:
   * - Account created successfully
   * - Account updated successfully
   */
  const [successMessage, setSuccessMessage] = useState('');

  /**
   * useEffect with empty dependency array [] runs once
   * when the component is first rendered.
   *
   * This is our page load flow:
   *
   * User opens http://localhost:5173
   * -> App component renders
   * -> useEffect runs
   * -> loadAccounts() calls backend
   * -> accounts state is updated
   * -> React re-renders table with the received accounts
   */
  useEffect(() => {
    loadAccounts();
  }, []);

  /**
   * Clears previous messages before a new action.
   */
  function clearMessages() {
    setError(null);
    setSuccessMessage('');
  }

  /**
   * Loads all accounts from backend.
   *
   * Backend endpoint:
   * GET /api/accounts
   */
  async function loadAccounts() {
    setLoading(true);
    setError(null);

    try {
      const data = await accountApi.getAllAccounts();
      setAccounts(data);
    } catch (apiError) {
      setError(apiError);
    } finally {
      setLoading(false);
    }
  }

  /**
   * Helper used after successful create/update/freeze/unfreeze.
   *
   * We reload the table from the backend instead of manually changing local state.
   * This keeps frontend data consistent with the database.
   */
  async function reloadAccountsAfterChange() {
    const data = await accountApi.getAllAccounts();
    setAccounts(data);
  }

  /**
   * Create account flow:
   *
   * User fills form
   * -> clicks Create Account
   * -> AccountForm calls onCreateAccount(payload)
   * -> App calls POST /api/accounts
   * -> backend validates and saves
   * -> frontend reloads accounts
   * -> form is reset because selectedAccount is null
   */
  async function handleCreateAccount(payload) {
    clearMessages();
    setLoading(true);

    try {
      await accountApi.createAccount(payload);
      await reloadAccountsAfterChange();

      setSelectedAccount(null);
      setSuccessMessage('Account created successfully');
    } catch (apiError) {
      setError(apiError);
    } finally {
      setLoading(false);
    }
  }

  /**
   * Edit button flow:
   *
   * User clicks Edit in table
   * -> selectedAccount becomes that account
   * -> AccountForm useEffect fills the form fields
   * -> form switches from create mode to edit mode
   */
  function handleEditAccount(account) {
    clearMessages();
    setSelectedAccount(account);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  /**
   * Update account flow:
   *
   * User edits form
   * -> clicks Update Account
   * -> AccountForm calls onUpdateAccount(id, payload)
   * -> App calls PUT /api/accounts/{id}
   * -> backend validates uniqueness and saves
   * -> frontend reloads accounts
   * -> selectedAccount becomes null
   * -> form returns to create mode
   */
  async function handleUpdateAccount(accountId, payload) {
    clearMessages();
    setLoading(true);

    try {
      await accountApi.updateAccount(accountId, payload);
      await reloadAccountsAfterChange();

      setSelectedAccount(null);
      setSuccessMessage('Account updated successfully');
    } catch (apiError) {
      setError(apiError);
    } finally {
      setLoading(false);
    }
  }

  /**
   * Cancel edit flow:
   *
   * User clicks Cancel
   * -> selectedAccount becomes null
   * -> AccountForm resets fields
   * -> form returns to create mode
   */
  function handleCancelEdit() {
    clearMessages();
    setSelectedAccount(null);
  }

  /**
   * Freeze flow:
   *
   * User clicks Freeze
   * -> App calls PATCH /api/accounts/{id}/freeze
   * -> backend changes status to FROZEN
   * -> frontend reloads accounts
   * -> table now shows Unfreeze button for that account
   */
  async function handleFreezeAccount(accountId) {
    clearMessages();
    setLoading(true);

    try {
      await accountApi.freezeAccount(accountId);
      await reloadAccountsAfterChange();

      setSuccessMessage('Account frozen successfully');
    } catch (apiError) {
      setError(apiError);
    } finally {
      setLoading(false);
    }
  }

  /**
   * Unfreeze flow:
   *
   * User clicks Unfreeze
   * -> App calls PATCH /api/accounts/{id}/unfreeze
   * -> backend changes status to ACTIVE
   * -> frontend reloads accounts
   * -> table now shows Freeze button for that account
   */
  async function handleUnfreezeAccount(accountId) {
    clearMessages();
    setLoading(true);

    try {
      await accountApi.unfreezeAccount(accountId);
      await reloadAccountsAfterChange();

      setSuccessMessage('Account unfrozen successfully');
    } catch (apiError) {
      setError(apiError);
    } finally {
      setLoading(false);
    }
  }

  /**
   * Backend error flow:
   *
   * Backend returns error response:
   * {
   *   "status": 409,
   *   "message": "IBAN already exists"
   * }
   *
   * or validation response:
   * {
   *   "status": 400,
   *   "message": "Validation failed",
   *   "validationErrors": {
   *     "availableAmount": "Available amount cannot be negative"
   *   }
   * }
   *
   * accountApi throws an Error object.
   * App catches it and saves it in error state.
   * AlertMessage displays it on the page.
   */
  return (
      <div className="container py-4">
        <header className="mb-4">
          <h1 className="mb-1">Bank Account Management</h1>
          <p className="text-muted mb-0">
            Create, edit, freeze and unfreeze bank accounts.
          </p>
        </header>

        <AlertMessage
            error={error}
            successMessage={successMessage}
            onClear={clearMessages}
        />

        {loading && (
            <div className="alert alert-secondary">
              Processing request...
            </div>
        )}

        <AccountForm
            selectedAccount={selectedAccount}
            onCreateAccount={handleCreateAccount}
            onUpdateAccount={handleUpdateAccount}
            onCancelEdit={handleCancelEdit}
            isLoading={loading}
        />

        <AccountTable
            accounts={accounts}
            onEditAccount={handleEditAccount}
            onFreezeAccount={handleFreezeAccount}
            onUnfreezeAccount={handleUnfreezeAccount}
            isLoading={loading}
        />
      </div>
  );
}

export default App;