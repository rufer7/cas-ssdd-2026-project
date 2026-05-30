package ch.ssdd.eventhub.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NoteTest {
    
    @Test
    void shouldCreateValidNote() {
        Note note = validNote();

        assertEquals("This is a valid note", note.content());
        assertNotNull(note.createdBy());
        assertNotNull(note.modifiedBy());
    }

    @Test
    void shouldThrowWhenContentIsNull() {
        LocalDateTime now = LocalDateTime.now();

        assertThrows(
                NullPointerException.class,
                () -> new Note(
                        null,
                        dummyUser(),
                        now,
                        dummyUser(),
                        now
                )
        );
    }

    @Test
    void shouldThrowWhenContentIsBlank() {
        LocalDateTime now = LocalDateTime.now();

        assertThrows(
                IllegalArgumentException.class,
                () -> new Note(
                        "",
                        dummyUser(),
                        now,
                        dummyUser(),
                        now
                )
        );
    }

    @Test
    void shouldThrowWhenCreatedAfterModified() {
        LocalDateTime now = LocalDateTime.now();

        assertThrows(
                IllegalArgumentException.class,
                () -> new Note(
                        "Valid content",
                        dummyUser(),
                        now.plusDays(1),
                        dummyUser(),
                        now
                )
        );
    }

    @Test
    void shouldThrowWhenCreatedInFuture() {
        LocalDateTime now = LocalDateTime.now();

        assertThrows(
                IllegalArgumentException.class,
                () -> new Note(
                        "Valid content",
                        dummyUser(),
                        now.plusDays(10),
                        dummyUser(),
                        now.plusDays(10)
                )
        );
    }

    @Test
    void shouldAcceptMinimalValidNote() {
        LocalDateTime now = LocalDateTime.now();

        Note note = new Note(
                "a",
                dummyUser(),
                now,
                dummyUser(),
                now
        );

        assertEquals("a", note.content());
    }

    private User dummyUser() {
        return new User(
                "john",
                "ext-1",
                Role.USER,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
    }

    private Note validNote() {
        LocalDateTime now = LocalDateTime.now();

        return new Note(
                "This is a valid note",
                dummyUser(),
                now,
                dummyUser(),
                now
        );
    }


}