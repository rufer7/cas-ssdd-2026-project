package ch.ssdd.eventhub.ports.inbound;

import ch.ssdd.eventhub.domain.Note;

public interface CreateNoteUseCase {

    Note createNote(String content, String username);
}
