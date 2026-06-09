package ch.ssdd.eventhub.security.audit;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationAuditListener {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationAuditListener.class);

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        // TODO: get email claim here
        String username = event.getAuthentication().getName();
        logger.info("Authentication Success: User '{}' authenticated successfully.", username);
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        // TODO: get email claim here
        String username = event.getAuthentication().getName();
        String exceptionMessage = event.getException().getMessage();
        logger.warn("Authentication Failure: Attempted authentication for user '{}' failed. Reason: {}",
                username, exceptionMessage);
    }
}