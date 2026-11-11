package com.lumi2021.urna.platform;

import com.lumi2021.urna.platform.events.InputAction;
import com.lumi2021.urna.platform.events.InputListener;

import javax.swing.*;

public interface InputService{
    void install(JFrame window);
    void uninstall();

    void emit(InputAction action);
    void addListener(InputListener listener);
    void removeListener(InputListener listener);
}
