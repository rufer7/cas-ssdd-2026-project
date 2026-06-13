package ch.ssdd.eventhub.security.config;

import java.util.Objects;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.CollectionUtils;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final String audience;

    public AudienceValidator(String audience) {
        this.audience = Objects.requireNonNull(audience, "audience cannot be null");
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (!CollectionUtils.isEmpty(jwt.getAudience()) && jwt.getAudience().contains(audience)) {
            return OAuth2TokenValidatorResult.success();
        }
        OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "The required audience '" + audience + "' is missing",
                null);
        return OAuth2TokenValidatorResult.failure(error);
    }
}
