package jp.bluearchive.shit;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

final class FeedbackSubmitter implements AutoCloseable {

    private static final String TARGET_URL = "https://bluearchive.jp/contact-1-hint";

    private final NativeAutomation automation;
    private final StopController stopController;
    private final AppConfig config;
    private final WebDriver driver;
    private final WebDriverWait wait;

    FeedbackSubmitter(NativeAutomation automation, StopController stopController, AppConfig config) {
        this.automation = automation;
        this.stopController = stopController;
        this.config = config;

        Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
        Logger.getLogger("io.github.bonigarcia").setLevel(Level.OFF);
        Logger.getLogger("com.github.kwhat.jnativehook").setLevel(Level.OFF);

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        String browserBinary = config.browser().binary().trim();
        if (!browserBinary.isEmpty()) {
            options.setBinary(ApplicationPaths.resolve(ApplicationPaths.home(), browserBinary).toString());
        }
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(config.browser().waitSeconds()));
    }

    void submit(String email, String content, boolean isFirst) {
        driver.get(TARGET_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("select")));
        automation.focusBrowser();

        if (isFirst) {
            step("[1] tab × 1", automation::tab);
            step("[2] enter", automation::enter);
            step("[3] ↓×4", () -> {
                for (int i = 0; i < config.workflow().categoryDownCount(); i++) {
                    automation.down();
                }
            });
            step("[4] enter", automation::enter);
            step("[5] tab", automation::tab);
            step("[6] enter", automation::enter);
            step("[7] Wait for page transition", () -> automation.delay(config.delays().pageTransitionMs()));
        } else {
            step("[1] Select category", () -> automation.click(config.coordinates().categorySelect()));
            step("[2] Select category option", () -> automation.click(config.coordinates().categoryOption()));
            step("[3] Confirm category", () -> automation.click(config.coordinates().categoryConfirm()));
            step("[4] Wait for page transition", () -> automation.delay(config.delays().pageTransitionMs()));
        }

        step("[8] Click form section 1", () -> automation.click(config.coordinates().formSection1()));
        step("[9] Click form section 2", () -> automation.click(config.coordinates().formSection2()));
        step("[10] Click form section 3", () -> automation.click(config.coordinates().formSection3()));
        step("[11] Click email field", () -> automation.click(config.coordinates().emailField()));
        step("[12] Paste email", () -> automation.paste(email));
        step("[13] tab", automation::tab);
        step("[14] Paste email", () -> automation.paste(email));
        step("[15] tab×2", () -> {
            automation.tab();
            automation.tab();
        });
        step("[16] Paste content", () -> automation.paste(content));
        step("[17] Click submit", () -> automation.click(config.coordinates().submit()));
        step("[18] down", automation::scrollDown);
        step("[19] Click confirm", () -> automation.click(config.coordinates().confirm()));

        automation.delay(config.delays().afterSubmitMs());
        step("[20] Ctrl+L", automation::ctrlL);
        step("[21] In put URL", () -> automation.paste(TARGET_URL));
        step("[22] Enter", automation::enter);
        step("[23] Wait for page transition", () -> automation.delay(config.delays().pageTransitionMs()));
        step("[24] Left click", automation::leftClick);
    }

    void resetPage() {
        try {
            driver.navigate().to(TARGET_URL);
        } catch (Exception ignored) {
        }
    }

    private void step(String label, Runnable action) {
        if (!stopController.isRunning()) {
            return;
        }
        System.out.println("  " + label);
        action.run();
        automation.delay(config.delays().stepMs());
    }

    @Override
    public void close() {
        driver.quit();
    }
}
