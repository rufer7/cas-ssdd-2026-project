package ch.ssdd.eventhub.adapters.inbound.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateEventRequestDto(
        @NotNull @NotBlank @Size(max = 255) String title,
        @NotNull @Size(max = 255) String description,
        @NotNull LocalDateTime from,
        @NotNull LocalDateTime to,
        @NotNull @NotBlank @Size(max = 255) String location,
        // TODO: to be removed as soon as authentication is in place
        String username) {
}
