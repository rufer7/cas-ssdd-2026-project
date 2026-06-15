package ch.ssdd.eventhub.domain.command;

import java.time.LocalDateTime;
import java.util.Objects;

public record CreateEventCommand(
        String title,
        String description,
        LocalDateTime from,
        LocalDateTime to,
        String location,
        String username
) {
    public CreateEventCommand {
        Objects.requireNonNull(title, "Title is required");
        Objects.requireNonNull(from, "Start date is required");
        Objects.requireNonNull(to, "End date is required");
        Objects.requireNonNull(location, "Location is required");
        Objects.requireNonNull(username, "Username is required");
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }
}
