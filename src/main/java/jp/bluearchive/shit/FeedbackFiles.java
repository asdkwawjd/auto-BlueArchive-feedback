package jp.bluearchive.shit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

final class FeedbackFiles {

    List<String> readLines(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createFile(path);
                return List.of();
            }
            return Files.readAllLines(path).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return List.of();
        }
    }

    String readContent(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createFile(path);
                return "";
            }
            return Files.readString(path).trim();
        } catch (IOException e) {
            return "";
        }
    }

    void removeLine(Path path, String email) {
        try {
            List<String> lines = Files.readAllLines(path);
            lines.removeIf(line -> line.trim().equals(email));
            Files.write(path, lines);
            System.out.println("  [deleted this email in emails.txt: " + email + "]");
        } catch (IOException e) {
            System.err.println("  [WARN] cant update emails.txt: " + e.getMessage());
        }
    }
}
