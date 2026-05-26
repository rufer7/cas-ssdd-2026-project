package ch.ssdd.eventhub.domain;

import java.time.LocalDateTime;

public class CommonValidators {
    static boolean isValidStringLength(String str) {
        return str.length() <= Constants.DEFAULT_MAX_STRING_LENGTH;
    }

    static boolean isCreatedAtBeforeModifiedAt(LocalDateTime createdAt, LocalDateTime modifiedAt) {
        return createdAt.isBefore(modifiedAt) || createdAt.isEqual(modifiedAt);
    }

    static boolean isCreatedAtNotInFuture(LocalDateTime createdAt) {
        return !createdAt.isAfter(LocalDateTime.now());
    }
}
