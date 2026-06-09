package ch.ssdd.eventhub.domain.service;

import ch.ssdd.eventhub.common.LocalDateTimeHelper;
import ch.ssdd.eventhub.domain.Note;
import ch.ssdd.eventhub.ports.inbound.CreateNoteUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadNotesByUserUseCase;
import ch.ssdd.eventhub.ports.outbound.NotePersistencePort;
import ch.ssdd.eventhub.ports.outbound.UserPersistencePort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteService implements CreateNoteUseCase, LoadNotesByUserUseCase {

    private final NotePersistencePort notePersistencePort;
    private final UserPersistencePort userPersistencePort;

    public NoteService(NotePersistencePort notePersistencePort,
            UserPersistencePort userPersistencePort) {
        this.notePersistencePort = notePersistencePort;
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public Note createNote(String content, String username) {
        var user = userPersistencePort.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        LocalDateTime now = LocalDateTimeHelper.utcNow();
        var note = new Note(content, user, now, user, now);

        return notePersistencePort.save(note, user);
    }

    @Override
    public List<Note> loadNotesByUser(String username) {
        var user = userPersistencePort.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return notePersistencePort.findAllByUser(user.username());
    }
}
