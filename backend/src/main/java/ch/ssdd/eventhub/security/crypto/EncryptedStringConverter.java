package ch.ssdd.eventhub.security.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA converter that encrypts string columns at rest with AES-256-GCM, used to protect the
 * "secret" personal note content (see the project's security requirements).
 *
 * <p>The key is derived (SHA-256) from the {@code APP_ENCRYPTION_KEY} environment variable. A
 * random 96-bit IV is generated per value and prepended to the ciphertext; the stored form is
 * {@code enc:v1:base64(iv || ciphertext+tag)}. Values without that prefix are returned verbatim so
 * pre-existing/seed plaintext keeps working after the feature is introduced.
 */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static final Logger logger = LoggerFactory.getLogger(EncryptedStringConverter.class);

    private static final String PREFIX = "enc:v1:";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final String DEV_FALLBACK_KEY = "dev-only-insecure-key-change-me";

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final SecretKey KEY = deriveKey();

    private static SecretKey deriveKey() {
        String secret = System.getenv("APP_ENCRYPTION_KEY");
        if (secret == null || secret.isBlank()) {
            logger.warn("APP_ENCRYPTION_KEY is not set; using an insecure development key. "
                    + "Set APP_ENCRYPTION_KEY in non-local environments.");
            secret = DEV_FALLBACK_KEY;
        }
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(hash, "AES");
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to derive encryption key", e);
        }
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, KEY, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            byte[] combined = ByteBuffer.allocate(iv.length + ciphertext.length)
                    .put(iv)
                    .put(ciphertext)
                    .array();
            return PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to encrypt value", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        if (!dbData.startsWith(PREFIX)) {
            // Legacy / seed plaintext written before encryption was introduced.
            return dbData;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(dbData.substring(PREFIX.length()));
            ByteBuffer buffer = ByteBuffer.wrap(combined);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, KEY, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Failed to decrypt value", e);
        }
    }
}
