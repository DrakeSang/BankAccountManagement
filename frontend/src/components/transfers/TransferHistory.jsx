/**
 * TransferHistory shows all transfer rows for a selected account.
 *
 * Important:
 * Backend returns transfers from the perspective of the selected account.
 *
 * DEBIT:
 * - money leaves the selected account
 * - UI displays: To counterparty
 *
 * CREDIT:
 * - money enters the selected account
 * - UI displays: From counterparty
 */
function TransferHistory({
                             accounts,
                             selectedAccountId,
                             onSelectedAccountChange,
                             transfers,
                             loading
                         }) {
    function formatMoney(value) {
        if (value === null || value === undefined) {
            return "0.00";
        }

        return Number(value).toFixed(2);
    }

    function formatDate(value) {
        if (!value) {
            return "";
        }

        return new Date(value).toLocaleString();
    }

    /**
     * counterpartyAccountName means the other account involved in the transfer.
     *
     * For DEBIT:
     * selected account sent money TO counterparty.
     *
     * For CREDIT:
     * selected account received money FROM counterparty.
     */
    function getCounterpartyText(transfer) {
        if (transfer.type === "DEBIT") {
            return `To ${transfer.counterpartyAccountName}`;
        }

        return `From ${transfer.counterpartyAccountName}`;
    }

    function getAmountText(transfer) {
        const amount = formatMoney(transfer.amount);

        if (transfer.type === "DEBIT") {
            return `- ${amount}`;
        }

        return `+ ${amount}`;
    }

    return (
        <div className="card shadow-sm mb-4">
            <div className="card-body">
                <h5 className="card-title mb-3">Transfer History</h5>

                <div className="mb-3">
                    <label htmlFor="transferHistoryAccountId" className="form-label">
                        Account
                    </label>

                    {/*
                      For history we show all accounts, including frozen accounts.
                      Reason:
                      A frozen account may still have old transfers from the past.
                    */}
                    <select
                        id="transferHistoryAccountId"
                        className="form-select"
                        value={selectedAccountId}
                        onChange={(event) => onSelectedAccountChange(event.target.value)}
                        disabled={loading}
                    >
                        <option value="">Select account to view transfers</option>

                        {accounts.map((account) => (
                            <option key={account.id} value={account.id}>
                                {account.name} — {account.iban} — {account.status}
                            </option>
                        ))}
                    </select>
                </div>

                {!selectedAccountId && (
                    <div className="alert alert-info mb-0">
                        Select an account to view its transfer history.
                    </div>
                )}

                {selectedAccountId && loading && (
                    <div className="alert alert-info mb-0">
                        Loading transfers...
                    </div>
                )}

                {selectedAccountId && !loading && transfers.length === 0 && (
                    <div className="alert alert-secondary mb-0">
                        No transfers found for this account.
                    </div>
                )}

                {selectedAccountId && !loading && transfers.length > 0 && (
                    <div className="table-responsive">
                        <table className="table table-striped table-hover align-middle">
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Date</th>
                                <th>Reference ID</th>
                                <th>Type</th>
                                <th>Counterparty</th>
                                <th className="text-end">Amount</th>
                            </tr>
                            </thead>

                            <tbody>
                            {transfers.map((transfer) => (
                                <tr key={transfer.id}>
                                    <td>{transfer.id}</td>

                                    <td>{formatDate(transfer.createdOn)}</td>

                                    <td>
                                        <small className="text-muted">
                                            {transfer.referenceId}
                                        </small>
                                    </td>

                                    <td>
                                        {transfer.type === "DEBIT" ? (
                                            <span className="badge bg-danger">DEBIT</span>
                                        ) : (
                                            <span className="badge bg-success">CREDIT</span>
                                        )}
                                    </td>

                                    <td>{getCounterpartyText(transfer)}</td>

                                    <td
                                        className={
                                            transfer.type === "DEBIT"
                                                ? "text-end text-danger fw-semibold"
                                                : "text-end text-success fw-semibold"
                                        }
                                    >
                                        {getAmountText(transfer)}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
}

export default TransferHistory;