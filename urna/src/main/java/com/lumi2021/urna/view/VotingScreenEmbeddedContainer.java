package com.lumi2021.urna.view;

import com.lumi2021.urna.platform.InputService;

import javax.swing.*;
import java.awt.*;

public class VotingScreenEmbeddedContainer extends JFrame {

    private final InputService inputService;
    private VotingScreen screen = new VotingScreen();

    public VotingScreenEmbeddedContainer(InputService inputService) {
        super("Tela da urna");
        this.inputService = inputService;
        inputService.install(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setContentPane(screen);

        GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        setUndecorated(true);
        device.setFullScreenWindow(this);
    }
}
