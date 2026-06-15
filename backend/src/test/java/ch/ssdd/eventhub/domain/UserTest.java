package ch.ssdd.eventhub.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ch.ssdd.eventhub.common.LocalDateTimeHelper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

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
        LocalDateTime now = LocalDateTimeHelper.utcNow();

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
        LocalDateTime now = LocalDateTimeHelper.utcNow();

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
        LocalDateTime now = LocalDateTimeHelper.utcNow();

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
        LocalDateTime now = LocalDateTimeHelper.utcNow();

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
        LocalDateTime now = LocalDateTimeHelper.utcNow();

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
        LocalDateTime now = LocalDateTimeHelper.utcNow();

        LocalDateTime plusOne = now.plusDays(1);
        assertThrows(
                IllegalArgumentException.class,
                () -> new User(
                        "john",
                        "ext-123",
                        Role.USER,
                        plusOne,
                        now
                )
        );
    }

    @Test
    void shouldThrowWhenCreatedInFuture() {
        LocalDateTime now = LocalDateTimeHelper.utcNow();

        LocalDateTime plusTen = now.plusDays(10);
        assertThrows(
                IllegalArgumentException.class,
                () -> new User(
                        "john",
                        "ext-123",
                        Role.USER,
                        plusTen,
                        plusTen
                )
        );
    }

    @Test
    void shouldAcceptMinimumValidUser() {
        LocalDateTime now = LocalDateTimeHelper.utcNow();

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

    @Test
    void shouldCreateProvisionedAdminUserWithAdminRole() {
        User user = User.createNewProvisionedAdminUser("alice_admin");

        assertEquals("alice_admin", user.username());
        assertEquals("alice_admin", user.externalId());
        assertEquals(Role.ADMIN, user.role());
    }

    private User validUser() {
        LocalDateTime now = LocalDateTimeHelper.utcNow();

        return new User(
                "john_doe",
                "ext-123",
                Role.USER,
                now.minusDays(1),
                now.minusDays(1)
        );
    }

}
