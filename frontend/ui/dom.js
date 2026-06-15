// Tiny DOM helpers. All user-derived text goes through textContent (never
// innerHTML), so the views are safe against stored/reflected XSS.

/**
 * Create an element.
 * @param {string} tag
 * @param {Object} [attrs] attribute/property map. Special keys: `class`, `text`,
 *   `dataset`, and `on<Event>` handler functions. Boolean true renders a bare
 *   attribute; null/undefined/false are skipped.
 * @param {(Node|string|null|false|undefined)[]|Node|string} [children]
 */
export function el(tag, attrs = {}, children = []) {
    const node = document.createElement(tag);
    for (const [key, value] of Object.entries(attrs)) {
        if (value === null || value === undefined || value === false) {
            continue;
        }
        if (key === 'class') {
            node.className = value;
        } else if (key === 'text') {
            node.textContent = value;
        } else if (key === 'dataset') {
            Object.assign(node.dataset, value);
        } else if (key.startsWith('on') && typeof value === 'function') {
            node.addEventListener(key.slice(2).toLowerCase(), value);
        } else if (value === true) {
            node.setAttribute(key, '');
        } else {
            node.setAttribute(key, String(value));
        }
    }
    const list = Array.isArray(children) ? children : [children];
    for (const child of list) {
        if (child === null || child === undefined || child === false) {
            continue;
        }
        node.append(child instanceof Node ? child : document.createTextNode(String(child)));
    }
    return node;
}

const mainEl = () => document.getElementById('main');

/** Replace the main region with `node` and move focus there for screen readers. */
export function mount(node) {
    const main = mainEl();
    main.replaceChildren(node);
    main.focus();
}

/** Announce a transient status message politely (e.g. "Loading events…"). */
export function announce(message) {
    const status = document.getElementById('status');
    status.textContent = '';
    requestAnimationFrame(() => {
        status.textContent = message;
    });
}

/** Show a transient toast. `type` is one of 'info' | 'success' | 'error'. */
export function toast(message, type = 'info') {
    const region = document.getElementById('toast-region');
    const node = el('div', { class: `toast toast--${type}`, role: 'alert' }, message);
    region.append(node);
    setTimeout(() => node.remove(), 5000);
}

export function loadingState(label = 'Loading…') {
    announce(label);
    return el('div', { class: 'state', role: 'status' }, [
        el('div', { class: 'spinner', 'aria-hidden': 'true' }),
        el('p', {}, label),
    ]);
}

export function errorState(error) {
    const message = error?.status === 403
        ? 'You do not have permission to perform this action.'
        : (error?.body || error?.message || 'Something went wrong.');
    return el('div', { class: 'state state--error', role: 'alert' }, [
        el('p', {}, 'Could not load this page.'),
        el('p', {}, String(message)),
    ]);
}

export function emptyState(message) {
    return el('div', { class: 'state' }, message);
}
