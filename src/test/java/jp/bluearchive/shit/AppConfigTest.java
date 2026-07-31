package jp.bluearchive.shit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void createsAndReloadsDefaultConfigWhenMissing() throws IOException {
        Path path = tempDir.resolve("new-directory").resolve("config.json");

        AppConfig created = AppConfig.load(path);
        AppConfig reloaded = AppConfig.load(path);

        assertTrue(Files.isRegularFile(path));
        assertEquals(created, reloaded);
        assertEquals("emails.txt", created.files().emails());
        assertEquals(new ScreenPoint(600, 360), created.coordinates().browserFocus());
    }

    @Test
    void loadsValidConfig() throws IOException {
        AppConfig config = AppConfig.load(writeConfig(2000, 4000, 600));

        assertEquals("emails.txt", config.files().emails());
        assertEquals(15, config.browser().waitSeconds());
        assertEquals(new ScreenPoint(600, 360), config.coordinates().browserFocus());
    }

    @Test
    void rejectsInvalidDelayRange() throws IOException {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> AppConfig.load(writeConfig(5000, 4000, 600)));

        assertTrue(exception.getMessage().contains("betweenEmailsMinMs"));
    }

    @Test
    void rejectsNegativeCoordinates() throws IOException {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> AppConfig.load(writeConfig(2000, 4000, -1)));

        assertTrue(exception.getMessage().contains("coordinates.browserFocus"));
    }

    private Path writeConfig(int minDelay, int maxDelay, int focusX) throws IOException {
        Path path = tempDir.resolve("config.json");
        Files.writeString(path, """
                {
                  "files": { "emails": "emails.txt", "content": "content.txt" },
                  "browser": { "binary": "", "waitSeconds": 15 },
                  "delays": {
                    "betweenEmailsMinMs": %d,
                    "betweenEmailsMaxMs": %d,
                    "robotAutoMs": 20,
                    "keyHoldMs": 30,
                    "clipboardMs": 50,
                    "clickBeforeMs": 80,
                    "clickHoldMs": 30,
                    "stepMs": 150,
                    "pageTransitionMs": 1500,
                    "afterSubmitMs": 1000,
                    "focusBeforeClickMs": 100,
                    "focusAfterClickMs": 300
                  },
                  "mouse": { "smoothSteps": 8, "smoothStepDelayMs": 10, "scrollAmount": 3 },
                  "workflow": { "categoryDownCount": 4 },
                  "coordinates": {
                    "browserFocus": { "x": %d, "y": 360 },
                    "categorySelect": { "x": 696, "y": 774 },
                    "categoryOption": { "x": 677, "y": 868 },
                    "categoryConfirm": { "x": 923, "y": 838 },
                    "formSection1": { "x": 500, "y": 480 },
                    "formSection2": { "x": 500, "y": 650 },
                    "formSection3": { "x": 500, "y": 850 },
                    "emailField": { "x": 500, "y": 956 },
                    "submit": { "x": 1064, "y": 902 },
                    "confirm": { "x": 945, "y": 795 }
                  }
                }
                """.formatted(minDelay, maxDelay, focusX));
        return path;
    }
}
