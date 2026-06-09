package ch.ssdd.eventhub.ports.outbound;

import ch.ssdd.eventhub.domain.Note;
import ch.ssdd.eventhub.domain.User;

import java.util.List;

public interface NotePersistencePort {

    Note save(Note note, User createdBy);

    List<Note> findAllByUser(String username);
}
