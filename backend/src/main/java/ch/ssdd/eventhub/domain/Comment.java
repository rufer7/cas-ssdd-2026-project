package ch.ssdd.eventhub.domain;

import ch.ssdd.eventhub.domain.common.CommonValidators;
import ch.ssdd.eventhub.domain.common.Constants;
import java.time.LocalDateTime;
import java.util.Objects;

public record Comment(
        String content,
        User createdBy,
        LocalDateTime createdAt,
        User modifiedBy,
        LocalDateTime modifiedAt) {

    public Comment {
        Objects.requireNonNull(content, "Comment content cannot be null");
        Objects.requireNonNull(createdBy, "Comment createdBy cannot be null");
        Objects.requireNonNull(createdAt, "Comment createdAt cannot be null");
        Objects.requireNonNull(modifiedBy, "Comment modifiedBy cannot be null");
        Objects.requireNonNull(modifiedAt, "Comment modifiedAt cannot be null");

        if (content.isBlank()) {
            throw new IllegalArgumentException("Comment content cannot be blank");
        }

        if (!CommonValidators.isValidStringLength(content)) {
            throw new IllegalArgumentException(
                    "Comment content cannot exceed " + Constants.DEFAULT_MAX_STRING_LENGTH + " characters");
        }

        if (!CommonValidators.isCreatedAtBeforeModifiedAt(createdAt, modifiedAt)) {
            throw new IllegalArgumentException("Comment createdAt must be before or equal to modifiedAt");
        }

        if (!CommonValidators.isNotInFuture(createdAt)) {
            throw new IllegalArgumentException("Comment createdAt cannot be in the future");
        }
    }
}
