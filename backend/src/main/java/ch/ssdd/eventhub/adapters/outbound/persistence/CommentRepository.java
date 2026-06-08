package ch.ssdd.eventhub.adapters.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<CommentEntity, UUID> {

    // Spring Data JPA traverses the 'event' association to match on event.id
    List<CommentEntity> findAllByEvent_Id(UUID eventId);
}
