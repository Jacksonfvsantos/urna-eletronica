package com.lumi2021.urna.platform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class DesktopInputService implements InputService {

    public void install(JFrame window) {
        KeyboardFocusManager
            .getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher(this);
    }
    public void uninstall() {
        KeyboardFocusManager
            .getCurrentKeyboardFocusManager()
            .removeKeyEventDispatcher(this);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getID()) {
            case KeyEvent.KEY_PRESSED -> onKeyPressed(event.getKeyCode());
            case KeyEvent.KEY_RELEASED -> onKeyReleased(event.getKeyCode());
            case KeyEvent.KEY_TYPED -> onCharacterTyped(event.getKeyChar());
        }

        return true;
    }

    private void onKeyPressed(int keyCode) {
        System.out.println("Key pressed: " + keyCode);
    }
    private void onKeyReleased(int keyCode) {
        System.out.println("Key released: " + keyCode);
    }
    private void onCharacterTyped(char character) {
        System.out.println("Character: " + character);
    }
}
