package ch.ssdd.eventhub.security.audit;

import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class AuthenticationAuditListenerTest {

    private final AuthenticationAuditListener listener = new AuthenticationAuditListener();

    @Test
    void shouldLogSuccessUsingEmailClaimWhenPrincipalIsJwt() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("auth0|123")
                .claim("email", "alice@example.com")
                .build();
        var authentication =
                new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("Admin")));

        assertThatNoException()
                .isThrownBy(() -> listener.onAuthenticationSuccess(
                        new AuthenticationSuccessEvent(authentication)));
    }

    @Test
    void shouldLogSuccessUsingNameWhenNoEmailClaim() {
        var authentication =
                new UsernamePasswordAuthenticationToken("john_user", null, List.of());

        assertThatNoException()
                .isThrownBy(() -> listener.onAuthenticationSuccess(
                        new AuthenticationSuccessEvent(authentication)));
    }

    @Test
    void shouldLogFailure() {
        var authentication =
                new UsernamePasswordAuthenticationToken("john_user", "secret");

        assertThatNoException()
                .isThrownBy(() -> listener.onAuthenticationFailure(
                        new AuthenticationFailureBadCredentialsEvent(
                                authentication, new BadCredentialsException("bad credentials"))));
    }
}
