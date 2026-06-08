package ch.ssdd.eventhub.ports.inbound;

import ch.ssdd.eventhub.domain.Note;

import java.util.List;

public interface LoadNotesByUserUseCase {

    List<Note> loadNotesByUser(String username);
}
