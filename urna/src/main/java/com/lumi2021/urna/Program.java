package com.lumi2021.urna;

import com.lumi2021.urna.application.BallotBox;
import com.lumi2021.urna.controller.VoterController;
import com.lumi2021.urna.controller.poolWorker.DesktopPoolWorkerController;
import com.lumi2021.urna.controller.poolWorker.EmbeddedPoolWorkerController;
import com.lumi2021.urna.controller.poolWorker.PoolWorkerController;
import com.lumi2021.urna.platform.DesktopInputService;
import com.lumi2021.urna.platform.InputService;
import com.lumi2021.urna.view.*;
import java.awt.event.*;
import javax.swing.*;

public class Program {

    static InputService inputService;

    static PoolWorkerView poolWorkerView;
    static VoterView voterView;
    static BulletinView bulletinView;

    static PoolWorkerController poolWorkerController;
    static VoterController voterController;
    static VotingScreenEmbeddedContainer votingScreenEmbeddedContainer;

    static BallotBox ballotBox;

    public static void main(String[] args) {

        boolean isEmbedded = args.length >= 1 && (args[0].equals("--embedded") || args[0].equals("-e"));

        WindowAdapter closeHandler = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        };

        if (!isEmbedded) {
            // Executando no modo desktop

            inputService = new DesktopInputService();

            poolWorkerView = new PoolWorkerView(inputService);
            voterView = new VoterView(inputService);

            poolWorkerController = new DesktopPoolWorkerController();
            voterController = new VoterController();

            ballotBox = new BallotBox();
            var result = ballotBox.emitBulletin();
            //IO.println(result);

            bulletinView = new BulletinView(result);

            SwingUtilities.invokeLater(() -> {
                poolWorkerView.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                voterView.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                bulletinView.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

                poolWorkerView.addWindowListener(closeHandler);
                voterView.addWindowListener(closeHandler);
                bulletinView.addWindowListener(closeHandler);

                poolWorkerView.setVisible(true);
                voterView.setVisible(true);
                bulletinView.setVisible(true);
            });
        }
        else {
            // Executando no modo embarcado

            inputService = new DesktopInputService();

            poolWorkerController = new DesktopPoolWorkerController();
            voterController = new VoterController();
            votingScreenEmbeddedContainer = new VotingScreenEmbeddedContainer(inputService);

            ballotBox = new BallotBox();

            SwingUtilities.invokeLater(() -> {
                votingScreenEmbeddedContainer.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                votingScreenEmbeddedContainer.addWindowListener(closeHandler);
                votingScreenEmbeddedContainer.setVisible(true);
            });
        }

    }

}
