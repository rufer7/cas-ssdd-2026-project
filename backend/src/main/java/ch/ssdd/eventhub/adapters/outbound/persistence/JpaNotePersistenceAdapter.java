package ch.ssdd.eventhub.adapters.outbound.persistence;

import ch.ssdd.eventhub.domain.Note;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.outbound.NotePersistencePort;
import java.util.List;
import org.springframework.stereotype.Component;

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
    public List<Note> findAllByUser(String username) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        return noteRepository.findByCreatedBy(userEntity)
                .stream()
                .map(NoteEntity::toNote)
                .toList();
    }
}
