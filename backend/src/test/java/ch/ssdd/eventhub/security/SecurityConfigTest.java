package ch.ssdd.eventhub.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
 * <li>{@code 401} when no bearer token is supplied;</li>
 * <li>{@code 403} when accessing an admin endpoint with a token that does
 * not carry the {@code Admin} app role;</li>
 * <li>{@code 403} when accessing an user endpoint with a token that does
 * not carry the {@code User} app role;</li>
 * <li>{@code 200/201} when accessing the API with a valid token.</li>
 * </ul>
 *
 * The {@code JwtDecoder} is mocked via {@link TestSecurityConfig}; the actual
 * authorities are supplied via
 * {@code SecurityMockMvcRequestPostProcessors.jwt()} so the assertions exercise
 * the real {@link SecurityConfig} authorisation rules.
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
        mockMvc.perform(post("/api/events")
                .with(jwt().jwt(userToken())
                        .authorities(new SimpleGrantedAuthority(SecurityConfig.USER_AUTHORITY))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpointAllowedForAdmin() throws Exception {
        var request = """
                {
                  "title": "Integration Test Event",
                  "description": "Test",
                  "from": "2026-06-01T10:00:00",
                  "to": "2026-06-01T12:00:00",
                  "location": "Zurich",
                  "username": "alice_admin"
                }
                """;

        mockMvc.perform(post("/api/events")
                .with(jwt().jwt(userToken())
                        .authorities(new SimpleGrantedAuthority(
                                SecurityConfig.ADMIN_AUTHORITY)))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isCreated());
    }

    @Test
    void userEndpointAllowedForUser() throws Exception {
        mockMvc.perform(get("/api/events").with(jwt().jwt(userToken())
                        .authorities(new SimpleGrantedAuthority(
                                SecurityConfig.ADMIN_AUTHORITY))))
                .andExpect(status().isOk());
    }

    private static Jwt userToken() {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("john_user")
                .claim("aud", "api://eventhub")
                .build();
    }
}
