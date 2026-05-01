import {useEffect, useState} from 'react';

/**
 * Reusable component for showing success and error messages.
 *
 * UX behavior:
 * - success messages disappear automatically after a few seconds
 * - error messages stay visible until the user closes them manually
 *
 * Reason:
 * Success messages are usually short confirmations.
 * Error messages may contain validation details that the user needs to read.
 */
function AlertMessage({error, successMessage, onClear}) {
    const [isVisible, setIsVisible] = useState(false);

    /**
     * Whenever a new success or error message appears,
     * we make the alert visible again.
     */
    useEffect(() => {
        if (error || successMessage) {
            setIsVisible(true);
        }
    }, [error, successMessage]);

    /**
     * Auto-hide only success messages.
     *
     * Errors are not auto-hidden because they may contain important
     * validation or business information.
     */
    useEffect(() => {
        if (!successMessage || error) {
            return;
        }

        const hideTimer = setTimeout(() => {
            setIsVisible(false);
        }, 3500);

        const clearTimer = setTimeout(() => {
            onClear();
        }, 3800);

        return () => {
            clearTimeout(hideTimer);
            clearTimeout(clearTimer);
        };
    }, [successMessage, error, onClear]);

    if (!error && !successMessage) {
        return null;
    }

    function handleClose() {
        setIsVisible(false);

        setTimeout(() => {
            onClear();
        }, 300);
    }

    const fadeClass = isVisible ? 'alert-fade show' : 'alert-fade hide';

    return (
        <div className="mb-3">
            {successMessage && (
                <div className={`alert alert-success d-flex justify-content-between align-items-start ${fadeClass}`}>
                    <span>{successMessage}</span>

                    <button
                        type="button"
                        className="btn-close"
                        aria-label="Close"
                        onClick={handleClose}
                    />
                </div>
            )}

            {error && (
                <div className={`alert alert-danger ${fadeClass}`}>
                    <div className="d-flex justify-content-between align-items-start">
                        <div>
                            <strong>Error: </strong>
                            {error.message || 'Something went wrong'}
                        </div>

                        <button
                            type="button"
                            className="btn-close"
                            aria-label="Close"
                            onClick={handleClose}
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