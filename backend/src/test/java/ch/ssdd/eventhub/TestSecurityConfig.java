package ch.ssdd.eventhub;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.mockito.Mockito.mock;

/**
 * Test configuration that provides a stub {@link JwtDecoder} so that the
 * {@code oauth2ResourceServer().jwt()} security configuration can be loaded
 * without requiring a real Microsoft Entra ID tenant. Tests that want to assert
 * authorisation behaviour should use {@code SecurityMockMvcRequestPostProcessors.jwt()}.
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return mock(JwtDecoder.class);
    }
}
