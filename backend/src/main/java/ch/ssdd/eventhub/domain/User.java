package ch.ssdd.eventhub.domain;

import java.util.Objects;

public record User(
        String username,
        String externalId,
        Role role) {

    public User {
        Objects.requireNonNull(username, "Username cannot be null");
        Objects.requireNonNull(externalId, "External ID cannot be null");
        Objects.requireNonNull(role, "Role cannot be null");

        validate();
    }

    private void validate() {
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
    }
}
