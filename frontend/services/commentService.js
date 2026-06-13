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
     * @param {string} eventId
     * @param {{ content: string, username: string }} comment
     *   `username` is still required by the API (transitional; will move to the token).
     * @returns {Promise<CommentResponse>}
     */
    addComment(eventId, { content, username }) {
        return apiClient.post(commentsPath(eventId), { content, username });
    },
};
