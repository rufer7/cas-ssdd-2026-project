package ch.ssdd.eventhub.adapters.outbound.persistence;

import ch.ssdd.eventhub.domain.Comment;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.outbound.CommentPersistencePort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class JpaCommentPersistenceAdapter implements CommentPersistencePort {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public JpaCommentPersistenceAdapter(CommentRepository commentRepository,
                                        EventRepository eventRepository,
                                        UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Comment save(UUID eventId, Comment comment, User createdBy) {
        EventEntity eventEntity = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        // TODO: resolve user from authentication context once security is in place
        UserEntity userEntity = userRepository.findByUsername(createdBy.username())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + createdBy.username()));

        CommentEntity entity = new CommentEntity();
        entity.setContent(comment.content());
        entity.setEvent(eventEntity);
        entity.setCreatedBy(userEntity);
        entity.setCreatedAt(comment.createdAt());
        entity.setModifiedBy(userEntity);
        entity.setModifiedAt(comment.modifiedAt());

        commentRepository.save(entity);
        return comment;
    }

    @Override
    public List<Comment> findAllByEventId(UUID eventId) {
        return commentRepository.findAllByEvent_Id(eventId)
                .stream()
                .map(CommentEntity::toComment)
                .toList();
    }
}
