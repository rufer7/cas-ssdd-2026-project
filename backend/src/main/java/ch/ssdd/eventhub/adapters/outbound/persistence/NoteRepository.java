package ch.ssdd.eventhub.adapters.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NoteRepository extends JpaRepository<NoteEntity, UUID> {
}
