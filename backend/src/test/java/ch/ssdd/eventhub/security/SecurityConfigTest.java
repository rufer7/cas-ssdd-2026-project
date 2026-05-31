package ch.ssdd.eventhub.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.ssdd.eventhub.TestSecurityConfig;
import ch.ssdd.eventhub.security.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies that the OIDC/Entra ID security configuration enforces the expected
 * status codes:
 * <ul>
 *     <li>{@code 401} when no bearer token is supplied;</li>
 *     <li>{@code 403} when accessing an admin endpoint with a token that does
 *     not carry the {@code Admin} app role;</li>
 *     <li>{@code 200} when accessing the API with a valid token.</li>
 * </ul>
 *
 * The {@code JwtDecoder} is mocked via {@link TestSecurityConfig}; the actual
 * authorities are supplied via {@code SecurityMockMvcRequestPostProcessors.jwt()}
 * so the assertions exercise the real {@link SecurityConfig} authorisation
 * rules.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void apiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpointForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/ping")
                        .with(jwt().jwt(userToken())
                                .authorities(new SimpleGrantedAuthority("APPROLE_User"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpointAllowedForAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/ping")
                        .with(jwt().jwt(userToken())
                                .authorities(new SimpleGrantedAuthority(SecurityConfig.ADMIN_AUTHORITY))))
                .andExpect(status().isOk());
    }

    @Test
    void apiAllowedWithValidToken() throws Exception {
        mockMvc.perform(get("/api/events").with(jwt().jwt(userToken())))
                .andExpect(status().isOk());
    }

    private static Jwt userToken() {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("user@example.com")
                .claim("aud", "api://eventhub")
                .build();
    }
}
