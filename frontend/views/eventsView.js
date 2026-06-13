import { eventService } from '../services/eventService.js';
import { isAdmin } from '../auth.js';
import { el, emptyState, toast } from '../ui/dom.js';
import { formatDateRange, toBackendDateTime } from '../ui/format.js';
import { openFormDialog } from '../ui/dialog.js';

export const eventFormFields = [
    { name: 'title', label: 'Title', required: true, maxlength: 255 },
    { name: 'description', label: 'Description', type: 'textarea', maxlength: 255 },
    { name: 'from', label: 'Starts', type: 'datetime-local', required: true },
    { name: 'to', label: 'Ends', type: 'datetime-local', required: true },
    { name: 'location', label: 'Location', required: true, maxlength: 255 },
];

export function toEventPayload(formValues) {
    return {
        title: formValues.title,
        description: formValues.description,
        from: toBackendDateTime(formValues.from),
        to: toBackendDateTime(formValues.to),
        location: formValues.location,
    };
}

function eventCard(event) {
    return el('li', { class: 'card' }, [
        el('h2', { class: 'card__title' }, [
            el('a', { href: `#/events/${encodeURIComponent(event.eventId)}` }, event.title),
        ]),
        el('div', { class: 'card__meta' }, [
            el('span', {}, formatDateRange(event.from, event.to)),
            el('span', {}, event.location),
        ]),
        event.description ? el('p', { class: 'card__desc' }, event.description) : null,
    ]);
}

export async function renderEventsView() {
    const admin = await isAdmin();

    const listContainer = el('div', { 'aria-busy': 'true' });

    async function refresh(query) {
        listContainer.setAttribute('aria-busy', 'true');
        const events = query
            ? await eventService.searchEvents(query)
            : await eventService.getAllEvents();
        listContainer.setAttribute('aria-busy', 'false');
        if (!events.length) {
            listContainer.replaceChildren(
                emptyState(query ? `No events match “${query}”.` : 'No events yet.'));
            return;
        }
        listContainer.replaceChildren(
            el('ul', { class: 'card-grid' }, events.map(eventCard)));
    }

    const searchInput = el('input', {
        id: 'event-search',
        class: 'input',
        type: 'search',
        name: 'query',
        placeholder: 'Search events…',
    });
    const searchForm = el('form', { class: 'form__inline', role: 'search' }, [
        el('div', { class: 'form__group' }, [
            el('label', { class: 'form__label', for: 'event-search' }, 'Search events'),
            searchInput,
        ]),
        el('button', { type: 'submit', class: 'btn' }, 'Search'),
    ]);
    searchForm.addEventListener('submit', (event) => {
        event.preventDefault();
        refresh(searchInput.value.trim()).catch((err) => toast(String(err.body || err.message), 'error'));
    });

    async function createEvent() {
        const values = await openFormDialog({
            title: 'New event',
            submitLabel: 'Create event',
            fields: eventFormFields,
        });
        if (!values) {
            return;
        }
        try {
            await eventService.createEvent(toEventPayload(values));
            toast('Event created.', 'success');
            await refresh(searchInput.value.trim());
        } catch (err) {
            toast(err.status === 403 ? 'Only admins can create events.' : String(err.body || err.message), 'error');
        }
    }

    const toolbar = el('div', { class: 'toolbar' }, [
        searchForm,
        admin
            ? el('button', { type: 'button', class: 'btn btn--primary', onclick: createEvent }, '+ New event')
            : null,
    ]);

    const container = el('div', {}, [
        el('div', { class: 'page-header' }, [el('h1', {}, 'Events'), toolbar]),
        listContainer,
    ]);

    await refresh('');
    return container;
}
