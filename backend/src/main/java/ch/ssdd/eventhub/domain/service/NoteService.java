package ch.ssdd.eventhub.domain.service;

import ch.ssdd.eventhub.common.LocalDateTimeHelper;
import ch.ssdd.eventhub.domain.Note;
import ch.ssdd.eventhub.domain.User;
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

        // The username is the trusted authenticated principal (never client-supplied), so a
        // missing local record is safely provisioned with the least-privilege USER role.
        var user = userPersistencePort.findByUsername(username)
                .orElseGet(() -> {
                    logger.info("Provisioning new user '{}' before creating note.", username);
                    return userPersistencePort.save(User.createNewProvisionedUser(username));
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
        // A not-yet-provisioned principal simply has no notes; avoid leaking existence details.
        var user = userPersistencePort.findByUsername(username);
        if (user.isEmpty()) {
            logger.info("Loading notes of user '{}' SUCCEEDED (user has no provisioned record yet)", username);
            return List.of();
        }
        var notes = notePersistencePort.findAllByUser(user.get().username());

        logger.info("Loading notes of user '{}' SUCCEEDED ({} notes found)", username, notes.size());

        return notes;
    }
}
