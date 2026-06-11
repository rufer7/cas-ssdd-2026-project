package ch.ssdd.eventhub.adapters.inbound.rest.security;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FileChecker {

    private FileChecker() {}

    private static final Logger logger = LoggerFactory.getLogger(FileChecker.class);

    private static final int MAX_ALLOWED_FILE_SIZE = 4 * 1024 * 1024; // 4 MB
    // Allowed file extensions and their expected file signatures
    public static final Map<String, List<byte[]>> ALLOWED_FILE_EXTENSIONS = Map.of(
            "jpg", List.of(
                    new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0 },
                    new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE1 },
                    new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE8 }
            ),
            "jpeg", List.of(
                    new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0 },
                    new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE2 },
                    new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE3 }
            ),
            "png", List.of(
                    new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A }
            )
    );

    public static boolean isValid(String fileName, byte[] fileContent)
    {
        if (fileName == null) {
            throw new IllegalArgumentException("fileName cannot be null");
        }
        if (fileContent == null) {
            throw new IllegalArgumentException("fileContent cannot be null");
        }
        if (fileContent.length == 0)
        {
            throw new IllegalArgumentException("Length of fileContent cannot be 0");
        }

        if (!isFileSizeBelowMaxAllowedSize(fileContent.length))
        {
            logger.error("Size of file with name '{}' exceeds the maximum allowed size of '{}' bytes", fileName, MAX_ALLOWED_FILE_SIZE);
            return false;
        }
        if (!hasAllowedFileExtension(fileName))
        {
            logger.error("File with name '{}' has an invalid file extension", fileName);
            return false;
        }
        if (!isExtensionMatchingSignature(fileName, fileContent))
        {
            logger.error("File with name '{}' does not match the expected signature for its extension", fileName);
            return false;
        }

        return true;
    }

    private static boolean isFileSizeBelowMaxAllowedSize(long fileLengthInBytes)
    {
        return fileLengthInBytes <= MAX_ALLOWED_FILE_SIZE;
    }

    private static boolean hasAllowedFileExtension(String fileName)
    {
        if (fileName.isEmpty()) {
            return false;
        }

        var extension = FilenameUtils.getExtension(fileName);

        return !extension.isEmpty() && ALLOWED_FILE_EXTENSIONS.containsKey(extension.toLowerCase());
    }

    private static boolean isExtensionMatchingSignature(String fileName, byte[] file)
    {
        var extension = FilenameUtils.getExtension(fileName).toLowerCase();
        if (extension.isEmpty())
        {
            logger.warn("File with name '{}' has no extension.", fileName);
            return false;
        }

        var expectedSignatures = ALLOWED_FILE_EXTENSIONS.get(extension);
        if (expectedSignatures == null) {
            logger.warn("Extension '{}' is not allowed (File name: {}).", extension, fileName);
            return false;
        }

        if (expectedSignatures.isEmpty())
        {
            logger.warn(
                    "Skipping signature validation for file with name '{}' as no expected signature(s) specified.",
                    fileName
            );
            return true;
        }

        for (byte[] expectedSignature : expectedSignatures) {
            byte[] fileSignature = Arrays.copyOf(file, expectedSignature.length);
            if (!Arrays.equals(fileSignature, expectedSignature)) {
                continue;
            }

            return true;
        }

        logger.warn("File with name '{}' does not match any expected signature for extension '{}'.", fileName, extension);
        return false;
    }
}
