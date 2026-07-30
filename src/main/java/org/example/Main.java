package org.example;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main {

    private static volatile boolean running = true;

    private static Robot robot;
    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String TARGET_URL = "https://bluearchive.jp/contact-1-hint";
    private static final Path EMAILS_FILE = Paths.get("emails.txt");
    private static final Path CONTENT_FILE = Paths.get("content.txt");

    public static void main(String[] args) {
        registerF2Hotkey();

        List<String> emails = readLines(EMAILS_FILE);
        String content = readContent(CONTENT_FILE);

        if (emails.isEmpty()) {
            System.out.println("[ERROR] emails.txt 中没有找到任何邮箱地址，请添加邮箱后重试。");
            return;
        }
        if (content.isBlank()) {
            System.out.println("[ERROR] content.txt 中没有找到反馈内容，请填写内容后重试。");
            return;
        }

        try {
            robot = new Robot();
            robot.setAutoDelay(20);
        } catch (AWTException e) {
            System.err.println("[ERROR] 无法初始化 Robot: " + e.getMessage());
            return;
        }

        Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
        Logger.getLogger("io.github.bonigarcia").setLevel(Level.OFF);
        Logger.getLogger("com.github.kwhat.jnativehook").setLevel(Level.OFF);

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:\\Users\\Administrator\\AppData\\Local\\Google\\Chrome\\Bin\\chromex.exe");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        System.out.println("==========================================");
        System.out.println("  蔚蓝档案 自动反馈脚本");
        System.out.println("  按 F2 键随时停止");
        System.out.println("==========================================");
        System.out.println("邮箱数量: " + emails.size());
        System.out.println("反馈内容: " + content.substring(0, Math.min(50, content.length())) + "...");
        System.out.println("==========================================");

        try {
            boolean isFirst = true;
            for (int i = 0; i < emails.size(); i++) {
                if (!running) {
                    System.out.println("\n[STOP] 用户按下了 F2，脚本已停止。");
                    break;
                }

                String email = emails.get(i).trim();
                if (email.isEmpty()) continue;

                System.out.println("\n[" + (i + 1) + "/" + emails.size() + "] 正在处理: " + email);

                try {
                    submitFeedback(email, content, isFirst);
                    isFirst = false;
                    System.out.println("  >>> 提交完成: " + email);
                    removeFromFile(EMAILS_FILE, email);
                } catch (Exception e) {
                    if (!running) break;
                    System.err.println("  >>> 提交失败: " + email + " - " + e.getMessage());
                    try { driver.navigate().to(TARGET_URL); } catch (Exception ignored) {}
                }

                randomDelay(2000, 4000);
            }
        } finally {
            driver.quit();
            try { GlobalScreen.unregisterNativeHook(); } catch (Exception ignored) {}
            System.out.println("\n==========================================");
            System.out.println("  脚本执行完毕");
            System.out.println("==========================================");
        }
    }

    // ==================== F2 热键 ====================

    private static void registerF2Hotkey() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
                @Override
                public void nativeKeyPressed(NativeKeyEvent e) {
                    if (e.getKeyCode() == NativeKeyEvent.VC_F2) {
                        running = false;
                        System.out.println("\n[F2] 停止信号已接收，当前操作完成后将退出...");
                    }
                }
            });
            System.out.println("[INFO] F2 热键已注册，按 F2 即可随时停止脚本。");
        } catch (Exception e) {
            System.err.println("[WARN] 无法注册 F2 热键: " + e.getMessage());
        }
    }

    // ==================== 原生键盘工具（全部用 Robot，不依赖 Selenium Actions） ====================

    private static void nativeTab() {
        robot.keyPress(KeyEvent.VK_TAB);
        robot.delay(30);
        robot.keyRelease(KeyEvent.VK_TAB);
    }

    private static void nativeEnter() {
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.delay(30);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }

    private static void nativeDown() {
        robot.keyPress(KeyEvent.VK_DOWN);
        robot.delay(30);
        robot.keyRelease(KeyEvent.VK_DOWN);
    }

    private static void nativeSpace() {
        robot.keyPress(KeyEvent.VK_SPACE);
        robot.delay(30);
        robot.keyRelease(KeyEvent.VK_SPACE);
    }

    private static void nativePaste(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(text), null);
        robot.delay(50);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.delay(30);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    private static void nativeF5() {
        robot.keyPress(KeyEvent.VK_F5);
        robot.delay(30);
        robot.keyRelease(KeyEvent.VK_F5);
    }

    private static void nativeCtrlL() {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_L);
        robot.delay(30);
        robot.keyRelease(KeyEvent.VK_L);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    private static void nativeType(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(text), null);
        robot.delay(50);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.delay(30);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    private static void nativeClick(int x, int y) {
        System.out.println("    -> Robot 移动并点击 (" + x + ", " + y + ")");
        smoothMove(x, y);
        robot.delay(80);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(50);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    private static void nativeScrollDown() {
        robot.mouseWheel(3);
    }

    private static void smoothMove(int targetX, int targetY) {
        if (!running) return;
        java.awt.Point cur = MouseInfo.getPointerInfo().getLocation();
        int steps = 8;
        for (int i = 1; i <= steps; i++) {
            if (!running) return;
            int mx = cur.x + (targetX - cur.x) * i / steps;
            int my = cur.y + (targetY - cur.y) * i / steps;
            robot.mouseMove(mx, my);
            robot.delay(10);
        }
        robot.mouseMove(targetX, targetY);
    }

    // ==================== 主流程 ====================

    private static void submitFeedback(String email, String content, boolean isFirst) {
        driver.get(TARGET_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("select")));

        focusBrowser();

        if (isFirst) {
            // ====== 首次：键盘操作选下拉菜单 ======
            step("[1] tab × 1",  Main::nativeTab);
            step("[2] enter",    Main::nativeEnter);
            step("[3] ↓×4",     () -> { for (int i = 0; i < 4; i++) nativeDown(); });
            step("[4] enter",    Main::nativeEnter);
            step("[5] tab",      Main::nativeTab);
            step("[6] enter",    Main::nativeEnter);
            step("[7] 等1.5s",   () -> robot.delay(1500));
        } else {
            // ====== 后续：鼠标点击选下拉菜单 ======
            step("[1] 移到696,774 + 点击", () -> nativeClick(696, 774));
            step("[2] 移到677,868 + 点击", () -> nativeClick(677, 868));
            step("[3] 移到923,838 + 点击", () -> nativeClick(923, 838));
            step("[4] 等1.5s",              () -> robot.delay(1500));
        }

        // ====== 以下全部用鼠标点击 ======

        step("[8] 点500,480",  () -> nativeClick(500, 480));
        step("[9] 点500,650",  () -> nativeClick(500, 650));
        step("[10] 点500,850", () -> nativeClick(500, 850));
        step("[11] 点500,956", () -> nativeClick(500, 956));

        step("[12] 粘贴邮箱",   () -> nativePaste(email));
        step("[13] tab",        Main::nativeTab);
        step("[14] 粘贴邮箱",   () -> nativePaste(email));
        step("[15] tab×2",      () -> { nativeTab(); nativeTab(); });
        step("[16] 粘贴意见",   () -> nativePaste(content));

        step("[17] 点1064,902", () -> nativeClick(1064, 902));
        step("[18] 滚轮下滚",   Main::nativeScrollDown);
        step("[19] 点945,795",  () -> nativeClick(945, 795));

        robot.delay(1000);

        step("[20] Ctrl+L",      Main::nativeCtrlL);
        step("[21] 输入URL",      () -> nativeType(TARGET_URL));
        step("[22] Enter",        Main::nativeEnter);
        step("[23] 等待1.5秒",    () -> robot.delay(1500));
        step("[24] 鼠标左键",     () -> {
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.delay(30);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        });
    }

    private static void focusBrowser() {
        // 把鼠标移到浏览器页面中央点一下，确保焦点在页面上
        smoothMove(600, 360);
        System.out.println("    -> focusBrowser 点击 (600, 360)");
        robot.delay(100);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(30);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(300);
    }

    // ==================== 步骤工具 ====================

    private static void step(String label, Runnable action) {
        if (!running) return;
        System.out.println("  " + label);
        action.run();
        robot.delay(150);
    }

    // ==================== 工具 ====================

    private static void randomDelay(int min, int max) {
        if (!running) return;
        robot.delay(min + (int) (Math.random() * (max - min)));
    }

    private static void removeFromFile(Path path, String email) {
        try {
            List<String> lines = Files.readAllLines(path);
            lines.removeIf(line -> line.trim().equals(email));
            Files.write(path, lines);
            System.out.println("  [已从 emails.txt 中删除: " + email + "]");
        } catch (IOException e) {
            System.err.println("  [WARN] 无法更新 emails.txt: " + e.getMessage());
        }
    }

    private static List<String> readLines(Path path) {
        try {
            if (!Files.exists(path)) { Files.createFile(path); return List.of(); }
            return Files.readAllLines(path).stream().map(String::trim).filter(l -> !l.isEmpty()).collect(Collectors.toList());
        } catch (IOException e) { return List.of(); }
    }

    private static String readContent(Path path) {
        try {
            if (!Files.exists(path)) { Files.createFile(path); return ""; }
            return Files.readString(path).trim();
        } catch (IOException e) { return ""; }
    }
}
