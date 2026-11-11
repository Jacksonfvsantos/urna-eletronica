package com.lumi2021.urna.view;

import com.lumi2021.urna.view.models.VotingScreenModel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VotingScreen extends JPanel {

    private static final Color BG = new Color(250, 250, 245);
    private static final Color GREEN = new Color(0, 105, 65);
    private static final Color TEXT = new Color(30, 30, 30);
    private static final Color WARN = new Color(170, 30, 30);

    private static final ExecutorService IMAGE_LOADER = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "candidate-image-loader");
        t.setDaemon(true);
        return t;
    });

    private final Map<String, Image> images = new ConcurrentHashMap<>();
    private final Set<String> requestedImages = ConcurrentHashMap.newKeySet();

    private VotingScreenModel model = new VotingScreenModel.Waiting(null);

    public VotingScreen() {
        setBackground(BG);
        setPreferredSize(new Dimension(800, 500));
        setFocusable(true);
    }

    public void setModel(VotingScreenModel model) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setModel(model));
            return;
        }
        this.model = model;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            double scale = Math.min(getWidth() / 800.0, getHeight() / 500.0);
            g2.translate((getWidth() - 800 * scale) / 2, (getHeight() - 500 * scale) / 2);
            g2.scale(scale, scale);

            paintHeader(g2);

            // Pattern Matching no switch para cada subtipo da interface selada
            switch (model) {
                case VotingScreenModel.Waiting w -> paintWaiting(g2, w);
                case VotingScreenModel.Voting v -> paintVoting(g2, v);
                case VotingScreenModel.Submitting s -> paintCentered(g2, s.message(), 28, TEXT);
                case VotingScreenModel.Finished f -> paintFinished(g2);
            }
        } finally {
            g2.dispose();
        }
    }

    private void paintHeader(Graphics2D g2) {
        g2.setColor(GREEN);
        g2.fillRect(0, 0, 800, 55);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2.drawString("JUSTIÇA ELEITORAL", 24, 35);
    }

    private void paintWaiting(Graphics2D g2, VotingScreenModel.Waiting w) {
        paintCentered(g2, "AGUARDE", 44, TEXT);

        g2.setColor(new Color(90, 90, 90));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        drawCentered(g2, "Aguardando a liberação pelo mesário.", 300);

        if (w.message() != null) {
            g2.setColor(WARN);
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            drawCentered(g2, w.message(), 360);
        }
    }

    private void paintFinished(Graphics2D g2) {
        paintCentered(g2, "FIM", 120, TEXT);

        g2.setColor(new Color(90, 90, 90));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 22));
        drawCentered(g2, "VOTOU", 380);
    }

    private void paintVoting(Graphics2D g2, VotingScreenModel.Voting v) {
        g2.setColor(TEXT);
        g2.setFont(new Font("SansSerif", Font.BOLD, 25));
        g2.drawString("SEU VOTO PARA", 30, 105);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 22));
        g2.drawString(v.cargoLabel(), 30, 140);

        g2.setColor(new Color(190, 190, 190));
        g2.drawLine(30, 160, 770, 160);

        boolean blank = "VOTO EM BRANCO".equals(v.headline());

        if (!blank) {
            g2.setColor(TEXT);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 17));
            g2.drawString("Número:", 30, 200);

            int x = 30;
            int y = 215;
            int size = 55;
            int gap = 8;

            for (int i = 0; i < v.digitCount(); i++) {
                int bx = x + i * (size + gap);

                g2.setColor(Color.WHITE);
                g2.fillRect(bx, y, size, size);

                g2.setColor(TEXT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRect(bx, y, size, size);

                if (i < v.digits().length()) {
                    String digit = v.digits().substring(i, i + 1);
                    g2.setFont(new Font("Monospaced", Font.BOLD, 32));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(digit, bx + (size - fm.stringWidth(digit)) / 2, y + 38);
                }
            }
        }

        if (blank) {
            g2.setColor(TEXT);
            g2.setFont(new Font("SansSerif", Font.BOLD, 40));
            g2.drawString(v.headline(), 30, 260);
        }
        else if (v.candidateName() != null) {
            g2.setColor(TEXT);
            g2.setFont(new Font("SansSerif", Font.BOLD, 21));
            g2.drawString("Nome: " + v.candidateName(), 30, 320);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            if (v.partyName() != null) {
                g2.drawString("Partido: " + v.partyName(), 30, 350);
            }
            paintPhoto(g2, v.imageUrl());
        }
        else {
            int y = 320;

            if (v.partyName() != null) {
                g2.setColor(TEXT);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
                g2.drawString("Partido: " + v.partyName(), 30, y);
                y += 30;
            }

            if (v.headline() != null) {
                g2.setColor(WARN);
                g2.setFont(new Font("SansSerif", Font.BOLD, 26));
                if (v.detail() != null) {
                    g2.drawString(v.detail(), 30, y);
                    y += 34;
                }
                g2.drawString(v.headline(), 30, y);
            }
            else if (v.digits().isEmpty()) {
                g2.setColor(new Color(90, 90, 90));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
                g2.drawString("Digite o número do candidato.", 30, 310);
            }
        }

        // Rodapé de instruções
        g2.setColor(new Color(235, 235, 230));
        g2.fillRect(0, 425, 800, 75);

        g2.setColor(TEXT);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2.drawString("Aperte a tecla:", 25, 444);
        g2.drawString("VERDE para CONFIRMAR este voto", 25, 462);
        g2.drawString("LARANJA para REINICIAR este voto", 25, 480);
        if (v.digits().isEmpty() && !blank) {
            g2.drawString("BRANCO para votar em branco", 450, 462);
        }
    }

    private void paintPhoto(Graphics2D g2, String imageUrl) {
        final int bx = 560, by = 185, bw = 180, bh = 210;

        g2.setColor(new Color(225, 225, 225));
        g2.fillRect(bx, by, bw, bh);

        Image img = imageFor(imageUrl);
        if (img != null) {
            int iw = img.getWidth(null);
            int ih = img.getHeight(null);
            if (iw > 0 && ih > 0) {
                double s = Math.min((double) bw / iw, (double) bh / ih);
                int w = (int) (iw * s);
                int h = (int) (ih * s);
                g2.drawImage(img, bx + (bw - w) / 2, by + (bh - h) / 2, w, h, null);
                return;
            }
        }

        // Silhueta enquanto a foto não carrega (ou se falhar)
        g2.setColor(new Color(180, 140, 110));
        g2.fillOval(620, 215, 60, 70);
        g2.setColor(new Color(55, 85, 125));
        g2.fillRoundRect(590, 295, 120, 90, 30, 30);
    }

    private void paintCentered(Graphics2D g2, String text, int fontSize, Color color) {
        g2.setColor(color);
        g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        drawCentered(g2, text, 250 + fontSize / 3);
    }

    private void drawCentered(Graphics2D g2, String text, int baseline) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (800 - fm.stringWidth(text)) / 2, baseline);
    }

    private Image imageFor(String url) {
        if (url == null || url.isBlank()) return null;

        Image cached = images.get(url);
        if (cached != null) return cached;

        if (requestedImages.add(url)) {
            IMAGE_LOADER.execute(() -> {
                try {
                    URLConnection connection = URI.create(url).toURL().openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(8000);

                    try (InputStream in = connection.getInputStream()) {
                        BufferedImage loaded = ImageIO.read(in);
                        if (loaded != null) images.put(url, loaded);
                    }
                } catch (Exception ignored) {
                    // Ignored
                }
                SwingUtilities.invokeLater(this::repaint);
            });
        }
        return null;
    }
}
