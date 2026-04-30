import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

// Bootstrap CSS gives us ready-to-use classes:
// container, card, table, btn, form-control, alert, etc.
import 'bootstrap/dist/css/bootstrap.min.css';

import './App.css';
import App from './App.jsx';

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <App />
    </StrictMode>
);