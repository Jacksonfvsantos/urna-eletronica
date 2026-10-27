package com.lumi2021.urna.view;

import com.lumi2021.urna.domain.models.bulletinDocument.BulletinDocument;
import com.lumi2021.urna.domain.models.bulletinDocument.BulletinElement;
import com.lumi2021.urna.domain.models.bulletinDocument.BulletinLine;

import javax.swing.*;
import java.awt.*;

public final class BulletinView extends JFrame {

    public BulletinView(BulletinDocument document) {
        super("Boletim");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        BulletinRenderer renderer = new BulletinRenderer(document);

        JScrollPane scrollPane = new JScrollPane(renderer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        setContentPane(scrollPane);

        setSize(350, 700);
        setLocationRelativeTo(null);
    }

    private final class BulletinRenderer extends JPanel {

        private static final int PADDING = 12;
        private static final int LINE_HEIGHT = 20;

        private final BulletinDocument document;

        public BulletinRenderer(BulletinDocument document) {
            this.document = document;

            setBackground(Color.WHITE);
            setForeground(Color.BLACK);
            setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics metrics = getFontMetrics(getFont());

            int width = metrics.charWidth('M') * 40 + PADDING * 2;
            int height = document.elements().size() * LINE_HEIGHT + PADDING * 2;

            return new Dimension(width, height);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g = (Graphics2D) graphics.create();

            try {
                g.setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                );

                FontMetrics metrics = g.getFontMetrics();
                int y = PADDING + metrics.getAscent();

                for (BulletinElement element : document.elements()) {
                    if (!(element instanceof BulletinLine line)) continue;

                    int style = switch (line.textMode()) {
                        case Normal -> Font.PLAIN;
                        case Bold -> Font.BOLD;
                        case Italic -> Font.ITALIC;
                    };

                    g.setFont(getFont().deriveFont(style));
                    g.setColor(getForeground());

                    g.drawString(line.content(), PADDING, y);
                    y += LINE_HEIGHT;
                }
            } finally {
                g.dispose();
            }
        }
    }
}
