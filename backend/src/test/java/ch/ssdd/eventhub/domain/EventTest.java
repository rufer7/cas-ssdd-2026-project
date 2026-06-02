package ch.ssdd.eventhub.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventTest {

    @Test
    void shouldCreateValidEvent() {
        Event event = validEvent();

        assertEquals("Title", event.title());
        assertEquals("Description", event.description());
        assertEquals("Zurich", event.location());
        assertNotNull(event.comments());
    }

    @Test
    void shouldThrowWhenTitleIsBlank() {
        LocalDateTime now = LocalDateTime.now();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Event(
                        "",
                        "Description",
                        now.plusDays(1),
                        now.plusDays(2),
                        "Zurich",
                        dummyUser(),
                        now,
                        dummyUser(),
                        now,
                        null
                )
        );

        assertTrue(ex.getMessage().contains("title"));
    }

    @Test
    void shouldThrowWhenTitleIsNull() {
        LocalDateTime now = LocalDateTime.now();

        assertThrows(
                NullPointerException.class,
                () -> new Event(
                        null,
                        "Description",
                        now.plusDays(1),
                        now.plusDays(2),
                        "Zurich",
                        dummyUser(),
                        now,
                        dummyUser(),
                        now,
                        null
                )
        );
    }

    @Test
    void shouldThrowWhenFromIsAfterTo() {
        LocalDateTime now = LocalDateTime.now();

        assertThrows(
                IllegalArgumentException.class,
                () -> new Event(
                        "Title",
                        "Description",
                        now.plusDays(3),
                        now.plusDays(1),
                        "Zurich",
                        dummyUser(),
                        now,
                        dummyUser(),
                        now,
                        null
                )
        );
    }

    @Test
    void shouldAddCommentImmutably() {
        Event event = validEvent();
        Comment comment = dummyComment();

        Event updated = event.withComment(comment);

        assertEquals(0, event.comments().size());
        assertEquals(1, updated.comments().size());
    }

    @Test
    void shouldRemoveComment() {
        Event event = validEvent();
        Comment comment = dummyComment();

        Event updated = event.withComment(comment);
        Event removed = updated.withoutComment(comment);

        assertEquals(0, removed.comments().size());
    }

    @Test
    void shouldIgnoreRemovingNonExistingComment() {
        Event event = validEvent();
        Comment comment = dummyComment();

        Event result = event.withoutComment(comment);

        assertSame(event, result);
    }

    private Comment dummyComment() {
        return new Comment(
                "Test",
                dummyUser(),
                LocalDateTime.now(),
                dummyUser(),
                LocalDateTime.now()
        );
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

    private Event validEvent() {
        LocalDateTime now = LocalDateTime.now();

        return new Event(
                "Title",
                "Description",
                now.plusDays(1),
                now.plusDays(2),
                "Zurich",
                dummyUser(),
                now,
                dummyUser(),
                now,
                null
        );
    }
}