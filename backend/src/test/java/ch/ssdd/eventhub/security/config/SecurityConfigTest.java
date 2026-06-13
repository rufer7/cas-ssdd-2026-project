package ch.ssdd.eventhub.security.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.ssdd.eventhub.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("auth0")
@Import(TestSecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void apiRequiresAuthorization() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpointForbiddenForNonAdmin() throws Exception {
        // A valid body is sent so the request reaches the @PreAuthorize check (otherwise body
        // binding would fail first with 400); only the missing 'Admin' authority should reject it.
        var request = """
                {
                  "title": "Integration Test Event",
                  "description": "Test",
                  "from": "2026-06-01T10:00:00",
                  "to": "2026-06-01T12:00:00",
                  "location": "Zurich",
                  "username": "john_user"
                }
                """;

        mockMvc.perform(post("/api/events")
                .with(jwt().jwt(userToken())
                        .authorities(new SimpleGrantedAuthority(SecurityConfig.USER_AUTHORITY)))
                        .contentType("application/json")
                        .content(request))
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
