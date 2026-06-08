package ch.ssdd.eventhub.adapters.inbound.rest.dto;

import ch.ssdd.eventhub.domain.Comment;

import java.time.LocalDateTime;

public record CommentResponseDto(
        String content,
        String createdBy,
        LocalDateTime createdAt) {

    public static CommentResponseDto of(Comment comment) {
        return new CommentResponseDto(
                comment.content(),
                comment.createdBy().username(),
                comment.createdAt());
    }
}
