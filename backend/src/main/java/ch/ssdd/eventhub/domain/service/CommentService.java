package ch.ssdd.eventhub.domain.service;

import ch.ssdd.eventhub.common.LocalDateTimeHelper;
import ch.ssdd.eventhub.domain.Comment;
import ch.ssdd.eventhub.ports.inbound.AddCommentUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadCommentsByEventUseCase;
import ch.ssdd.eventhub.ports.outbound.CommentPersistencePort;
import ch.ssdd.eventhub.ports.outbound.UserPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CommentService implements AddCommentUseCase, LoadCommentsByEventUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    private final CommentPersistencePort commentPersistencePort;
    private final UserPersistencePort userPersistencePort;

    public CommentService(CommentPersistencePort commentPersistencePort,
                          UserPersistencePort userPersistencePort) {
        this.commentPersistencePort = commentPersistencePort;
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public Comment addComment(UUID eventId, String content, String username) {
        logger.debug("Processing comment creation business logic for user '{}' and event with ID '{}' ...", username, eventId);

        var user = userPersistencePort.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Processing comment creation business logic for user '{}' and event with ID '{}' FAILED as the user does not exist in the system", username, eventId);
                    return new IllegalArgumentException("User not found for username: " + username);
                });

        var now = LocalDateTimeHelper.utcNow();
        var comment = new Comment(content, user, now, user, now);

        var savedComment = commentPersistencePort.save(eventId, comment, user);

        logger.info("Processing comment creation business logic for user '{}' and event with ID '{}' SUCCEEDED",
                username, eventId);

        return savedComment;
    }

    @Override
    public List<Comment> loadCommentsByEvent(UUID eventId) {
        logger.debug("Loading all comments of event with ID '{}' ...", eventId);
        var comments = commentPersistencePort.findAllByEventId(eventId);
        logger.info("Loading all comments of event with ID '{}' SUCCEEDED ({} events found)", eventId, comments.size());
        return comments;
    }
}
