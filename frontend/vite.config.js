import { defineConfig } from 'vite';

// Dev server config. The /api proxy lets the SPA call the backend same-origin
// (e.g. "/api/events"), so there is no CORS setup needed when running locally —
// including the no-Auth0 `local` profile that uses HTTP Basic.
export default defineConfig({
    server: {
        port: 5173,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
            },
        },
    },
});
