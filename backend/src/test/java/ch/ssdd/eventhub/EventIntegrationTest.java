package ch.ssdd.eventhub;

import com.jayway.jsonpath.JsonPath;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void shouldCreateAndFetchEvent() throws Exception {

        String request = """
                {
                  "title": "Integration Test Event",
                  "description": "Test",
                  "from": "2026-06-01T10:00:00",
                  "to": "2026-06-01T12:00:00",
                  "location": "Zurich",
                  "username": "alice_admin"
                }
                """;

        MvcResult initialGet = mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andReturn();

        String initialJson = initialGet.getResponse().getContentAsString();
        int initialCount = JsonPath.parse(initialJson).read("$.length()", Integer.class);

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
    void shouldReturnBadRequestWhenCreatingEventWithInvalidData() throws Exception {
        String invalidRequest = """
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
}