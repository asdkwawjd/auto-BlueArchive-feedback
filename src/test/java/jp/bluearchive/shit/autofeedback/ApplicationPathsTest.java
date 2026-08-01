package jp.bluearchive.shit.autofeedback;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationPathsTest {

    @TempDir
    Path tempDir;

    @Test
    void resolvesRelativePathsFromApplicationHome() {
        assertEquals(
                tempDir.resolve("data").resolve("emails.txt"),
                ApplicationPaths.resolve(tempDir, "data/emails.txt"));
    }

    @Test
    void preservesAbsolutePaths() {
        Path absolute = tempDir.resolve("content.txt").toAbsolutePath();

        assertEquals(absolute.normalize(), ApplicationPaths.resolve(tempDir, absolute.toString()));
    }
}
