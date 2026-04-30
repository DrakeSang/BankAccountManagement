// Base URL на backend API-то.
// Backend-ът работи на port 8080, а React/Vite обикновено на 5173.
const API_BASE_URL = "http://localhost:8080/api";

/**
 * Common response handler за всички transfer API заявки.
 *
 * Защо го правим отделно?
 * - Ако response е успешен, връщаме JSON body-то.
 * - Ако backend върне грешка, хвърляме error object-а.
 *
 * Backend грешките при нас могат да бъдат:
 *
 * 1. Business error:
 * {
 *   "status": 400,
 *   "message": "Insufficient funds"
 * }
 *
 * 2. Validation errors:
 * {
 *   "status": 400,
 *   "message": "Validation failed",
 *   "validationErrors": {
 *      "amount": "Transfer amount must be greater than zero"
 *   }
 * }
 */
async function handleResponse(response) {
    const data = await response.json().catch(() => null);

    if (!response.ok) {
        throw data || {
            message: "Something went wrong while communicating with the server."
        };
    }

    return data;
}

/**
 * Creates a bank transfer between two accounts.
 *
 * Backend endpoint:
 * POST /api/transfers
 *
 * Expected payload:
 * {
 *   accountId: 1,
 *   beneficiaryAccountId: 2,
 *   amount: "100.00"
 * }
 */
export async function createTransfer(payload) {
    const response = await fetch(`${API_BASE_URL}/transfers`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    });

    return handleResponse(response);
}

/**
 * Loads all transfers for a specific account.
 *
 * Backend endpoint:
 * GET /api/accounts/{accountId}/transfers
 *
 * This returns the transfer history from the perspective of this account.
 */
export async function getTransfersByAccountId(accountId) {
    const response = await fetch(`${API_BASE_URL}/accounts/${accountId}/transfers`);

    return handleResponse(response);
}

/**
 * Loads one transfer row by id.
 *
 * We may not use this immediately in the UI,
 * but the backend endpoint exists and this keeps the API layer complete.
 */
export async function getTransferById(id) {
    const response = await fetch(`${API_BASE_URL}/transfers/${id}`);

    return handleResponse(response);
}