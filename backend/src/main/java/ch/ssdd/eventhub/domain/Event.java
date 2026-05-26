package ch.ssdd.eventhub.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.ssdd.eventhub.domain.common.CommonValidators;
import ch.ssdd.eventhub.domain.common.Constants;

public record Event(
        String title,
        String description,
        LocalDateTime from,
        LocalDateTime to,
        String location,
        User createdBy,
        LocalDateTime createdAt,
        User modifiedBy,
        LocalDateTime modifiedAt,
        byte[] featuredImage,
        List<Comment> comments) {

    public Event(
            String title,
            String description,
            LocalDateTime from,
            LocalDateTime to,
            String location,
            User createdBy,
            LocalDateTime createdAt,
            User modifiedBy,
            LocalDateTime modifiedAt,
            byte[] featuredImage) {
        this(title, description, from, to, location, createdBy, createdAt, modifiedBy, modifiedAt, featuredImage,
                List.of());
    }

    public Event {
        Objects.requireNonNull(title, "Event title cannot be null");
        Objects.requireNonNull(description, "Event description cannot be null");
        Objects.requireNonNull(from, "Event from date cannot be null");
        Objects.requireNonNull(to, "Event to date cannot be null");
        Objects.requireNonNull(location, "Event location cannot be null");
        Objects.requireNonNull(createdBy, "Event createdBy cannot be null");
        Objects.requireNonNull(createdAt, "Event createdAt cannot be null");
        Objects.requireNonNull(modifiedBy, "Event modifiedBy cannot be null");
        Objects.requireNonNull(modifiedAt, "Event modifiedAt cannot be null");

        comments = comments == null ? List.of() : List.copyOf(comments);

        validate();
    }

    public Event addComment(Comment comment) {
        Objects.requireNonNull(comment, "Comment cannot be null");

        var updatedComments = new ArrayList<>(comments);
        updatedComments.add(comment);

        return new Event(
                title,
                description,
                from,
                to,
                location,
                createdBy,
                createdAt,
                modifiedBy,
                modifiedAt,
                featuredImage,
                updatedComments);
    }

    public Event removeComment(Comment comment) {
        Objects.requireNonNull(comment, "Comment cannot be null");

        if (!comments.contains(comment)) {
            return this;
        }

        var updatedComments = new ArrayList<>(comments);
        updatedComments.remove(comment);

        return new Event(
                title,
                description,
                from,
                to,
                location,
                createdBy,
                createdAt,
                modifiedBy,
                modifiedAt,
                featuredImage,
                updatedComments);
    }

    private void validate() {
        if (title.isBlank()) {
            throw new IllegalArgumentException("Event title cannot be blank");
        }

        if (location.isBlank()) {
            throw new IllegalArgumentException("Event location cannot be blank");
        }

        if (!CommonValidators.isValidStringLength(title)) {
            throw new IllegalArgumentException(
                    "Event title cannot exceed " + Constants.DEFAULT_MAX_STRING_LENGTH + " characters");
        }

        if (!CommonValidators.isValidStringLength(description)) {
            throw new IllegalArgumentException(
                    "Event description cannot exceed " + Constants.DEFAULT_MAX_STRING_LENGTH + " characters");
        }

        if (!CommonValidators.isValidStringLength(location)) {
            throw new IllegalArgumentException(
                    "Event location cannot exceed " + Constants.DEFAULT_MAX_STRING_LENGTH + " characters");
        }

        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("Event from date must be before to date");
        }

        if (!CommonValidators.isCreatedAtBeforeModifiedAt(createdAt, modifiedAt)) {
            throw new IllegalArgumentException("Event createdAt must be before or equal to modifiedAt");
        }

        if (!CommonValidators.isNotInFuture(createdAt)) {
            throw new IllegalArgumentException("Event createdAt cannot be in the future");
        }

        for (var comment : comments) {
            if (comment == null) {
                throw new IllegalArgumentException("Event comments cannot contain null elements");
            }
        }
    }
}
