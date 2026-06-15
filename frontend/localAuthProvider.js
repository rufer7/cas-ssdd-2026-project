// Local authentication provider for running fully without Auth0.
//
// Talks to the backend's `local` Spring profile, which secures the API with
// HTTP Basic and two in-memory users (see application-local.properties). The
// chosen credentials are kept in sessionStorage and sent as a Basic auth header.
//
// Enabled by setting VITE_AUTH_MODE=local. NEVER use this against a real backend.

import { configureApiClient } from './services/apiClient.js';
import { openFormDialog } from './ui/dialog.js';
import { toast } from './ui/dom.js';

const STORAGE_KEY = 'eventhub.localAuth';

// Mirrors the seeded users and their authorities in application-local.properties.
const LOCAL_USERS = {
    alice_admin: { password: 'password', roles: ['Admin'], name: 'Alice (Admin)' },
    john_user: { password: 'password', roles: ['User'], name: 'John (User)' },
};

let session = null;

function persist() {
    if (session) {
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    } else {
        sessionStorage.removeItem(STORAGE_KEY);
    }
}

export async function initAuth() {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    session = raw ? JSON.parse(raw) : null;
    configureApiClient({
        getAuthHeader: () => (session ? `Basic ${session.basic}` : null),
    });
}

export async function isAuthenticated() {
    return session !== null;
}

export async function getUser() {
    if (!session) {
        return null;
    }
    return { name: session.name, email: `${session.username}@local`, sub: session.username };
}

export async function getRoles() {
    return session ? session.roles : [];
}

export async function isAdmin() {
    return (await getRoles()).includes('Admin');
}

export async function currentUsername() {
    return session ? session.username : '';
}

export async function login() {
    const values = await openFormDialog({
        title: 'Local sign in (no Auth0)',
        submitLabel: 'Sign in',
        fields: [
            {
                name: 'username',
                label: 'Username',
                required: true,
                hint: 'alice_admin (Admin) or john_user (User)',
            },
            { name: 'password', label: 'Password', type: 'password', required: true },
        ],
        values: { username: 'alice_admin', password: 'password' },
    });
    if (!values) {
        return;
    }
    const user = LOCAL_USERS[values.username];
    if (!user || user.password !== values.password) {
        toast('Invalid local credentials.', 'error');
        return;
    }
    session = {
        username: values.username,
        name: user.name,
        roles: user.roles,
        basic: btoa(`${values.username}:${values.password}`),
    };
    persist();
    // Full reload mirrors the Auth0 redirect flow and re-runs the auth gate.
    window.location.reload();
}

export async function logout() {
    session = null;
    persist();
    window.location.reload();
}
