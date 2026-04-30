/**
 * Formats ISO timestamp from backend.
 *
 * Backend returns Instant values like:
 * 2026-04-30T12:45:10.123Z
 *
 * Frontend converts them to readable local date/time.
 */
function formatDateTime(value) {
    if (!value) {
        return '-';
    }

    return new Date(value).toLocaleString();
}

/**
 * Formats money-like values.
 *
 * This is simple formatting for the UI.
 * The backend still stores the real value as BigDecimal.
 */
function formatAmount(value) {
    return Number(value).toFixed(2);
}

/**
 * AccountTable shows all accounts in a table.
 *
 * It receives data and actions from App.jsx.
 *
 * It does not call the backend directly.
 * This keeps the component reusable and focused only on UI.
 */
function AccountTable({
                          accounts,
                          onEditAccount,
                          onFreezeAccount,
                          onUnfreezeAccount,
                          isLoading
                      }) {
    if (accounts.length === 0) {
        return (
            <div className="alert alert-info">
                No accounts found. Create the first account using the form above.
            </div>
        );
    }

    return (
        <div className="card">
            <div className="card-header">
                <h5 className="mb-0">Accounts</h5>
            </div>

            <div className="card-body">
                <div className="table-responsive">
                    <table className="table table-striped table-hover align-middle">
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>IBAN</th>
                            <th>Status</th>
                            <th>Available Amount</th>
                            <th>Created On</th>
                            <th>Modified On</th>
                            <th>Actions</th>
                        </tr>
                        </thead>

                        <tbody>
                        {accounts.map((account) => (
                            <tr key={account.id}>
                                <td>{account.id}</td>
                                <td>{account.name}</td>
                                <td>{account.iban}</td>

                                <td>
                    <span
                        className={
                            account.status === 'ACTIVE'
                                ? 'badge text-bg-success'
                                : 'badge text-bg-secondary'
                        }
                    >
                      {account.status}
                    </span>
                                </td>

                                <td>{formatAmount(account.availableAmount)}</td>
                                <td>{formatDateTime(account.createdOn)}</td>
                                <td>{formatDateTime(account.modifiedOn)}</td>

                                <td>
                                    <div className="d-flex gap-2">
                                        <button
                                            type="button"
                                            className="btn btn-sm btn-outline-primary"
                                            onClick={() => onEditAccount(account)}
                                            disabled={isLoading}
                                        >
                                            Edit
                                        </button>

                                        {/*
                        Correct frontend behavior:

                        If account is ACTIVE:
                        - show only Freeze button

                        If account is FROZEN:
                        - show only Unfreeze button

                        This prevents unnecessary user mistakes.
                        The backend still validates the same rules.
                      */}
                                        {account.status === 'ACTIVE' ? (
                                            <button
                                                type="button"
                                                className="btn btn-sm btn-outline-warning"
                                                onClick={() => onFreezeAccount(account.id)}
                                                disabled={isLoading}
                                            >
                                                Freeze
                                            </button>
                                        ) : (
                                            <button
                                                type="button"
                                                className="btn btn-sm btn-outline-success"
                                                onClick={() => onUnfreezeAccount(account.id)}
                                                disabled={isLoading}
                                            >
                                                Unfreeze
                                            </button>
                                        )}
                                    </div>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}

export default AccountTable;