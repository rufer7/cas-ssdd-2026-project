// Accessible modal form built on the native <dialog> element, which provides
// focus trapping, Escape-to-close and focus restoration for free.

import { el } from './dom.js';

let dialogCounter = 0;

/**
 * Open a modal form and resolve with the field values, or null if cancelled.
 *
 * @param {Object} options
 * @param {string} options.title
 * @param {string} [options.submitLabel]
 * @param {{ name: string, label: string, type?: string, required?: boolean,
 *           maxlength?: number, hint?: string }[]} options.fields
 * @param {Object} [options.values] initial values keyed by field name
 * @returns {Promise<Object|null>}
 */
export function openFormDialog({ title, submitLabel = 'Save', fields, values = {} }) {
    return new Promise((resolve) => {
        dialogCounter += 1;
        const titleId = `dialog-title-${dialogCounter}`;
        const inputs = {};

        const groups = fields.map((field) => {
            const inputId = `dialog-field-${dialogCounter}-${field.name}`;
            const label = el('label', { class: 'form__label', for: inputId }, field.label);
            const common = {
                id: inputId,
                name: field.name,
                class: 'input',
                required: field.required === true,
                maxlength: field.maxlength,
            };
            const input = field.type === 'textarea'
                ? el('textarea', { ...common, rows: 3 })
                : el('input', { ...common, type: field.type || 'text' });
            if (values[field.name] != null) {
                input.value = values[field.name];
            }
            inputs[field.name] = input;
            return el('div', { class: 'form__group' }, [
                label,
                input,
                field.hint ? el('span', { class: 'field-hint' }, field.hint) : null,
            ]);
        });

        const cancelBtn = el(
            'button',
            { type: 'button', class: 'btn btn--ghost', onclick: () => finish(null) },
            'Cancel',
        );
        const submitBtn = el('button', { type: 'submit', class: 'btn btn--primary' }, submitLabel);

        const form = el('form', { class: 'form', method: 'dialog' }, [
            el('h2', { id: titleId, class: 'dialog__title' }, title),
            ...groups,
            el('div', { class: 'form__actions' }, [cancelBtn, submitBtn]),
        ]);

        const dialog = el('dialog', { class: 'dialog', 'aria-labelledby': titleId }, form);

        form.addEventListener('submit', () => {
            const result = {};
            for (const field of fields) {
                result[field.name] = inputs[field.name].value;
            }
            finish(result);
        });
        // Escape key fires a `cancel` event on the dialog.
        dialog.addEventListener('cancel', (event) => {
            event.preventDefault();
            finish(null);
        });

        function finish(result) {
            if (dialog.open) {
                dialog.close();
            }
            dialog.remove();
            resolve(result);
        }

        document.body.append(dialog);
        dialog.showModal();
    });
}
