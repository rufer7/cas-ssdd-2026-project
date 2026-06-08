package ch.ssdd.eventhub.adapters.inbound.rest.dto;

import ch.ssdd.eventhub.domain.Note;

import java.time.LocalDateTime;

public record NoteResponseDto(
        String content,
        String createdBy,
        LocalDateTime createdAt) {

    public static NoteResponseDto of(Note note) {
        return new NoteResponseDto(
                note.content(),
                note.createdBy().username(),
                note.createdAt());
    }
}
