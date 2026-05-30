package ch.ssdd.eventhub.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    @Test
    void shouldCreateValidUser() {
        User user = validUser();

        assertEquals("john_doe", user.username());
        assertEquals("ext-123", user.externalId());
        assertEquals(Role.USER, user.role());
    }

    @Test
    void shouldThrowWhenUsernameIsNull() {
        LocalDateTime now = LocalDateTime.now();

        assertThrows(
                NullPointerException.class,
                () -> new User(
                        null,
                        "ext-123",
                        Role.USER,
                        now,
                        now
                )
        );
    }

    @Test
    void shouldThrowWhenExternalIdIsNull() {
        LocalDateTime now = LocalDateTime.now();

        assertThrows(
                NullPointerException.class,
                () -> new User(
                        "john",
                        null,
                        Role.USER,
                        now,
                        now
                )
        );
    }

    @Test
    void shouldThrowWhenRoleIsNull() {
        LocalDateTime now = LocalDateTime.now();

        assertThrows(
                NullPointerException.class,
                () -> new User(
                        "john",
                        "ext-123",
                        null,
                        now,
                        now
                )
        );
    }

    @Test
    void shouldThrowWhenUsernameIsBlank() {
        LocalDateTime now = LocalDateTime.now();

        assertThrows(
                IllegalArgumentException.class,
                () -> new User(
                        "",
                        "ext-123",
                        Role.USER,
                        now,
                        now
                )
        );
    }

    @Test
    void shouldThrowWhenExternalIdIsBlank() {
        LocalDateTime now = LocalDateTime.now();

        assertThrows(
                IllegalArgumentException.class,
                () -> new User(
                        "john",
                        "",
                        Role.USER,
                        now,
                        now
                )
        );
    }

    @Test
    void shouldThrowWhenCreatedAfterModified() {
        LocalDateTime now = LocalDateTime.now();

        assertThrows(
                IllegalArgumentException.class,
                () -> new User(
                        "john",
                        "ext-123",
                        Role.USER,
                        now.plusDays(1),
                        now
                )
        );
    }

    @Test
    void shouldThrowWhenCreatedInFuture() {
        LocalDateTime now = LocalDateTime.now();

        assertThrows(
                IllegalArgumentException.class,
                () -> new User(
                        "john",
                        "ext-123",
                        Role.USER,
                        now.plusDays(10),
                        now.plusDays(10)
                )
        );
    }

    @Test
    void shouldAcceptMinimumValidUser() {
        LocalDateTime now = LocalDateTime.now();

        User user = new User(
                "a",
                "b",
                Role.USER,
                now,
                now
        );

        assertEquals("a", user.username());
        assertEquals("b", user.externalId());
    }

    private User validUser() {
        LocalDateTime now = LocalDateTime.now();

        return new User(
                "john_doe",
                "ext-123",
                Role.USER,
                now.minusDays(1),
                now.minusDays(1)
        );
    }

}