package ch.ssdd.eventhub.adapters.inbound.rest.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@ExtendWith(MockitoExtension.class)
class SanitizerDeserializerTest {

    private SanitizerDeserializer deserializer;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private DeserializationContext deserializationContext;

    @BeforeEach
    void setUp() {
        deserializer = new SanitizerDeserializer();
    }

    @Test
    void deserialize_WithNullValue_ReturnsNull() {
        when(jsonParser.getValueAsString()).thenReturn(null);

        String result = deserializer.deserialize(jsonParser, deserializationContext);

        assertNull(result);
    }

    @Test
    void deserialize_WithPlainString_ReturnsSameString() {
        String plainText = "Hello World";
        when(jsonParser.getValueAsString()).thenReturn(plainText);

        String result = deserializer.deserialize(jsonParser, deserializationContext);

        assertEquals("Hello World", result);
    }

    @Test
    void deserialize_WithSafeHtml_KeepsSafeHtmlTags() {
        String safeHtml = "<b>Bold text</b> and a <a href=\"https://example.com\">link</a>";
        when(jsonParser.getValueAsString()).thenReturn(safeHtml);

        String result = deserializer.deserialize(jsonParser, deserializationContext);

        // OWASP Sanitizers.LINKS appends rel="nofollow" to target elements by default
        String expected = "<b>Bold text</b> and a <a href=\"https://example.com\" rel=\"nofollow\">link</a>";
        assertEquals(expected, result);
    }

    @Test
    void deserialize_WithMaliciousHtml_RemovesUnsafeTags() {
        String maliciousHtml = "<script>alert('hack')</script><p>Safe text</p><img src=\"x\" onerror=\"alert(1)\">";
        when(jsonParser.getValueAsString()).thenReturn(maliciousHtml);

        String result = deserializer.deserialize(jsonParser, deserializationContext);

        assertEquals("<p>Safe text</p>", result);
    }

    @Test
    void integration_WithJsonMapper_DeserializesAndSanitizesCorrectly() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new SanitizerDeserializer());

        JsonMapper mapper = JsonMapper.builder()
                .addModule(module)
                .build();

        String json = "{\"content\": \"<script>bad()</script><i>Good</i>\"}";

        TestWrapper result = mapper.readValue(json, TestWrapper.class);

        assertEquals("<i>Good</i>", result.getContent());
    }

    // Standard POJO wrapper used for the integration test
    private static class TestWrapper {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
