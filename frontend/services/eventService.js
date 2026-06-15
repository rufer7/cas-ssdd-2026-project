// Service for the events API (ch.ssdd.eventhub.adapters.inbound.rest.EventRestController).
//
// Endpoints:
//   GET    /api/events                          (Admin or User)
//   GET    /api/events/search?query=...         (Admin or User)
//   POST   /api/events                          (Admin)
//   PUT    /api/events/{id}                     (Admin)
//   DELETE /api/events/{id}                     (Admin)
//   POST   /api/events/{id}/uploadFeaturedImage (Admin)

import { apiClient } from './apiClient.js';

const EVENTS_PATH = '/api/events';

/**
 * @typedef {Object} EventResponse
 * @property {string} eventId
 * @property {string} title
 * @property {string} description
 * @property {string} from  ISO-8601 local date-time
 * @property {string} to    ISO-8601 local date-time
 * @property {string} location
 */

/**
 * @typedef {Object} EventInput
 * @property {string} title
 * @property {string} description
 * @property {string} from  ISO-8601 local date-time, e.g. "2026-06-01T10:00:00"
 * @property {string} to    ISO-8601 local date-time
 * @property {string} location
 */

export const eventService = {
    /** @returns {Promise<EventResponse[]>} */
    getAllEvents() {
        return apiClient.get(EVENTS_PATH);
    },

    /**
     * @param {string} query free-text search term (sanitized server-side)
     * @returns {Promise<EventResponse[]>}
     */
    searchEvents(query) {
        return apiClient.get(`${EVENTS_PATH}/search?query=${encodeURIComponent(query)}`);
    },

    /**
     * Admin only. The creator is derived from the authenticated principal server-side.
     * @param {EventInput} event
     * @returns {Promise<EventResponse>}
     */
    createEvent(event) {
        return apiClient.post(EVENTS_PATH, event);
    },

    /**
     * Admin only.
     * @param {string} id event id
     * @param {EventInput} event
     * @returns {Promise<EventResponse>}
     */
    updateEvent(id, event) {
        return apiClient.put(`${EVENTS_PATH}/${id}`, event);
    },

    /**
     * Admin only.
     * @param {string} id event id
     * @returns {Promise<null>}
     */
    deleteEvent(id) {
        return apiClient.delete(`${EVENTS_PATH}/${id}`);
    },

    /**
     * Admin only. Uploads a featured image (.jpg/.jpeg/.png, max 4 MB).
     * @param {string} id event id
     * @param {File} file
     * @returns {Promise<string>}
     */
    uploadFeaturedImage(id, file) {
        const formData = new FormData();
        formData.append('file', file);
        return apiClient.postMultipart(`${EVENTS_PATH}/${id}/uploadFeaturedImage`, formData);
    },
};
