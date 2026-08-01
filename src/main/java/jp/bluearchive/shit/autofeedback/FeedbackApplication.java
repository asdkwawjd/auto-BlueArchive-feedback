package jp.bluearchive.shit.autofeedback;

import java.awt.AWTException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class FeedbackApplication {

    void run() {
        Path appHome = ApplicationPaths.home();
        Path configFile = appHome.resolve("config.json");
        AppConfig config;
        try {
            config = AppConfig.load(configFile);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("[ERROR] cant load config.json: " + e.getMessage());
            return;
        }

        Path emailsFile = ApplicationPaths.resolve(appHome, config.files().emails());
        Path contentFile = ApplicationPaths.resolve(appHome, config.files().content());
        String browserBinary = config.browser().binary().trim();
        if (!browserBinary.isEmpty() && !Files.isRegularFile(ApplicationPaths.resolve(appHome, browserBinary))) {
            System.err.println("[ERROR] cant find Chrome binary: " + browserBinary);
            return;
        }

        StopController stopController = new StopController();
        stopController.register();

        FeedbackFiles files = new FeedbackFiles();
        List<String> emails = files.readLines(emailsFile);
        String content = files.readContent(contentFile);

        if (emails.isEmpty()) {
            System.out.println("[ERROR] cant find emails in " + emailsFile);
            stopController.close();
            return;
        }
        if (content.isBlank()) {
            System.out.println("[ERROR] cant find anything in " + contentFile);
            stopController.close();
            return;
        }

        NativeAutomation automation;
        try {
            automation = new NativeAutomation(stopController, config);
        } catch (AWTException e) {
            System.err.println("[ERROR]Error" + e.getMessage());
            stopController.close();
            return;
        }

        FeedbackSubmitter submitter = null;
        try {
            submitter = new FeedbackSubmitter(automation, stopController, config);
            printWelcome(emails, content);
            processEmails(emails, content, emailsFile, files, automation, submitter, stopController, config);
        } finally {
            if (submitter != null) {
                submitter.close();
            }
            stopController.close();
            System.out.println("\n==========================================");
            System.out.println("  Finish");
            System.out.println("==========================================");
        }
    }

    private void processEmails(
            List<String> emails,
            String content,
            Path emailsFile,
            FeedbackFiles files,
            NativeAutomation automation,
            FeedbackSubmitter submitter,
            StopController stopController,
            AppConfig config) {
        boolean isFirst = true;
        for (int i = 0; i < emails.size(); i++) {
            if (!stopController.isRunning()) {
                System.out.println("\n[STOP]Stop");
                break;
            }

            String email = emails.get(i).trim();
            if (email.isEmpty()) {
                continue;
            }

            System.out.println("\n[" + (i + 1) + "/" + emails.size() + "] Processing: " + email);

            try {
                submitter.submit(email, content, isFirst);
                isFirst = false;
                System.out.println("  >>> Succeed: " + email);
                files.removeLine(emailsFile, email);
            } catch (Exception e) {
                if (!stopController.isRunning()) {
                    break;
                }
                System.err.println("  >>> Failed: " + email + " - " + e.getMessage());
                submitter.resetPage();
            }

            automation.randomDelay(
                    config.delays().betweenEmailsMinMs(),
                    config.delays().betweenEmailsMaxMs());
        }
    }

    private void printWelcome(List<String> emails, String content) {
        System.out.println("==========================================");
        System.out.println("Welcome to BlueArchive Auto Feedback Tool");
        System.out.println("Press F2 to Stop");
        System.out.println("==========================================");
        System.out.println("Quantity: " + emails.size());
        System.out.println("Content: " + content.substring(0, Math.min(50, content.length())) + "...");
        System.out.println("==========================================");
    }
}
