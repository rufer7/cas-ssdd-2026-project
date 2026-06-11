package ch.ssdd.eventhub.adapters.inbound.rest.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SanitizedStringConverterTest {

    private SanitizedStringConverter converter;

    @BeforeEach
    void setUp() {
        converter = new SanitizedStringConverter();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnNullOrEmpty_WhenInputIsNullOrEmpty(String input) {
        // when
        SanitizedString result = converter.convert(input);

        // then
        if (input == null) {
            assertNull(result);
        } else {
            assertNotNull(result);
            assertEquals("", result.value());
        }
    }

    @Test
    void shouldKeepSafeTextAsIs_WhenNoHtmlElementsArePresent() {
        // given
        String plainText = "Tech Conference 2026";

        // when
        SanitizedString result = converter.convert(plainText);

        // then
        assertNotNull(result);
        assertEquals("Tech Conference 2026", result.value());
    }

    @Test
    void shouldStripMaliciousScripts_WhenInputContainsXss() {
        // given
        String dangerousInput = "<script>alert('hack')</script>Concert";

        // when
        SanitizedString result = converter.convert(dangerousInput);

        // then
        assertNotNull(result);
        assertEquals("Concert", result.value());
    }

    @Test
    void shouldStripUnsupportedBlocks_WhenInputContainsIframes() {
        // given
        String inputWithIframe = "Club <iframe src='http://evil.com'></iframe>";

        // when
        SanitizedString result = converter.convert(inputWithIframe);

        // then
        assertNotNull(result);
        assertEquals("Club ", result.value());
    }

    @Test
    void shouldKeepFormattingAndLinks_WhenPolicyAllowsTheTags() {
        // given
        // Sanitizers.FORMATTING allows tags like <b>, <i>
        // Sanitizers.LINKS allows <a> tags with safe protocols
        String formattedInput = "<b>Party</b> time <a href='https://example.com'>click here</a>";

        // when
        SanitizedString result = converter.convert(formattedInput);

        // then
        assertNotNull(result);
        assertEquals("<b>Party</b> time <a href=\"https://example.com\" rel=\"nofollow\">click here</a>", result.value());
    }

    @Test
    void shouldRemoveJavaScriptLinks_WhenUsingUnsafeProtocols() {
        // given
        String maliciousLink = "<a href='javascript:exploit()'>click</a>";

        // when
        SanitizedString result = converter.convert(maliciousLink);

        // then
        assertNotNull(result);
        assertEquals("click", result.value()); // Leaves the inner text but completely drops the unsafe href attribute/tag context
    }
}
