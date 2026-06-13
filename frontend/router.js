// Minimal dependency-free hash router.
//
// Patterns use ':' for params, e.g. register('/events/:id', handler).
// Handlers receive an object of decoded params.

const routes = [];
let renderFn = () => {};

export function register(pattern, handler) {
    routes.push({ parts: pattern.split('/').filter(Boolean), handler });
}

function matchRoute(path) {
    const segments = path.split('/').filter(Boolean);
    for (const route of routes) {
        if (route.parts.length !== segments.length) {
            continue;
        }
        const params = {};
        let matched = true;
        for (let i = 0; i < route.parts.length; i += 1) {
            const part = route.parts[i];
            if (part.startsWith(':')) {
                params[part.slice(1)] = decodeURIComponent(segments[i]);
            } else if (part !== segments[i]) {
                matched = false;
                break;
            }
        }
        if (matched) {
            return { handler: route.handler, params };
        }
    }
    return null;
}

export function currentPath() {
    return window.location.hash.replace(/^#/, '') || '/events';
}

export function navigate(path) {
    if (window.location.hash === `#${path}`) {
        reload();
    } else {
        window.location.hash = path;
    }
}

export function reload() {
    return renderFn();
}

export function start(onNotFound) {
    renderFn = async () => {
        const match = matchRoute(currentPath());
        if (match) {
            await match.handler(match.params);
        } else {
            onNotFound();
        }
    };
    window.addEventListener('hashchange', renderFn);
    return renderFn;
}
