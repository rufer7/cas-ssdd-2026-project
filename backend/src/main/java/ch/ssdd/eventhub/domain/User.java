package ch.ssdd.eventhub.domain;

import ch.ssdd.eventhub.common.LocalDateTimeHelper;
import ch.ssdd.eventhub.domain.common.CommonValidators;
import ch.ssdd.eventhub.domain.common.Constants;
import java.time.LocalDateTime;
import java.util.Objects;

public record User(
        String username,
        String externalId,
        Role role,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {

    public User {
        Objects.requireNonNull(username, "Username cannot be null");
        Objects.requireNonNull(externalId, "External ID cannot be null");
        Objects.requireNonNull(role, "Role cannot be null");
        Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        Objects.requireNonNull(modifiedAt, "ModifiedAt cannot be null");

        if (username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }

        if (externalId.isBlank()) {
            throw new IllegalArgumentException("External ID cannot be blank");
        }

        if (!CommonValidators.isValidStringLength(username)) {
            throw new IllegalArgumentException(
                    "Username cannot exceed " + Constants.DEFAULT_MAX_STRING_LENGTH + " characters");
        }

        if (!CommonValidators.isValidStringLength(externalId)) {
            throw new IllegalArgumentException(
                    "External ID cannot exceed " + Constants.DEFAULT_MAX_STRING_LENGTH + " characters");
        }

        if (!CommonValidators.isCreatedAtBeforeModifiedAt(createdAt, modifiedAt)) {
            throw new IllegalArgumentException("CreatedAt must be before or equal to ModifiedAt");
        }

        if (!CommonValidators.isNotInFuture(createdAt)) {
            throw new IllegalArgumentException("CreatedAt cannot be in the future");
        }
    }

    public static User createNewProvisionedAdminUser(String username) {
        return new User(username, username, Role.ADMIN, LocalDateTimeHelper.utcNow(), LocalDateTimeHelper.utcNow());
    }
}
