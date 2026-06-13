// Date/time formatting helpers. The backend exchanges LocalDateTime strings
// like "2026-06-01T10:00:00" (no timezone); we render them in the user's locale.

const dateTimeFormat = new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
});

export function formatDateTime(isoLocalDateTime) {
    if (!isoLocalDateTime) {
        return '';
    }
    const date = new Date(isoLocalDateTime);
    return Number.isNaN(date.getTime()) ? isoLocalDateTime : dateTimeFormat.format(date);
}

export function formatDateRange(fromIso, toIso) {
    return `${formatDateTime(fromIso)} – ${formatDateTime(toIso)}`;
}

/**
 * Normalise an <input type="datetime-local"> value ("2026-06-01T10:00") to the
 * seconds-precision LocalDateTime the backend expects ("2026-06-01T10:00:00").
 */
export function toBackendDateTime(localInputValue) {
    if (!localInputValue) {
        return localInputValue;
    }
    return localInputValue.length === 16 ? `${localInputValue}:00` : localInputValue;
}

/** Convert a backend LocalDateTime to a datetime-local input value (drops seconds). */
export function toInputDateTime(isoLocalDateTime) {
    return isoLocalDateTime ? isoLocalDateTime.slice(0, 16) : '';
}
