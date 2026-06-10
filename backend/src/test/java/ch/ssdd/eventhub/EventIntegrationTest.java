package ch.ssdd.eventhub;

import com.jayway.jsonpath.JsonPath;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@AutoConfigureMockMvc
class EventIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void clearDatabase(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    @WithMockUser(username = "alice_admin", roles = {"ADMIN"})
    void shouldCreateAndFetchEvent() throws Exception {

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

        var initialGet = mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andReturn();

        var initialJson = initialGet.getResponse().getContentAsString();
        var initialCount = JsonPath.parse(initialJson).read("$.length()", Integer.class);

        mockMvc.perform(post("/api/events")
                .with(csrf())
                .contentType("application/json")
                .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test Event"));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.title == 'Integration Test Event')]").exists())
                .andExpect(jsonPath("$.length()").value(initialCount + 1));
    }

    @Test
    @WithMockUser(username = "john_user", roles = {"USER"})
    void shouldNotCreateEventBecauseNotAdmin() throws Exception {

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

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alice_admin", roles = {"ADMIN"})
    void shouldReturnBadRequestWhenCreatingEventWithInvalidData() throws Exception {
        var invalidRequest = """
                {
                  "title": "",
                  "description": "Test",
                  "from": "2026-06-01T10:00:00",
                  "to": "2026-06-01T12:00:00",
                  "location": "Zurich",
                  "username": "alice_admin"
                }
                """;

        mockMvc.perform(post("/api/events")
                .with(csrf())
                .contentType("application/json")
                .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice_admin", roles = {"ADMIN"})
    void shouldUploadFeaturedImageToEvent() throws Exception {

        var file = Files.readAllBytes(Path.of("src/test/resources/spring.png"));
        var multipartFile = new MockMultipartFile("file", "spring.png",
                "image/png", file);

        var initialGet = mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andReturn();

        var initialJson = initialGet.getResponse().getContentAsString();
        var eventId = JsonPath.parse(initialJson).read("$.[0].eventId", String.class);

        mockMvc.perform(multipart("/api/events/" + eventId + "/uploadFeaturedImage")
                        .with(csrf())
                        .file(multipartFile))
                .andExpect(status().isOk());
    }
}