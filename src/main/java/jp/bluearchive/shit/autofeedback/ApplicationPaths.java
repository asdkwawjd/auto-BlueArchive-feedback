package jp.bluearchive.shit.autofeedback;

import java.nio.file.Path;
import java.nio.file.Paths;

final class ApplicationPaths {

    private static final String APP_HOME_PROPERTY = "app.home";

    private ApplicationPaths() {
    }

    static Path home() {
        String appHome = System.getProperty(APP_HOME_PROPERTY);
        if (appHome == null || appHome.isBlank()) {
            return Paths.get("").toAbsolutePath().normalize();
        }
        return Paths.get(appHome).toAbsolutePath().normalize();
    }

    static Path resolve(Path home, String configuredPath) {
        Path path = Paths.get(configuredPath);
        return path.isAbsolute() ? path.normalize() : home.resolve(path).normalize();
    }
}
