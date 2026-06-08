package ch.ssdd.eventhub.domain.service;

import ch.ssdd.eventhub.domain.Comment;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.inbound.AddCommentUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadCommentsByEventUseCase;
import ch.ssdd.eventhub.ports.outbound.CommentPersistencePort;
import ch.ssdd.eventhub.ports.outbound.UserPersistencePort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CommentService implements AddCommentUseCase, LoadCommentsByEventUseCase {

    private final CommentPersistencePort commentPersistencePort;
    private final UserPersistencePort userPersistencePort;

    public CommentService(CommentPersistencePort commentPersistencePort,
                          UserPersistencePort userPersistencePort) {
        this.commentPersistencePort = commentPersistencePort;
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public Comment addComment(UUID eventId, String content, String username) {
        User user = userPersistencePort.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found for username: " + username));

        LocalDateTime now = LocalDateTime.now();
        Comment comment = new Comment(content, user, now, user, now);

        return commentPersistencePort.save(eventId, comment, user);
    }

    @Override
    public List<Comment> loadCommentsByEvent(UUID eventId) {
        return commentPersistencePort.findAllByEventId(eventId);
    }
}
