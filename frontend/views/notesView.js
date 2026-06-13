import { noteService } from '../services/noteService.js';
import { currentUsername } from '../auth.js';
import { el, emptyState, toast } from '../ui/dom.js';
import { formatDateTime } from '../ui/format.js';

function noteItem(note) {
    return el('li', { class: 'note' }, [
        el('div', { class: 'note__head' }, [
            el('span', {}, formatDateTime(note.createdAt)),
        ]),
        el('p', { class: 'note__body' }, note.content),
    ]);
}

export async function renderNotesView() {
    const listContainer = el('div', {});

    async function refresh() {
        const notes = await noteService.getNotesByUser();
        listContainer.replaceChildren(
            notes.length
                ? el('ul', { class: 'note-list' }, notes.map(noteItem))
                : emptyState('You have no notes yet.'));
    }

    const contentInput = el('textarea', {
        id: 'note-content',
        class: 'input',
        name: 'content',
        rows: 3,
        required: true,
        maxlength: 255,
        placeholder: 'Write a private note…',
    });
    const form = el('form', { class: 'form' }, [
        el('div', { class: 'form__group' }, [
            el('label', { class: 'form__label', for: 'note-content' }, 'New note'),
            contentInput,
            el('span', { class: 'field-hint' }, 'Notes are private to your account.'),
        ]),
        el('div', { class: 'form__actions' }, [
            el('button', { type: 'submit', class: 'btn btn--primary' }, 'Save note'),
        ]),
    ]);
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const content = contentInput.value.trim();
        if (!content) {
            return;
        }
        try {
            await noteService.createNote({ content, username: await currentUsername() });
            contentInput.value = '';
            toast('Note saved.', 'success');
            await refresh();
        } catch (err) {
            toast(String(err.body || err.message), 'error');
        }
    });

    const container = el('div', {}, [
        el('div', { class: 'page-header' }, [el('h1', {}, 'My notes')]),
        el('section', { class: 'section' }, [form]),
        listContainer,
    ]);

    await refresh();
    return container;
}
