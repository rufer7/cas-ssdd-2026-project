package ch.ssdd.eventhub.domain.common;

import java.time.LocalDateTime;

public class CommonValidators {

    private CommonValidators() {
        // Private constructor to prevent instantiation
    }

    public static boolean isValidStringLength(String str) {
        return str.length() <= Constants.DEFAULT_MAX_STRING_LENGTH;
    }

    public static boolean isNotInFuture(LocalDateTime dateTime) {
        return !dateTime.isAfter(LocalDateTime.now());
    }

    public static boolean isCreatedAtBeforeModifiedAt(LocalDateTime createdAt, LocalDateTime modifiedAt) {
        return createdAt.isBefore(modifiedAt) || createdAt.isEqual(modifiedAt);
    }
}
