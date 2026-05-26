package ch.ssdd.eventhub.domain.common;

import java.time.LocalDateTime;

import ch.ssdd.eventhub.domain.Constants;

public class CommonValidators {
    static boolean isValidStringLength(String str) {
        return str.length() <= Constants.DEFAULT_MAX_STRING_LENGTH;
    }

    static boolean isNotInFuture(LocalDateTime dateTime) {
        return !dateTime.isAfter(LocalDateTime.now());
    }

    static boolean isCreatedAtBeforeModifiedAt(LocalDateTime createdAt, LocalDateTime modifiedAt) {
        return createdAt.isBefore(modifiedAt) || createdAt.isEqual(modifiedAt);
    }
}
