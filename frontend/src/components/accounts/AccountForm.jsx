import {useEffect, useState} from 'react';

// Initial empty form state.
// This is used when we create a new account or reset the form.
const initialFormState = {
    name: '',
    iban: '',
    availableAmount: ''
};

/**
 * AccountForm is used for both:
 * - creating a new account
 * - editing an existing account
 *
 * How do we decide the mode?
 *
 * If selectedAccount === null:
 * - create mode
 *
 * If selectedAccount has value:
 * - edit mode
 *
 * The parent component App.jsx controls selectedAccount.
 */
function AccountForm({
                         selectedAccount,
                         onCreateAccount,
                         onUpdateAccount,
                         onCancelEdit,
                         isLoading
                     }) {
    /**
     * State means data that React remembers between renders.
     *
     * Here formData stores the current values in the form inputs.
     *
     * Every time the user types in an input,
     * we update this state with setFormData().
     *
     * Because the input value comes from React state,
     * these are called controlled inputs.
     */
    const [formData, setFormData] = useState(initialFormState);

    /**
     * useEffect runs side effects.
     *
     * Here it runs whenever selectedAccount changes.
     *
     * Flow:
     * - user clicks Edit in the table
     * - App.jsx sets selectedAccount
     * - this useEffect receives the new selectedAccount
     * - the form is filled with the selected account values
     *
     * If selectedAccount becomes null, we reset the form.
     */
    useEffect(() => {
        if (selectedAccount) {
            setFormData({
                name: selectedAccount.name,
                iban: selectedAccount.iban,
                availableAmount: selectedAccount.availableAmount
            });
        } else {
            setFormData(initialFormState);
        }
    }, [selectedAccount]);

    /**
     * Generic input change handler.
     *
     * It works because every input has a "name" attribute:
     * name="name"
     * name="iban"
     * name="availableAmount"
     *
     * Example:
     * If user types in the IBAN field:
     * event.target.name = "iban"
     * event.target.value = current input value
     */
    function handleInputChange(event) {
        const {name, value} = event.target;

        setFormData((currentFormData) => ({
            ...currentFormData,
            [name]: value
        }));
    }

    /**
     * Handles form submit.
     *
     * Flow:
     * - prevent default browser submit
     * - build request payload
     * - if selectedAccount exists -> update
     * - otherwise -> create
     *
     * Important UX behavior:
     * - if create/update is successful, we reset the form
     * - if backend returns validation/business error, we keep the user input
     *
     * This is why onCreateAccount/onUpdateAccount should return:
     * - true  when operation succeeds
     * - false when operation fails
     */
    async function handleSubmit(event) {
        event.preventDefault();

        const payload = {
            name: formData.name,
            iban: formData.iban,

            // Backend expects BigDecimal.
            // JSON sends this as number.
            // If the field is empty, we send null so backend @NotNull validation can handle it.
            availableAmount: formData.availableAmount === '' ? null : Number(formData.availableAmount)
        };

        let isSuccessful;

        if (selectedAccount) {
            isSuccessful = await onUpdateAccount(selectedAccount.id, payload);
        } else {
            isSuccessful = await onCreateAccount(payload);
        }

        /**
         * Reset the form only after successful create/update.
         *
         * For create:
         * - fields become empty again
         * - placeholders are visible again
         *
         * For update:
         * - App.jsx also clears selectedAccount
         * - form returns to create mode
         */
        if (isSuccessful) {
            setFormData(initialFormState);
        }
    }

    const isEditMode = Boolean(selectedAccount);

    return (
        <div className="card mb-4">
            <div className="card-header">
                <h5 className="mb-0">
                    {isEditMode ? 'Edit Account' : 'Create Account'}
                </h5>
            </div>

            <div className="card-body">
                <form onSubmit={handleSubmit} noValidate>
                    <div className="row g-3">
                        <div className="col-md-4">
                            <label htmlFor="name" className="form-label">
                                Account Name
                            </label>

                            <input
                                id="name"
                                name="name"
                                type="text"
                                className="form-control"
                                value={formData.name}
                                onChange={handleInputChange}
                                placeholder="Main Account"
                                disabled={isLoading}
                                required
                            />
                        </div>

                        <div className="col-md-5">
                            <label htmlFor="iban" className="form-label">
                                IBAN
                            </label>

                            <input
                                id="iban"
                                name="iban"
                                type="text"
                                className="form-control"
                                value={formData.iban}
                                onChange={handleInputChange}
                                placeholder="BG18RZBB91550123456789"
                                disabled={isLoading}
                                minLength={15}
                                maxLength={34}
                                required
                            />
                        </div>

                        <div className="col-md-3">
                            <label htmlFor="availableAmount" className="form-label">
                                Available Amount
                            </label>

                            <input
                                id="availableAmount"
                                name="availableAmount"
                                type="number"
                                step="0.01"
                                min="0"
                                className="form-control"
                                value={formData.availableAmount}
                                onChange={handleInputChange}
                                placeholder="1000.00"
                                disabled={isLoading}
                                required
                            />
                        </div>
                    </div>

                    <div className="mt-3 d-flex gap-2">
                        <button
                            type="submit"
                            className="btn btn-primary"
                            disabled={isLoading}
                        >
                            {isEditMode ? 'Update Account' : 'Create Account'}
                        </button>

                        {isEditMode && (
                            <button
                                type="button"
                                className="btn btn-outline-secondary"
                                onClick={onCancelEdit}
                                disabled={isLoading}
                            >
                                Cancel
                            </button>
                        )}
                    </div>
                </form>
            </div>
        </div>
    );
}

export default AccountForm;