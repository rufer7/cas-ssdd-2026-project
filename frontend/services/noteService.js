// Service for the notes API (ch.ssdd.eventhub.adapters.inbound.rest.NoteRestController).
//
// Notes belong to the authenticated user.
// Endpoints:
//   GET  /api/notes  (authenticated; returns the current user's notes)
//   POST /api/notes  (authenticated)

import { apiClient } from './apiClient.js';

const NOTES_PATH = '/api/notes';

/**
 * @typedef {Object} NoteResponse
 * @property {string} content
 * @property {string} createdBy username
 * @property {string} createdAt ISO-8601 local date-time
 */

export const noteService = {
    /**
     * Returns the notes owned by the currently authenticated user.
     * @returns {Promise<NoteResponse[]>}
     */
    getNotesByUser() {
        return apiClient.get(NOTES_PATH);
    },

    /**
     * The owner is taken from the authenticated principal server-side.
     * @param {{ content: string }} note
     * @returns {Promise<NoteResponse>}
     */
    createNote({ content }) {
        return apiClient.post(NOTES_PATH, { content });
    },
};
