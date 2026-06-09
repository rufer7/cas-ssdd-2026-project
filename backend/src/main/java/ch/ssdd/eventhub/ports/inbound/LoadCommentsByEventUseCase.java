package ch.ssdd.eventhub.ports.inbound;

import ch.ssdd.eventhub.domain.Comment;

import java.util.List;
import java.util.UUID;

public interface LoadCommentsByEventUseCase {

    List<Comment> loadCommentsByEvent(UUID eventId);
}
