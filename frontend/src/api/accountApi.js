// This file contains all HTTP calls related to accounts.
//
// The main idea is:
// React components should not know the exact fetch implementation.
// Components call accountApi.getAllAccounts(), accountApi.createAccount(), etc.
// This keeps API logic in one place.

const API_BASE_URL = "http://localhost:8080/api";

/**
 * Handles every backend response in one central place.
 *
 * If the response is successful:
 * - return the parsed JSON body
 *
 * If the response is not successful:
 * - read the backend ErrorResponse
 * - create a JavaScript Error object
 * - attach status and validationErrors to it
 * - throw it so App.jsx can display it
 */
async function handleResponse(response) {
    const contentType = response.headers.get('content-type');

    // Some responses may have JSON body, others may not.
    // Our backend returns JSON, but this keeps the helper safer.
    const data = contentType?.includes('application/json')
        ? await response.json()
        : null;

    if (!response.ok) {
        const error = new Error(data?.message || 'Request failed');

        // We attach extra backend details to the error object.
        // Later AlertMessage can show validationErrors if they exist.
        error.status = response.status;
        error.backendError = data;
        error.validationErrors = data?.validationErrors || null;

        throw error;
    }

    return data;
}

/**
 * Small helper around fetch.
 *
 * endpoint example:
 * /accounts
 * /accounts/1/freeze
 *
 * Full URL becomes:
 * http://localhost:8080/api/accounts
 */
async function request(endpoint, options = {}) {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        },
        ...options
    });

    return handleResponse(response);
}

/**
 * Object that exposes all account API operations.
 *
 * These methods match our backend endpoints:
 *
 * GET    /api/accounts
 * POST   /api/accounts
 * PUT    /api/accounts/{id}
 * PATCH  /api/accounts/{id}/freeze
 * PATCH  /api/accounts/{id}/unfreeze
 */
export const accountApi = {
    getAllAccounts() {
        return request('/accounts');
    },

    createAccount(accountData) {
        return request('/accounts', {
            method: 'POST',
            body: JSON.stringify(accountData)
        });
    },

    updateAccount(accountId, accountData) {
        return request(`/accounts/${accountId}`, {
            method: 'PUT',
            body: JSON.stringify(accountData)
        });
    },

    freezeAccount(accountId) {
        return request(`/accounts/${accountId}/freeze`, {
            method: 'PATCH'
        });
    },

    unfreezeAccount(accountId) {
        return request(`/accounts/${accountId}/unfreeze`, {
            method: 'PATCH'
        });
    }
};