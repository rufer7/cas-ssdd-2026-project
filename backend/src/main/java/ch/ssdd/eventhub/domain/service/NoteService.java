package ch.ssdd.eventhub.domain.service;

import ch.ssdd.eventhub.common.LocalDateTimeHelper;
import ch.ssdd.eventhub.domain.Note;
import ch.ssdd.eventhub.ports.inbound.CreateNoteUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadNotesByUserUseCase;
import ch.ssdd.eventhub.ports.outbound.NotePersistencePort;
import ch.ssdd.eventhub.ports.outbound.UserPersistencePort;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NoteService implements CreateNoteUseCase, LoadNotesByUserUseCase {

    private static final Logger logger = LoggerFactory.getLogger(NoteService.class);

    private final NotePersistencePort notePersistencePort;
    private final UserPersistencePort userPersistencePort;

    public NoteService(NotePersistencePort notePersistencePort,
            UserPersistencePort userPersistencePort) {
        this.notePersistencePort = notePersistencePort;
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public Note createNote(String content, String username) {
        logger.debug("Processing note creation business logic for user '{}' ...", username);

        var user = userPersistencePort.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Processing note creation business logic for user '{}' FAILED as the user does not exist in the system", username);
                    return new IllegalArgumentException("User not found: " + username);
                });

        var now = LocalDateTimeHelper.utcNow();
        var note = new Note(content, user, now, user, now);

        var savedNote = notePersistencePort.save(note, user);

        logger.info("Processing note creation business logic for user '{}' SUCCEEDED", username);

        return savedNote;
    }

    @Override
    public List<Note> loadNotesByUser(String username) {
        logger.debug("Loading notes of user '{}' ...", username);
        var user = userPersistencePort.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Loading notes of user '{}' FAILED as the user does not exist in the system", username);
                    return new IllegalArgumentException("User not found: " + username);
                });
        var notes = notePersistencePort.findAllByUser(user.username());

        logger.info("Loading notes of user '{}' SUCCEEDED ({} notes found)", username, notes.size());

        return notes;
    }
}
