import { useState } from "react";
import TransferForm from "./TransferForm";
import TransferHistory from "./TransferHistory";
import {
    createTransfer,
    getTransfersByAccountId
} from "../../api/transferApi";

/**
 * TransferSection is the parent component for transfer functionality.
 *
 * It combines:
 * - TransferForm
 * - TransferHistory
 *
 * Why do we use this wrapper component?
 * - App.jsx stays cleaner.
 * - Transfer-specific state stays in one place.
 * - Form and history can communicate through this parent.
 */
function TransferSection({
                             accounts,
                             refreshAccounts,
                             setError,
                             setSuccessMessage
                         }) {
    /**
     * selectedAccountId is the account for which we display transfer history.
     */
    const [selectedAccountId, setSelectedAccountId] = useState("");

    /**
     * transfers contains all transfer rows for selectedAccountId.
     */
    const [transfers, setTransfers] = useState([]);

    /**
     * loadingTransfers is true while transfer history is loading.
     */
    const [loadingTransfers, setLoadingTransfers] = useState(false);

    /**
     * creatingTransfer is true while POST /api/transfers is running.
     */
    const [creatingTransfer, setCreatingTransfer] = useState(false);

    async function loadTransfersForAccount(accountId) {
        if (!accountId) {
            setTransfers([]);
            return;
        }

        try {
            setLoadingTransfers(true);
            setError(null);

            const data = await getTransfersByAccountId(accountId);

            setTransfers(data);
        } catch (error) {
            setTransfers([]);
            setError(error);
        } finally {
            setLoadingTransfers(false);
        }
    }

    async function handleSelectedAccountChange(accountId) {
        setSelectedAccountId(accountId);
        await loadTransfersForAccount(accountId);
    }

    async function handleCreateTransfer(payload) {
        try {
            setCreatingTransfer(true);
            setError(null);
            setSuccessMessage("");

            const result = await createTransfer(payload);

            /**
             * After a successful transfer:
             * 1. Backend created DEBIT and CREDIT rows.
             * 2. Backend changed balances of both accounts.
             * 3. Frontend must reload accounts so the account table shows new balances.
             */
            await refreshAccounts();

            /**
             * After creation, we show the history for the source account.
             * This makes the new DEBIT row visible immediately.
             */
            const accountIdToShow = String(payload.accountId);
            setSelectedAccountId(accountIdToShow);
            await loadTransfersForAccount(accountIdToShow);

            setSuccessMessage(
                `Transfer created successfully. Reference ID: ${result.referenceId}`
            );
        } catch (error) {
            setError(error);
        } finally {
            setCreatingTransfer(false);
        }
    }

    return (
        <section className="mt-4">
            <h3 className="mb-3">Transfers</h3>

            <TransferForm
                accounts={accounts}
                onCreateTransfer={handleCreateTransfer}
                loading={creatingTransfer}
            />

            <TransferHistory
                accounts={accounts}
                selectedAccountId={selectedAccountId}
                onSelectedAccountChange={handleSelectedAccountChange}
                transfers={transfers}
                loading={loadingTransfers}
            />
        </section>
    );
}

export default TransferSection;