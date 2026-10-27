package com.lumi2021.urna.view;

import com.lumi2021.urna.platform.InputService;

import javax.swing.*;
import java.awt.*;

public class VoterView extends JFrame {

    private final InputService inputService;

    public VoterView(InputService inputService) {
        super("Terminal da urna");
        this.inputService = inputService;
        inputService.install(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(640, 480));
        setLocationRelativeTo(null);

        initializeUI();
    }

    private void initializeUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(30, 30, 30));

        JLabel title = new JLabel("Olá, mundo");
        title.setForeground(Color.WHITE);
        title.setBorder(
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        );

        root.add(title, BorderLayout.NORTH);

        setContentPane(root);
    }

}
