package ch.ssdd.eventhub.adapters.outbound.persistence;

import ch.ssdd.eventhub.domain.Note;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.outbound.NotePersistencePort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JpaNotePersistenceAdapter implements NotePersistencePort {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public JpaNotePersistenceAdapter(NoteRepository noteRepository,
                                     UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Note save(Note note, User createdBy) {
        // TODO: resolve user from authentication context once security is in place
        UserEntity userEntity = userRepository.findByUsername(createdBy.username())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + createdBy.username()));

        NoteEntity entity = new NoteEntity();
        entity.setContent(note.content());
        entity.setCreatedBy(userEntity);
        entity.setCreatedAt(note.createdAt());
        entity.setModifiedBy(userEntity);
        entity.setModifiedAt(note.modifiedAt());

        noteRepository.save(entity);
        return note;
    }

    @Override
    public List<Note> findAll() {
        return noteRepository.findAll()
                .stream()
                .map(NoteEntity::toNote)
                .toList();
    }
}
