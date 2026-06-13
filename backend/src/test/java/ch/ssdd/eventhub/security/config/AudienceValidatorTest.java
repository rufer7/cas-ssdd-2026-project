package ch.ssdd.eventhub.security.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class AudienceValidatorTest {

    private static final String AUDIENCE = "https://api.eventhub.ssdd.ch";

    private final AudienceValidator validator = new AudienceValidator(AUDIENCE);

    @Test
    void shouldSucceedWhenAudienceIsPresent() {
        Jwt jwt = jwtWithAudience(List.of("https://other", AUDIENCE));

        assertThat(validator.validate(jwt).hasErrors()).isFalse();
    }

    @Test
    void shouldFailWhenAudienceIsMissing() {
        Jwt jwt = jwtWithAudience(List.of("https://other"));

        var result = validator.validate(jwt);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors())
                .anySatisfy(error -> assertThat(error.getErrorCode()).isEqualTo("invalid_token"));
    }

    private static Jwt jwtWithAudience(List<String> audience) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("john")
                .audience(audience)
                .build();
    }
}
