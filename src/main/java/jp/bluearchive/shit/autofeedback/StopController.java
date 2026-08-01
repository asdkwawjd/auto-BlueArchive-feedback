package jp.bluearchive.shit.autofeedback;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

final class StopController implements AutoCloseable {

    private volatile boolean running = true;
    private boolean registered;

    void register() {
        try {
            GlobalScreen.registerNativeHook();
            registered = true;
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
                @Override
                public void nativeKeyPressed(NativeKeyEvent event) {
                    if (event.getKeyCode() == NativeKeyEvent.VC_F2) {
                        running = false;
                        System.out.println("\n[F2]Finish soon...");
                    }
                }
            });
            System.out.println("[INFO]Stop");
        } catch (Exception e) {
            System.err.println("[WARN]Error" + e.getMessage());
        }
    }

    boolean isRunning() {
        return running;
    }

    @Override
    public void close() {
        if (!registered) {
            return;
        }
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (Exception ignored) {
        } finally {
            registered = false;
        }
    }
}
