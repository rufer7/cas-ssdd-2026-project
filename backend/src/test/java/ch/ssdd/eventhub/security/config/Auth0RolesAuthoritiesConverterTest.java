package ch.ssdd.eventhub.security.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class Auth0RolesAuthoritiesConverterTest {

    private static final String ROLES_CLAIM = "https://eventhub.ssdd.ch/roles";

    private final Auth0RolesAuthoritiesConverter converter =
            new Auth0RolesAuthoritiesConverter(ROLES_CLAIM);

    @Test
    void shouldMapRolesClaimToAuthorities() {
        Jwt jwt = jwtWithRoles(List.of("Admin", "User"));

        assertThat(converter.convert(jwt))
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("Admin", "User");
    }

    @Test
    void shouldReturnEmptyWhenClaimMissing() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("john")
                .build();

        assertThat(converter.convert(jwt)).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenClaimIsNotACollection() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("john")
                .claim(ROLES_CLAIM, "Admin")
                .build();

        assertThat(converter.convert(jwt)).isEmpty();
    }

    @Test
    void shouldIgnoreBlankAndNullEntriesAndDeduplicate() {
        Jwt jwt = jwtWithRoles(java.util.Arrays.asList("Admin", "  ", null, "Admin", " User "));

        assertThat(converter.convert(jwt))
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("Admin", "User");
    }

    private static Jwt jwtWithRoles(List<?> roles) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("john")
                .claim(ROLES_CLAIM, roles)
                .build();
    }
}
