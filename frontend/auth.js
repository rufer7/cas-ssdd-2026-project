// Authentication facade. Selects the provider based on VITE_AUTH_MODE:
//   - "auth0" (default): real Auth0 OIDC login (auth0Provider.js)
//   - "local": HTTP Basic against the backend `local` profile (localAuthProvider.js)
//
// Views and the app shell import only from here, so switching modes is a config
// change (an env var), not a code change.

import * as auth0Provider from './auth0Provider.js';
import * as localAuthProvider from './localAuthProvider.js';

const provider = (import.meta.env.VITE_AUTH_MODE ?? 'auth0') === 'local'
    ? localAuthProvider
    : auth0Provider;

export const initAuth = (...args) => provider.initAuth(...args);
export const isAuthenticated = (...args) => provider.isAuthenticated(...args);
export const getUser = (...args) => provider.getUser(...args);
export const getRoles = (...args) => provider.getRoles(...args);
export const isAdmin = (...args) => provider.isAdmin(...args);
export const currentUsername = (...args) => provider.currentUsername(...args);
export const login = (...args) => provider.login(...args);
export const logout = (...args) => provider.logout(...args);
