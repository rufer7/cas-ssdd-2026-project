package ch.ssdd.eventhub.adapters.inbound.rest.dto;

import java.time.LocalDateTime;

import ch.ssdd.eventhub.adapters.inbound.rest.config.SanitizerDeserializer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.annotation.JsonDeserialize;

public record UpdateEventRequestDto(
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
}
