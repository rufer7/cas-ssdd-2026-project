import { eventService } from '../services/eventService.js';
import { commentService } from '../services/commentService.js';
import { isAdmin } from '../auth.js';
import { el, emptyState, toast } from '../ui/dom.js';
import { formatDateTime, formatDateRange, toInputDateTime } from '../ui/format.js';
import { openFormDialog } from '../ui/dialog.js';
import { eventFormFields, toEventPayload } from './eventsView.js';
import { navigate, reload } from '../router.js';

function commentItem(comment) {
    return el('li', { class: 'comment' }, [
        el('div', { class: 'comment__head' }, [
            el('span', { class: 'comment__author' }, comment.createdBy),
            el('span', {}, formatDateTime(comment.createdAt)),
        ]),
        el('p', { class: 'comment__body' }, comment.content),
    ]);
}

export async function renderEventDetailView(id) {
    // There is no GET /api/events/{id}; resolve the event from the list.
    const events = await eventService.getAllEvents();
    const event = events.find((candidate) => candidate.eventId === id);

    if (!event) {
        return el('div', {}, [
            el('a', { class: 'back-link', href: '#/events' }, '← Back to events'),
            emptyState('This event could not be found.'),
        ]);
    }

    const admin = await isAdmin();

    async function editEvent() {
        const values = await openFormDialog({
            title: 'Edit event',
            submitLabel: 'Save changes',
            fields: eventFormFields,
            values: {
                title: event.title,
                description: event.description,
                from: toInputDateTime(event.from),
                to: toInputDateTime(event.to),
                location: event.location,
            },
        });
        if (!values) {
            return;
        }
        try {
            await eventService.updateEvent(id, toEventPayload(values));
            toast('Event updated.', 'success');
            reload();
        } catch (err) {
            toast(err.status === 403 ? 'Only admins can edit events.' : String(err.body || err.message), 'error');
        }
    }

    async function deleteEvent() {
        if (!window.confirm(`Delete “${event.title}”? This cannot be undone.`)) {
            return;
        }
        try {
            await eventService.deleteEvent(id);
            toast('Event deleted.', 'success');
            navigate('/events');
        } catch (err) {
            toast(err.status === 403 ? 'Only admins can delete events.' : String(err.body || err.message), 'error');
        }
    }

    const adminActions = admin
        ? el('div', { class: 'toolbar' }, [
            el('button', { type: 'button', class: 'btn', onclick: editEvent }, 'Edit'),
            el('button', { type: 'button', class: 'btn btn--danger', onclick: deleteEvent }, 'Delete'),
        ])
        : null;

    const detailSection = el('section', { class: 'section', 'aria-labelledby': 'event-title' }, [
        el('div', { class: 'page-header' }, [
            el('h1', { id: 'event-title' }, event.title),
            adminActions,
        ]),
        el('dl', { class: 'meta-list' }, [
            el('dt', {}, 'When'),
            el('dd', {}, formatDateRange(event.from, event.to)),
            el('dt', {}, 'Where'),
            el('dd', {}, event.location),
            el('dt', {}, 'Description'),
            el('dd', {}, event.description || '—'),
        ]),
        admin ? uploadImageForm(id) : null,
    ]);

    const commentsSection = await commentsSectionFor(id);

    return el('div', {}, [
        el('a', { class: 'back-link', href: '#/events' }, '← Back to events'),
        detailSection,
        commentsSection,
    ]);
}

function uploadImageForm(eventId) {
    const fileInput = el('input', {
        id: 'featured-image',
        class: 'input',
        type: 'file',
        name: 'file',
        accept: '.jpg,.jpeg,.png,image/png,image/jpeg',
        required: true,
    });
    const form = el('form', { class: 'form__inline' }, [
        el('div', { class: 'form__group' }, [
            el('label', { class: 'form__label', for: 'featured-image' }, 'Featured image'),
            fileInput,
            el('span', { class: 'field-hint' }, 'JPG or PNG, up to 4 MB.'),
        ]),
        el('button', { type: 'submit', class: 'btn' }, 'Upload'),
    ]);
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const file = fileInput.files?.[0];
        if (!file) {
            return;
        }
        try {
            await eventService.uploadFeaturedImage(eventId, file);
            toast('Image uploaded.', 'success');
            form.reset();
        } catch (err) {
            toast(err.status === 400 ? 'Invalid file.' : String(err.body || err.message), 'error');
        }
    });
    return form;
}

async function commentsSectionFor(eventId) {
    const listContainer = el('div', {});

    async function refresh() {
        const comments = await commentService.getCommentsByEvent(eventId);
        listContainer.replaceChildren(
            comments.length
                ? el('ul', { class: 'comment-list' }, comments.map(commentItem))
                : emptyState('No comments yet. Be the first to comment.'));
    }

    const contentInput = el('textarea', {
        id: 'comment-content',
        class: 'input',
        name: 'content',
        rows: 3,
        required: true,
        maxlength: 255,
        placeholder: 'Write a comment…',
    });
    const form = el('form', { class: 'form' }, [
        el('div', { class: 'form__group' }, [
            el('label', { class: 'form__label', for: 'comment-content' }, 'Add a comment'),
            contentInput,
        ]),
        el('div', { class: 'form__actions' }, [
            el('button', { type: 'submit', class: 'btn btn--primary' }, 'Post comment'),
        ]),
    ]);
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const content = contentInput.value.trim();
        if (!content) {
            return;
        }
        try {
            await commentService.addComment(eventId, { content });
            contentInput.value = '';
            toast('Comment posted.', 'success');
            await refresh();
        } catch (err) {
            toast(String(err.body || err.message), 'error');
        }
    });

    await refresh();

    return el('section', { class: 'section', 'aria-labelledby': 'comments-title' }, [
        el('h2', { id: 'comments-title' }, 'Comments'),
        form,
        listContainer,
    ]);
}
