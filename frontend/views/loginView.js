import { login } from '../auth.js';
import { el } from '../ui/dom.js';

export function renderLoginView() {
    return el('div', { class: 'landing' }, [
        el('h1', {}, 'Welcome to EventHub'),
        el('p', {}, 'Browse and discuss events, and keep private notes. Sign in with Auth0 to continue.'),
        el(
            'button',
            { type: 'button', class: 'btn btn--primary', onclick: () => login() },
            'Log in',
        ),
    ]);
}
