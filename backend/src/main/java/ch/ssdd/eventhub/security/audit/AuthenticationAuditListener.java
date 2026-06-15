package ch.ssdd.eventhub.security.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationAuditListener {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationAuditListener.class);

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String principal = describePrincipal(event.getAuthentication());
        logger.info("Authentication Success: User '{}' authenticated successfully.", principal);
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String principal = describePrincipal(event.getAuthentication());
        String exceptionMessage = event.getException().getMessage();
        logger.warn("Authentication Failure: Attempted authentication for user '{}' failed. Reason: {}",
                principal, exceptionMessage);
    }

    private String describePrincipal(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("email");
            if (email != null && !email.isBlank()) {
                return email;
            }
        }
        return authentication != null ? authentication.getName() : "unknown";
    }
}
