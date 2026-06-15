package ch.ssdd.eventhub.security.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EncryptedStringConverterTest {

    private final EncryptedStringConverter converter = new EncryptedStringConverter();

    @Test
    void shouldRoundTripValue() {
        String plaintext = "Review Q3 event budgets before the next board meeting.";

        String encrypted = converter.convertToDatabaseColumn(plaintext);

        assertThat(encrypted).startsWith("enc:v1:").isNotEqualTo(plaintext);
        assertThat(converter.convertToEntityAttribute(encrypted)).isEqualTo(plaintext);
    }

    @Test
    void shouldProduceDifferentCiphertextForSamePlaintext() {
        // Random per-value IV => no deterministic ciphertext (resists correlation).
        assertThat(converter.convertToDatabaseColumn("secret"))
                .isNotEqualTo(converter.convertToDatabaseColumn("secret"));
    }

    @Test
    void shouldReturnLegacyPlaintextUnchanged() {
        assertThat(converter.convertToEntityAttribute("legacy plaintext note"))
                .isEqualTo("legacy plaintext note");
    }

    @Test
    void shouldHandleNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }
}
