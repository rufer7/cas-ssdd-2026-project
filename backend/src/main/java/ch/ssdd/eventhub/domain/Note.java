package ch.ssdd.eventhub.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import ch.ssdd.eventhub.domain.common.CommonValidators;
import ch.ssdd.eventhub.domain.common.Constants;

public record Note(
        String content,
        User createdBy,
        LocalDateTime createdAt,
        User modifiedBy,
        LocalDateTime modifiedAt) {

    public Note {
        Objects.requireNonNull(content, "Note content cannot be null");
        Objects.requireNonNull(createdBy, "Note createdBy cannot be null");
        Objects.requireNonNull(createdAt, "Note createdAt cannot be null");
        Objects.requireNonNull(modifiedBy, "Note modifiedBy cannot be null");
        Objects.requireNonNull(modifiedAt, "Note modifiedAt cannot be null");

        if (content.isBlank()) {
            throw new IllegalArgumentException("Note content cannot be blank");
        }

        if (!CommonValidators.isValidStringLength(content)) {
            throw new IllegalArgumentException(
                    "Note content cannot exceed " + Constants.DEFAULT_MAX_STRING_LENGTH + " characters");
        }

        if (!CommonValidators.isCreatedAtBeforeModifiedAt(createdAt, modifiedAt)) {
            throw new IllegalArgumentException("Note createdAt must be before or equal to modifiedAt");
        }

        if (!CommonValidators.isNotInFuture(createdAt)) {
            throw new IllegalArgumentException("Note createdAt cannot be in the future");
        }
    }
}
