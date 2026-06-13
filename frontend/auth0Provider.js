// Auth0-backed authentication provider (default mode).
//
// Initialises the Auth0 SPA client (with the backend API audience so
// getTokenSilently returns an access token the resource server accepts),
// handles the redirect callback, wires the apiClient Authorization header, and
// exposes role helpers.

import { createAuth0Client } from '@auth0/auth0-spa-js';
import { configureApiClient } from './services/apiClient.js';

// Must match auth0.roles-claim on the backend (Auth0RolesAuthoritiesConverter).
const ROLES_CLAIM = 'https://eventhub.ssdd.ch/roles';

let client;

export async function initAuth() {
    client = await createAuth0Client({
        domain: import.meta.env.VITE_AUTH0_DOMAIN,
        clientId: import.meta.env.VITE_AUTH0_CLIENT_ID,
        authorizationParams: {
            redirect_uri: window.location.origin,
            audience: import.meta.env.VITE_AUTH0_AUDIENCE,
        },
        // Persist the session across reloads (dev convenience).
        cacheLocation: 'localstorage',
        useRefreshTokens: true,
    });

    // Complete the login redirect, then strip the ?code/&state query params.
    const params = new URLSearchParams(window.location.search);
    if (params.has('code') && params.has('state')) {
        await client.handleRedirectCallback();
        window.history.replaceState(
            {}, document.title, window.location.pathname + window.location.hash);
    }

    configureApiClient({
        getAuthHeader: async () => {
            const token = await client.getTokenSilently();
            return token ? `Bearer ${token}` : null;
        },
    });
    return client;
}

export function isAuthenticated() {
    return client.isAuthenticated();
}

export function getUser() {
    return client.getUser();
}

export async function getRoles() {
    const user = await client.getUser();
    const roles = user?.[ROLES_CLAIM];
    return Array.isArray(roles) ? roles : [];
}

export async function isAdmin() {
    return (await getRoles()).includes('Admin');
}

/**
 * The identifier the backend expects as the "username" (the access-token
 * subject). Used for endpoints that still take a username in the body.
 */
export async function currentUsername() {
    const user = await client.getUser();
    return user?.sub ?? '';
}

export function login() {
    return client.loginWithRedirect();
}

export function logout() {
    return client.logout({ logoutParams: { returnTo: window.location.origin } });
}
