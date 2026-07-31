package jp.bluearchive.shit;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

final class NativeAutomation {

    private final Robot robot;
    private final StopController stopController;
    private final DelayConfig delays;
    private final MouseConfig mouse;
    private final CoordinateConfig coordinates;

    NativeAutomation(StopController stopController, AppConfig config) throws AWTException {
        this.stopController = stopController;
        delays = config.delays();
        mouse = config.mouse();
        coordinates = config.coordinates();
        robot = new Robot();
        robot.setAutoDelay(delays.robotAutoMs());
    }

    void tab() {
        pressKey(KeyEvent.VK_TAB);
    }

    void enter() {
        pressKey(KeyEvent.VK_ENTER);
    }

    void down() {
        pressKey(KeyEvent.VK_DOWN);
    }

    void paste(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        robot.delay(delays.clipboardMs());
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.delay(delays.keyHoldMs());
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    void ctrlL() {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_L);
        robot.delay(delays.keyHoldMs());
        robot.keyRelease(KeyEvent.VK_L);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    void click(int x, int y) {
        System.out.println("    -> Robot 移动并点击 (" + x + ", " + y + ")");
        smoothMove(x, y);
        robot.delay(delays.clickBeforeMs());
        leftClick();
    }

    void click(ScreenPoint point) {
        click(point.x(), point.y());
    }

    void focusBrowser() {
        ScreenPoint point = coordinates.browserFocus();
        smoothMove(point.x(), point.y());
        System.out.println("    -> focusBrowser click (" + point.x() + ", " + point.y() + ")");
        robot.delay(delays.focusBeforeClickMs());
        leftClick();
        robot.delay(delays.focusAfterClickMs());
    }

    void scrollDown() {
        robot.mouseWheel(mouse.scrollAmount());
    }

    void leftClick() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(delays.clickHoldMs());
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    void delay(int milliseconds) {
        robot.delay(milliseconds);
    }

    void randomDelay(int min, int max) {
        if (!stopController.isRunning()) {
            return;
        }
        robot.delay(min + (int) (Math.random() * (max - min)));
    }

    private void pressKey(int keyCode) {
        robot.keyPress(keyCode);
        robot.delay(delays.keyHoldMs());
        robot.keyRelease(keyCode);
    }

    private void smoothMove(int targetX, int targetY) {
        if (!stopController.isRunning()) {
            return;
        }
        java.awt.Point current = MouseInfo.getPointerInfo().getLocation();
        int steps = mouse.smoothSteps();
        for (int i = 1; i <= steps; i++) {
            if (!stopController.isRunning()) {
                return;
            }
            int x = current.x + (targetX - current.x) * i / steps;
            int y = current.y + (targetY - current.y) * i / steps;
            robot.mouseMove(x, y);
            robot.delay(mouse.smoothStepDelayMs());
        }
        robot.mouseMove(targetX, targetY);
    }
}
