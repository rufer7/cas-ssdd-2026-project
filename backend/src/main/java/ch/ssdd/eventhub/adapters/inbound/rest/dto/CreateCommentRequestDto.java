package ch.ssdd.eventhub.adapters.inbound.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCommentRequestDto(
        @NotNull @NotBlank @Size(max = 255) String content,
        // TODO: to be removed as soon as authentication is in place
        String username) {
}
