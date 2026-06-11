package ch.ssdd.eventhub.adapters.inbound.rest.security;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileCheckerTest {

    private static byte[] sampleFile;

    @BeforeAll
    static void setUp() throws IOException {
        sampleFile = Files.readAllBytes(Path.of("src/test/resources/spring.png"));
    }

    @Test
    void shouldThrowWhenFileNameNull() {
        var exception = assertThrows(
                RuntimeException.class,
                () -> FileChecker.isValid(null, sampleFile)
        );
        assertTrue(exception.getMessage().contains("fileName"));
    }

    @Test
    void shouldThrowWhenFileNull() {
        var exception = assertThrows(
                RuntimeException.class,
                () -> FileChecker.isValid("spring.png", null)
        );
        assertTrue(exception.getMessage().contains("fileContent cannot be null"));
    }

    @Test
    void shouldThrowWhenLengthZero() {
        var exception = assertThrows(
                RuntimeException.class,
                () -> FileChecker.isValid("spring.png", new byte[0])
        );
        assertTrue(exception.getMessage().contains("Length of fileContent"));
    }

    @Test
    void shouldReturnFalseWhenFileNameEmpty() {
        var result = FileChecker.isValid("", sampleFile);
        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "arbitrary.docx",
            "arbitrary.DOCX",
            "arbitrary.zip",
            "arbitrary.",
            "arbitrary"
    })
    void shouldReturnFalseWhenFileExtensionIsNotInAllowedFileExtensions(String fileName) {
        var result = FileChecker.isValid(fileName, sampleFile);
        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "arbitrary.jpg",
            "arbitrary.JPEG",
    })
    void shouldReturnFalseWhenExtensionIsNotMatchingSignature(String fileName) {
        var result = FileChecker.isValid(fileName, sampleFile);
        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "arbitrary.png",
            "arbitrary.PNG"
    })
    void shouldReturnTrueWhenExtensionIsMatchingSignature(String fileName) {
        var result = FileChecker.isValid(fileName, sampleFile);
        assertTrue(result);
    }
}
