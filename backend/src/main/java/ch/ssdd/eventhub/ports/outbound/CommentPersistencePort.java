package ch.ssdd.eventhub.ports.outbound;

import ch.ssdd.eventhub.domain.Comment;
import ch.ssdd.eventhub.domain.User;

import java.util.List;
import java.util.UUID;

public interface CommentPersistencePort {

    Comment save(UUID eventId, Comment comment, User createdBy);

    List<Comment> findAllByEventId(UUID eventId);
}
