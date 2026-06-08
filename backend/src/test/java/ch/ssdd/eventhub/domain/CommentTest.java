package ch.ssdd.eventhub.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommentTest {

    @Test
    void shouldCreateValidComment() {
        Comment comment = validComment();

        assertEquals("This is a valid comment", comment.content());
        assertNotNull(comment.createdBy());
        assertNotNull(comment.modifiedBy());
    }

    @Test
    void shouldThrowWhenContentIsBlank() {
        LocalDateTime now = LocalDateTime.now();

        User dummyUser = dummyUser();
        assertThrows(IllegalArgumentException.class,
                () -> new Comment("", dummyUser, now, dummyUser, now));
    }

    @Test
    void shouldThrowWhenContentIsNull() {
        LocalDateTime now = LocalDateTime.now();

        User dummyUser = dummyUser();
        assertThrows(
                NullPointerException.class,
                () -> new Comment(
                        null,
                        dummyUser,
                        now,
                        dummyUser,
                        now
                )
        );
    }

    @Test
    void shouldThrowWhenCreatedAfterModified() {
        LocalDateTime now = LocalDateTime.now();

        User dummyUser = dummyUser();
        LocalDateTime createdAt = now.plusDays(1);
        assertThrows(
                IllegalArgumentException.class,
                () -> new Comment(
                        "Valid content",
                        dummyUser,
                        createdAt,
                        dummyUser,
                        now
                )
        );
    }

    @Test
    void shouldThrowWhenCreatedAtInFuture() {
        LocalDateTime now = LocalDateTime.now();

        User dummyUser = dummyUser();
        LocalDateTime localDatePlusDays = now.plusDays(10);
        assertThrows(
                IllegalArgumentException.class,
                () -> new Comment(
                        "Valid content",
                        dummyUser,
                        localDatePlusDays,
                        dummyUser,
                        localDatePlusDays
                )
        );
    }

    @Test
    void shouldAcceptMinimumValidComment() {
        LocalDateTime now = LocalDateTime.now();

        User dummyUser = dummyUser();
        Comment comment = new Comment(
                "a",
                dummyUser,
                now,
                dummyUser,
                now
        );

        assertEquals("a", comment.content());
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

    private Comment validComment() {
        LocalDateTime now = LocalDateTime.now();

        return new Comment(
                "This is a valid comment",
                dummyUser(),
                now,
                dummyUser(),
                now
        );
    }
}