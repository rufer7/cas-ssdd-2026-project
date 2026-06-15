package ch.ssdd.eventhub.domain.service;

import ch.ssdd.eventhub.common.LocalDateTimeHelper;
import ch.ssdd.eventhub.domain.Comment;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.inbound.AddCommentUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadCommentsByEventUseCase;
import ch.ssdd.eventhub.ports.outbound.CommentPersistencePort;
import ch.ssdd.eventhub.ports.outbound.UserPersistencePort;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

        // The username is the trusted authenticated principal (never client-supplied), so a
        // missing local record is safely provisioned with the least-privilege USER role.
        var user = userPersistencePort.findByUsername(username)
                .orElseGet(() -> {
                    logger.info("Provisioning new user '{}' before adding comment.", username);
                    return userPersistencePort.save(User.createNewProvisionedUser(username));
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
