import { useState } from "react";

/**
 * TransferForm is responsible only for creating new transfers.
 *
 * It receives:
 * - accounts: all accounts loaded from the backend
 * - onCreateTransfer: function from TransferSection that calls the backend
 * - loading: disables the button while request is in progress
 *
 * Important frontend behavior:
 * - only ACTIVE accounts are shown in the transfer form
 * - beneficiary dropdown does not show the selected source account
 *
 * Backend still validates everything.
 * Frontend only improves the user experience.
 */
function TransferForm({ accounts, onCreateTransfer, loading }) {
    const [formData, setFormData] = useState({
        accountId: "",
        beneficiaryAccountId: "",
        amount: ""
    });

    /**
     * Frozen accounts are not shown in create transfer dropdowns,
     * because backend does not allow frozen accounts to participate in transfers.
     */
    const activeAccounts = accounts.filter((account) => account.status === "ACTIVE");

    /**
     * Beneficiary options exclude the selected source account.
     *
     * Example:
     * If source account is Main Account,
     * user should not be able to select Main Account again as beneficiary.
     */
    const beneficiaryOptions = activeAccounts.filter(
        (account) => String(account.id) !== String(formData.accountId)
    );

    function handleInputChange(event) {
        const { name, value } = event.target;

        setFormData((previousFormData) => {
            const updatedFormData = {
                ...previousFormData,
                [name]: value
            };

            /**
             * If the user changes the source account and the beneficiary
             * becomes the same account, we clear the beneficiary selection.
             */
            if (
                name === "accountId" &&
                String(value) === String(previousFormData.beneficiaryAccountId)
            ) {
                updatedFormData.beneficiaryAccountId = "";
            }

            return updatedFormData;
        });
    }

    async function handleSubmit(event) {
        event.preventDefault();

        /**
         * We intentionally convert empty values to null.
         *
         * This allows backend validation to return clear messages like:
         * - Source account id is required
         * - Beneficiary account id is required
         * - Transfer amount is required
         */
        const payload = {
            accountId: formData.accountId ? Number(formData.accountId) : null,
            beneficiaryAccountId: formData.beneficiaryAccountId
                ? Number(formData.beneficiaryAccountId)
                : null,
            amount: formData.amount === "" ? null : formData.amount
        };

        await onCreateTransfer(payload);

        /**
         * After successful transfer we clear only the amount.
         * Keeping selected accounts makes it easier to create another transfer
         * between the same two accounts.
         */
        setFormData((previousFormData) => ({
            ...previousFormData,
            amount: ""
        }));
    }

    return (
        <div className="card shadow-sm mb-4">
            <div className="card-body">
                <h5 className="card-title mb-3">Create Transfer</h5>

                {activeAccounts.length < 2 && (
                    <div className="alert alert-warning">
                        You need at least two active accounts to create a transfer.
                    </div>
                )}

                {/* noValidate disables browser validation so we can see backend validation errors */}
                <form onSubmit={handleSubmit} noValidate>
                    <div className="row g-3">
                        <div className="col-md-4">
                            <label htmlFor="accountId" className="form-label">
                                Source Account
                            </label>

                            <select
                                id="accountId"
                                name="accountId"
                                className="form-select"
                                value={formData.accountId}
                                onChange={handleInputChange}
                                disabled={loading}
                            >
                                <option value="">Select source account</option>

                                {activeAccounts.map((account) => (
                                    <option key={account.id} value={account.id}>
                                        {account.name} — {account.iban} — {account.availableAmount}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="col-md-4">
                            <label htmlFor="beneficiaryAccountId" className="form-label">
                                Beneficiary Account
                            </label>

                            <select
                                id="beneficiaryAccountId"
                                name="beneficiaryAccountId"
                                className="form-select"
                                value={formData.beneficiaryAccountId}
                                onChange={handleInputChange}
                                disabled={loading}
                            >
                                <option value="">Select beneficiary account</option>

                                {beneficiaryOptions.map((account) => (
                                    <option key={account.id} value={account.id}>
                                        {account.name} — {account.iban}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="col-md-4">
                            <label htmlFor="amount" className="form-label">
                                Amount
                            </label>

                            <input
                                id="amount"
                                name="amount"
                                type="number"
                                step="0.01"
                                min="0"
                                className="form-control"
                                placeholder="Example: 100.00"
                                value={formData.amount}
                                onChange={handleInputChange}
                                disabled={loading}
                            />
                        </div>
                    </div>

                    <div className="mt-3">
                        <button
                            type="submit"
                            className="btn btn-primary"
                            disabled={loading || activeAccounts.length < 2}
                        >
                            {loading ? "Creating..." : "Create Transfer"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default TransferForm;