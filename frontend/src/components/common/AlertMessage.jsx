/**
 * Reusable component for showing success and error messages.
 *
 * It supports:
 * - simple success message
 * - backend business error message
 * - backend validation errors from validationErrors map
 *
 * Example backend validation response:
 *
 * {
 *   "message": "Validation failed",
 *   "validationErrors": {
 *     "name": "Account name is required",
 *     "availableAmount": "Available amount cannot be negative"
 *   }
 * }
 */
function AlertMessage({ error, successMessage, onClear }) {
    if (!error && !successMessage) {
        return null;
    }

    return (
        <div className="mb-3">
            {successMessage && (
                <div className="alert alert-success d-flex justify-content-between align-items-start">
                    <span>{successMessage}</span>

                    <button
                        type="button"
                        className="btn-close"
                        aria-label="Close"
                        onClick={onClear}
                    />
                </div>
            )}

            {error && (
                <div className="alert alert-danger">
                    <div className="d-flex justify-content-between align-items-start">
                        <div>
                            <strong>Error: </strong>
                            {error.message || 'Something went wrong'}
                        </div>

                        <button
                            type="button"
                            className="btn-close"
                            aria-label="Close"
                            onClick={onClear}
                        />
                    </div>

                    {error.validationErrors && (
                        <ul className="mt-2 mb-0">
                            {Object.entries(error.validationErrors).map(([field, message]) => (
                                <li key={field}>
                                    <strong>{field}</strong>: {message}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            )}
        </div>
    );
}

export default AlertMessage;