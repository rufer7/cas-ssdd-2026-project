// Service for the comments API (ch.ssdd.eventhub.adapters.inbound.rest.CommentRestController).
//
// Comments are a sub-resource of an event.
// Endpoints:
//   GET  /api/events/{eventId}/comments  (authenticated)
//   POST /api/events/{eventId}/comments  (authenticated)

import { apiClient } from './apiClient.js';

const commentsPath = (eventId) => `/api/events/${eventId}/comments`;

/**
 * @typedef {Object} CommentResponse
 * @property {string} content
 * @property {string} createdBy username
 * @property {string} createdAt ISO-8601 local date-time
 */

export const commentService = {
    /**
     * @param {string} eventId
     * @returns {Promise<CommentResponse[]>}
     */
    getCommentsByEvent(eventId) {
        return apiClient.get(commentsPath(eventId));
    },

    /**
     * The author is taken from the authenticated principal server-side.
     * @param {string} eventId
     * @param {{ content: string }} comment
     * @returns {Promise<CommentResponse>}
     */
    addComment(eventId, { content }) {
        return apiClient.post(commentsPath(eventId), { content });
    },
};
