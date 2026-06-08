package ch.ssdd.eventhub.adapters.inbound.rest.dto;

import ch.ssdd.eventhub.adapters.inbound.rest.config.SanitizerDeserializer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.annotation.JsonDeserialize;

public record CreateNoteRequestDto(
        @NotNull
        @NotBlank
        @Size(max = 255)
        @JsonDeserialize(using = SanitizerDeserializer.class)
        String content,
        // TODO: to be removed as soon as authentication is in place
        String username) {
}
