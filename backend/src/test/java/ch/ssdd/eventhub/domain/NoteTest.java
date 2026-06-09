package ch.ssdd.eventhub.domain;

import ch.ssdd.eventhub.common.LocalDateTimeHelper;
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
        LocalDateTime now = LocalDateTimeHelper.utcNow();

        User dummyUser = dummyUser();
        assertThrows(
                NullPointerException.class,
                () -> new Note(
                        null,
                        dummyUser,
                        now,
                        dummyUser,
                        now
                )
        );
    }

    @Test
    void shouldThrowWhenContentIsBlank() {
        LocalDateTime now = LocalDateTimeHelper.utcNow();

        User dummyUser = dummyUser();
        assertThrows(
                IllegalArgumentException.class,
                () -> new Note(
                        "",
                        dummyUser,
                        now,
                        dummyUser,
                        now
                )
        );
    }

    @Test
    void shouldThrowWhenCreatedAfterModified() {
        LocalDateTime now = LocalDateTimeHelper.utcNow();

        User dummyUser = dummyUser();
        LocalDateTime plusOne = now.plusDays(1);
        assertThrows(
                IllegalArgumentException.class,
                () -> new Note(
                        "Valid content",
                        dummyUser,
                        plusOne,
                        dummyUser,
                        now
                )
        );
    }

    @Test
    void shouldThrowWhenCreatedInFuture() {
        LocalDateTime now = LocalDateTimeHelper.utcNow();

        LocalDateTime plusTen = now.plusDays(10);
        User dummyUser = dummyUser();
        assertThrows(
                IllegalArgumentException.class,
                () -> new Note(
                        "Valid content",
                        dummyUser,
                        plusTen,
                        dummyUser,
                        plusTen
                )
        );
    }

    @Test
    void shouldAcceptMinimalValidNote() {
        LocalDateTime now = LocalDateTimeHelper.utcNow();

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
                LocalDateTimeHelper.utcNow().minusDays(1),
                LocalDateTimeHelper.utcNow().minusDays(1)
        );
    }

    private Note validNote() {
        LocalDateTime now = LocalDateTimeHelper.utcNow();

        return new Note(
                "This is a valid note",
                dummyUser(),
                now,
                dummyUser(),
                now
        );
    }


}