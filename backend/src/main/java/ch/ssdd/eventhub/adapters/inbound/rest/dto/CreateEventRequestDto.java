package ch.ssdd.eventhub.adapters.inbound.rest.dto;

import ch.ssdd.eventhub.adapters.inbound.rest.config.SanitizerDeserializer;
import ch.ssdd.eventhub.domain.command.CreateEventCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import tools.jackson.databind.annotation.JsonDeserialize;

public record CreateEventRequestDto(
        @NotNull
        @NotBlank
        @Size(max = 255)
        @JsonDeserialize(using = SanitizerDeserializer.class)
        String title,
        @NotNull
        @Size(max = 255)
        @JsonDeserialize(using = SanitizerDeserializer.class)
        String description,
        @NotNull
        LocalDateTime from,
        @NotNull
        LocalDateTime to,
        @NotNull
        @NotBlank
        @Size(max = 255)
        @JsonDeserialize(using = SanitizerDeserializer.class)
        String location) {

    public CreateEventCommand toCommand(String username) {
        return new CreateEventCommand(title, description, from, to, location, username);
    }
}
