// Application shell: bootstraps Auth0, builds the header, and drives the
// hash router with an authentication gate.

import { initAuth, isAuthenticated, getUser, isAdmin, login, logout } from './auth.js';
import * as router from './router.js';
import { el, mount, loadingState, errorState } from './ui/dom.js';
import { renderEventsView } from './views/eventsView.js';
import { renderEventDetailView } from './views/eventDetailView.js';
import { renderNotesView } from './views/notesView.js';
import { renderLoginView } from './views/loginView.js';

function navLink(path, label) {
    const attrs = { class: 'nav__link', href: `#${path}` };
    if (router.currentPath() === path) {
        attrs['aria-current'] = 'page';
    }
    return el('a', attrs, label);
}

function renderNav(authenticated) {
    const nav = document.getElementById('nav');
    nav.replaceChildren(
        ...(authenticated ? [navLink('/events', 'Events'), navLink('/notes', 'Notes')] : []));
}

async function renderAuthControls(authenticated) {
    const controls = document.getElementById('auth-controls');
    if (!authenticated) {
        controls.replaceChildren(
            el('button', { type: 'button', class: 'btn btn--primary', onclick: () => login() }, 'Log in'));
        return;
    }
    const [user, admin] = await Promise.all([getUser(), isAdmin()]);
    controls.replaceChildren(
        el('span', { class: 'user-badge' }, [
            el('span', { class: 'user-badge__name' }, user?.name || user?.email || 'Account'),
            admin ? el('span', { class: 'badge' }, 'Admin') : null,
        ]),
        el('button', { type: 'button', class: 'btn btn--ghost', onclick: () => logout() }, 'Log out'));
}

async function updateHeader() {
    const authenticated = await isAuthenticated();
    renderNav(authenticated);
    await renderAuthControls(authenticated);
}

async function guard(viewFactory) {
    await updateHeader();
    if (!(await isAuthenticated())) {
        mount(renderLoginView());
        return;
    }
    mount(loadingState());
    try {
        mount(await viewFactory());
    } catch (err) {
        mount(errorState(err));
    }
}

async function boot() {
    try {
        await initAuth();
    } catch (err) {
        console.error('Auth initialisation failed', err);
        mount(errorState({
            message: 'Authentication could not be initialised. Check the VITE_AUTH0_* settings in .env.local.',
        }));
        return;
    }

    router.register('/events', () => guard(renderEventsView));
    router.register('/events/:id', ({ id }) => guard(() => renderEventDetailView(id)));
    router.register('/notes', () => guard(renderNotesView));

    const render = router.start(() => router.navigate('/events'));
    await render();
}

boot();
