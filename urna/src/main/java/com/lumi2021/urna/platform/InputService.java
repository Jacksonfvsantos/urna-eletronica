package com.lumi2021.urna.platform;

import javax.swing.*;
import java.awt.*;

public interface InputService extends KeyEventDispatcher {
    void install(JFrame window);
    void uninstall();
}
