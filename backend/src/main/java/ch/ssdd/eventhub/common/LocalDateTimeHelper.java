package ch.ssdd.eventhub.common;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class LocalDateTimeHelper {

    private LocalDateTimeHelper() {
    }

    public static LocalDateTime utcNow() {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }
}
