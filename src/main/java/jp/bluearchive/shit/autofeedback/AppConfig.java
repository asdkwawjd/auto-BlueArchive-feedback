package jp.bluearchive.shit.autofeedback;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

record AppConfig(
        FileConfig files,
        BrowserConfig browser,
        DelayConfig delays,
        MouseConfig mouse,
        WorkflowConfig workflow,
        CoordinateConfig coordinates) {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static AppConfig load(Path path) throws IOException {
        if (Files.notExists(path)) {
            AppConfig config = defaults();
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), config);
            System.out.println("[INFO] Created default config: " + path.toAbsolutePath());
            return config;
        }

        AppConfig config = OBJECT_MAPPER.readValue(path.toFile(), AppConfig.class);
        config.validate();
        return config;
    }

    private static AppConfig defaults() {
        return new AppConfig(
                new FileConfig("emails.txt", "content.txt"),
                new BrowserConfig(
                        "C:\\Users\\Administrator\\AppData\\Local\\Google\\Chrome\\Bin\\chromex.exe",
                        15),
                new DelayConfig(2000, 4000, 20, 30, 50, 80, 30, 150, 1500, 1000, 100, 300),
                new MouseConfig(8, 10, 3),
                new WorkflowConfig(4),
                new CoordinateConfig(
                        new ScreenPoint(600, 360),
                        new ScreenPoint(696, 774),
                        new ScreenPoint(677, 868),
                        new ScreenPoint(923, 838),
                        new ScreenPoint(500, 480),
                        new ScreenPoint(500, 650),
                        new ScreenPoint(500, 850),
                        new ScreenPoint(500, 956),
                        new ScreenPoint(1064, 902),
                        new ScreenPoint(945, 795)));
    }

    private void validate() {
        require(files != null, "files is required");
        require(browser != null, "browser is required");
        require(delays != null, "delays is required");
        require(mouse != null, "mouse is required");
        require(workflow != null, "workflow is required");
        require(coordinates != null, "coordinates is required");

        requireNotBlank(files.emails(), "files.emails");
        requireNotBlank(files.content(), "files.content");
        require(browser.binary() != null, "browser.binary is required; use an empty string for the default Chrome");
        requirePositive(browser.waitSeconds(), "browser.waitSeconds");

        requireNonNegative(delays.betweenEmailsMinMs(), "delays.betweenEmailsMinMs");
        requireNonNegative(delays.betweenEmailsMaxMs(), "delays.betweenEmailsMaxMs");
        require(delays.betweenEmailsMinMs() <= delays.betweenEmailsMaxMs(),
                "delays.betweenEmailsMinMs must not exceed delays.betweenEmailsMaxMs");
        requireNonNegative(delays.robotAutoMs(), "delays.robotAutoMs");
        requireNonNegative(delays.keyHoldMs(), "delays.keyHoldMs");
        requireNonNegative(delays.clipboardMs(), "delays.clipboardMs");
        requireNonNegative(delays.clickBeforeMs(), "delays.clickBeforeMs");
        requireNonNegative(delays.clickHoldMs(), "delays.clickHoldMs");
        requireNonNegative(delays.stepMs(), "delays.stepMs");
        requireNonNegative(delays.pageTransitionMs(), "delays.pageTransitionMs");
        requireNonNegative(delays.afterSubmitMs(), "delays.afterSubmitMs");
        requireNonNegative(delays.focusBeforeClickMs(), "delays.focusBeforeClickMs");
        requireNonNegative(delays.focusAfterClickMs(), "delays.focusAfterClickMs");

        requirePositive(mouse.smoothSteps(), "mouse.smoothSteps");
        requireNonNegative(mouse.smoothStepDelayMs(), "mouse.smoothStepDelayMs");
        require(mouse.scrollAmount() != 0, "mouse.scrollAmount must not be zero");
        requirePositive(workflow.categoryDownCount(), "workflow.categoryDownCount");

        validatePoint(coordinates.browserFocus(), "coordinates.browserFocus");
        validatePoint(coordinates.categorySelect(), "coordinates.categorySelect");
        validatePoint(coordinates.categoryOption(), "coordinates.categoryOption");
        validatePoint(coordinates.categoryConfirm(), "coordinates.categoryConfirm");
        validatePoint(coordinates.formSection1(), "coordinates.formSection1");
        validatePoint(coordinates.formSection2(), "coordinates.formSection2");
        validatePoint(coordinates.formSection3(), "coordinates.formSection3");
        validatePoint(coordinates.emailField(), "coordinates.emailField");
        validatePoint(coordinates.submit(), "coordinates.submit");
        validatePoint(coordinates.confirm(), "coordinates.confirm");
    }

    private static void validatePoint(ScreenPoint point, String name) {
        require(point != null, name + " is required");
        require(point.x() >= 0 && point.y() >= 0, name + " coordinates must not be negative");
    }

    private static void requireNotBlank(String value, String name) {
        require(value != null && !value.isBlank(), name + " must not be blank");
    }

    private static void requirePositive(int value, String name) {
        require(value > 0, name + " must be greater than zero");
    }

    private static void requireNonNegative(int value, String name) {
        require(value >= 0, name + " must not be negative");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException("Invalid config.json: " + message);
        }
    }
}

record FileConfig(String emails, String content) {
}

record BrowserConfig(String binary, int waitSeconds) {
}

record DelayConfig(
        int betweenEmailsMinMs,
        int betweenEmailsMaxMs,
        int robotAutoMs,
        int keyHoldMs,
        int clipboardMs,
        int clickBeforeMs,
        int clickHoldMs,
        int stepMs,
        int pageTransitionMs,
        int afterSubmitMs,
        int focusBeforeClickMs,
        int focusAfterClickMs) {
}

record MouseConfig(int smoothSteps, int smoothStepDelayMs, int scrollAmount) {
}

record WorkflowConfig(int categoryDownCount) {
}

record CoordinateConfig(
        ScreenPoint browserFocus,
        ScreenPoint categorySelect,
        ScreenPoint categoryOption,
        ScreenPoint categoryConfirm,
        ScreenPoint formSection1,
        ScreenPoint formSection2,
        ScreenPoint formSection3,
        ScreenPoint emailField,
        ScreenPoint submit,
        ScreenPoint confirm) {
}

record ScreenPoint(int x, int y) {
}
