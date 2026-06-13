package ch.ssdd.eventhub.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.ssdd.eventhub.common.LocalDateTimeHelper;
import ch.ssdd.eventhub.domain.command.CreateEventCommand;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

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
        UUID fakeUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        LocalDateTime now = LocalDateTimeHelper.utcNow();

        LocalDateTime plusOneDay = now.plusDays(1);
        LocalDateTime plusTwoDays = now.plusDays(2);
        User dummyUser = dummyUser();
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Event(fakeUuid,
                        "",
                        "Description",
                        plusOneDay,
                        plusTwoDays,
                        "Zurich",
                        dummyUser,
                        now,
                        dummyUser,
                        now,
                        null
                )
        );

        assertTrue(ex.getMessage().contains("title"));
    }

    @Test
    void shouldThrowWhenTitleIsNull() {
        UUID fakeUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        LocalDateTime now = LocalDateTimeHelper.utcNow();

        LocalDateTime plusOneDay = now.plusDays(1);
        LocalDateTime plusTwoDay = now.plusDays(2);
        User dummyUser = dummyUser();
        assertThrows(
                NullPointerException.class,
                () -> new Event(fakeUuid,
                        null,
                        "Description",
                        plusOneDay,
                        plusTwoDay,
                        "Zurich",
                        dummyUser,
                        now,
                        dummyUser,
                        now,
                        null
                )
        );
    }

    @Test
    void shouldThrowWhenFromIsAfterTo() {
        UUID fakeUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        LocalDateTime now = LocalDateTimeHelper.utcNow();

        LocalDateTime plusThree = now.plusDays(3);
        LocalDateTime plusOne = now.plusDays(1);
        User dummyUser = dummyUser();
        assertThrows(
                IllegalArgumentException.class,
                () -> new Event(
                        fakeUuid,
                        "Title",
                        "Description",
                        plusThree,
                        plusOne,
                        "Zurich",
                        dummyUser,
                        now,
                        dummyUser,
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

    @Test
    void shouldUpdateDetailsImmutably() {
        Event event = validEvent();
        LocalDateTime now = LocalDateTimeHelper.utcNow();

        Event updated = event.updateDetails(
                "New Title", "New Description", now.plusDays(3), now.plusDays(4), "Bern");

        assertEquals("New Title", updated.title());
        assertEquals("New Description", updated.description());
        assertEquals("Bern", updated.location());
        // original is untouched
        assertEquals("Title", event.title());
        assertEquals(event.id(), updated.id());
    }

    @Test
    void shouldThrowWhenUpdateDetailsTitleBlank() {
        Event event = validEvent();
        LocalDateTime now = LocalDateTimeHelper.utcNow();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> event.updateDetails("", "d", now.plusDays(3), now.plusDays(4), "Bern"));

        assertTrue(ex.getMessage().contains("title"));
    }

    @Test
    void shouldThrowWhenUpdateDetailsFromAfterTo() {
        Event event = validEvent();
        LocalDateTime now = LocalDateTimeHelper.utcNow();

        assertThrows(
                IllegalArgumentException.class,
                () -> event.updateDetails("t", "d", now.plusDays(5), now.plusDays(4), "Bern"));
    }

    @Test
    void shouldUpdateFeaturedImageImmutably() {
        Event event = validEvent();
        byte[] image = {1, 2, 3};

        Event updated = event.updateFeaturedImage(image);

        assertArrayEquals(image, updated.featuredImage());
        assertEquals(event.id(), updated.id());
    }

    @Test
    void shouldCreateFromCommand() {
        LocalDateTime now = LocalDateTimeHelper.utcNow();
        User creator = dummyUser();
        CreateEventCommand command = new CreateEventCommand(
                "Conf", "Desc", now.plusDays(1), now.plusDays(2), "Zurich", creator.username());

        Event event = Event.createFromCommand(command, creator);

        assertEquals("Conf", event.title());
        assertSame(creator, event.createdBy());
        assertSame(creator, event.modifiedBy());
        assertNotNull(event.id());
    }

    @Test
    void shouldRejectCommandWithToBeforeFrom() {
        LocalDateTime now = LocalDateTimeHelper.utcNow();

        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateEventCommand(
                        "Conf", "Desc", now.plusDays(2), now.plusDays(1), "Zurich", "john"));
    }

    @Test
    void shouldBeEqualWhenAllFieldsExceptIdMatch() {
        UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 10, 0);
        User user = dummyUser();

        Event first = new Event(id1, "T", "D", base.plusDays(1), base.plusDays(2),
                "Zurich", user, base, user, base, null);
        Event sameButDifferentId = new Event(id2, "T", "D", base.plusDays(1), base.plusDays(2),
                "Zurich", user, base, user, base, null);

        assertEquals(first, sameButDifferentId);
        assertEquals(first.hashCode(), sameButDifferentId.hashCode());
        assertEquals(first, first);
        assertNotEquals(first, null);
        assertNotEquals(first, "not an event");

        Event differentTitle = new Event(id1, "Other", "D", base.plusDays(1), base.plusDays(2),
                "Zurich", user, base, user, base, null);
        assertNotEquals(first, differentTitle);
    }

    @Test
    void shouldIncludeTitleInToString() {
        Event event = validEvent();

        assertTrue(event.toString().contains("Title"));
    }

    private Comment dummyComment() {
        return new Comment(
                "Test",
                dummyUser(),
                LocalDateTimeHelper.utcNow(),
                dummyUser(),
                LocalDateTimeHelper.utcNow()
        );
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

    private Event validEvent() {
        UUID fakeUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        LocalDateTime now = LocalDateTimeHelper.utcNow();

        return new Event(
                fakeUuid,
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
