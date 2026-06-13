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
     * @param {{ content: string, username: string }} note
     *   `username` is still required by the API (transitional; will move to the token).
     * @returns {Promise<NoteResponse>}
     */
    createNote({ content, username }) {
        return apiClient.post(NOTES_PATH, { content, username });
    },
};
