package ch.ssdd.eventhub.adapters.outbound.jpa;

import ch.ssdd.eventhub.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<EventEntity, UUID> {
}
