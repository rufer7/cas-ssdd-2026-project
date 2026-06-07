package ch.ssdd.eventhub.domain.service;

import ch.ssdd.eventhub.domain.Note;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.inbound.CreateNoteUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadAllNotesUseCase;
import ch.ssdd.eventhub.ports.outbound.NotePersistencePort;
import ch.ssdd.eventhub.ports.outbound.UserPersistencePort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteService implements CreateNoteUseCase, LoadAllNotesUseCase {

    private final NotePersistencePort notePersistencePort;
    private final UserPersistencePort userPersistencePort;

    public NoteService(NotePersistencePort notePersistencePort,
                       UserPersistencePort userPersistencePort) {
        this.notePersistencePort = notePersistencePort;
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public Note createNote(String content, String username) {
        User user = userPersistencePort.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found for username: " + username));

        LocalDateTime now = LocalDateTime.now();
        Note note = new Note(content, user, now, user, now);

        return notePersistencePort.save(note, user);
    }

    @Override
    public List<Note> loadAllNotes() {
        return notePersistencePort.findAll();
    }
}
