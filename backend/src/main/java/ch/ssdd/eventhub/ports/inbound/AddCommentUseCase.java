package ch.ssdd.eventhub.ports.inbound;

import ch.ssdd.eventhub.domain.Comment;
import java.util.UUID;

public interface AddCommentUseCase {

    Comment addComment(UUID eventId, String content, String username);
}
