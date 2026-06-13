// Shared HTTP client for the eventhub REST API.
//
// Responsibilities:
//  - prefix every call with the configured API base URL
//  - attach the Auth0 bearer token (the backend is a stateless resource server)
//  - serialize/deserialize JSON and surface non-2xx responses as ApiError
//
// The token is obtained through a provider configured at startup (see app.js) so
// this module stays decoupled from the Auth0 client.

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

let tokenProvider = async () => null;

/**
 * Error thrown for any non-2xx response. Carries the HTTP status and the raw
 * response body so callers can react (e.g. 401 -> prompt login, 403 -> hide action).
 */
export class ApiError extends Error {
    constructor(status, body) {
        super(`API request failed with status ${status}`);
        this.name = 'ApiError';
        this.status = status;
        this.body = body;
    }
}

/**
 * Configure how the client obtains an access token.
 * @param {{ getToken: () => Promise<string|null> }} options
 */
export function configureApiClient({ getToken }) {
    tokenProvider = getToken;
}

async function buildHeaders(extraHeaders) {
    const headers = { ...extraHeaders };
    const token = await tokenProvider();
    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }
    return headers;
}

async function parseResponse(response) {
    if (!response.ok) {
        const body = await response.text().catch(() => '');
        throw new ApiError(response.status, body);
    }
    if (response.status === 204) {
        return null;
    }
    const contentType = response.headers.get('content-type') ?? '';
    return contentType.includes('application/json')
        ? response.json()
        : response.text();
}

async function requestJson(path, { method = 'GET', body } = {}) {
    const headers = await buildHeaders(
        body !== undefined ? { 'Content-Type': 'application/json' } : {});
    const response = await fetch(`${API_BASE_URL}${path}`, {
        method,
        headers,
        body: body !== undefined ? JSON.stringify(body) : undefined,
    });
    return parseResponse(response);
}

export const apiClient = {
    get: (path) => requestJson(path, { method: 'GET' }),
    post: (path, body) => requestJson(path, { method: 'POST', body }),
    put: (path, body) => requestJson(path, { method: 'PUT', body }),
    delete: (path) => requestJson(path, { method: 'DELETE' }),

    /**
     * POST multipart/form-data (used for file uploads). The browser sets the
     * Content-Type (with boundary) automatically, so it must not be set here.
     */
    async postMultipart(path, formData) {
        const headers = await buildHeaders();
        const response = await fetch(`${API_BASE_URL}${path}`, {
            method: 'POST',
            headers,
            body: formData,
        });
        return parseResponse(response);
    },
};
