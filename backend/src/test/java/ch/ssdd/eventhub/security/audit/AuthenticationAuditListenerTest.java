package ch.ssdd.eventhub.security.audit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationAuditListenerTest {

    @InjectMocks
    private AuthenticationAuditListener auditListener;

    @Mock
    private AuthenticationSuccessEvent successEvent;

    @Mock
    private AbstractAuthenticationFailureEvent failureEvent;

    @Mock
    private Authentication authentication;

    @Mock
    private AuthenticationException authenticationException;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        // Attach ListAppender to capture log outputs
        logger = (Logger) LoggerFactory.getLogger(AuthenticationAuditListener.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void onAuthenticationSuccess_ShouldLogInfoMessageWithUsername() {
        // Arrange
        String testUser = "john_doe";
        when(successEvent.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testUser);

        // Act
        auditListener.onAuthenticationSuccess(successEvent);

        // Assert
        List<ILoggingEvent> logs = listAppender.list;
        assertEquals(1, logs.size(), "Should log exactly one message");

        ILoggingEvent logEvent = logs.getFirst();
        assertEquals(Level.INFO, logEvent.getLevel());
        assertEquals("Authentication Success: User 'john_doe' logged in successfully.", logEvent.getFormattedMessage());
    }

    @Test
    void onAuthenticationFailure_ShouldLogWarnMessageWithUsernameAndReason() {
        // Arrange
        String testUser = "jane_doe";
        String exceptionMessage = "Bad credentials";

        when(failureEvent.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testUser);
        when(failureEvent.getException()).thenReturn(authenticationException);
        when(authenticationException.getMessage()).thenReturn(exceptionMessage);

        // Act
        auditListener.onAuthenticationFailure(failureEvent);

        // Assert
        List<ILoggingEvent> logs = listAppender.list;
        assertEquals(1, logs.size(), "Should log exactly one message");

        ILoggingEvent logEvent = logs.getFirst();
        assertEquals(Level.WARN, logEvent.getLevel());
        assertEquals("Authentication Failure: Attempted login for user 'jane_doe' failed. Reason: Bad credentials", logEvent.getFormattedMessage());
    }
}
