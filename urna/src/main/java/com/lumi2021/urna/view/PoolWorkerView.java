package com.lumi2021.urna.view;

import com.lumi2021.urna.controller.poolWorker.PoolWorkerController;
import com.lumi2021.urna.platform.InputService;
import com.lumi2021.urna.platform.events.InputAction;
import com.lumi2021.urna.platform.events.InputListener;
import com.lumi2021.urna.view.models.MesarioScreenModel;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class PoolWorkerView extends JFrame implements InputListener {

    private static final int LCD_COLS = 40;
    private static final int LCD_ROWS = 2;

    private final PoolWorkerController controller;

    // Visor e indicadores visuais originais
    private LcdScreen lcd;
    private Led ledBateria;
    private Led ledAguarde;
    private Led ledLiberado;

    // Teclado
    private final JButton[] btnsNumericos = new JButton[10];
    private JButton btnCorrige;
    private JButton btnConfirma;

    public PoolWorkerView(InputService inputService, PoolWorkerController controller) {
        super("Terminal do mesário");
        this.controller = controller;
        inputService.install(this);

        configurarJanela();
        inicializarComponentes();

        controller.setViewStateListener(this::render);
    }

    /** Atualiza os componentes visuais com o estado remoto enviado pelo Controller. */
    public void render(MesarioScreenModel state) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> render(state));
            return;
        }

        lcd.setLines(new String[]{ state.lcdLine1(), state.lcdLine2() });
        ledLiberado.setOn(state.ledLiberadoOn());
        ledAguarde.setOn(state.ledAguardeOn());

        boolean enabled = state.tecladoAtivo();
        for (JButton btn : btnsNumericos) {
            if (btn != null) {
                btn.setEnabled(enabled);
            }
        }
        btnCorrige.setEnabled(enabled);
        btnConfirma.setEnabled(enabled);
    }

    @Override
    public void onInput(InputAction inputAction) {
        switch (inputAction) {
            case KEY_0 -> controller.handleInput(InputAction.KEY_0);
            case KEY_1 -> controller.handleInput(InputAction.KEY_1);
            case KEY_2 -> controller.handleInput(InputAction.KEY_2);
            case KEY_3 -> controller.handleInput(InputAction.KEY_3);
            case KEY_4 -> controller.handleInput(InputAction.KEY_4);
            case KEY_5 -> controller.handleInput(InputAction.KEY_5);
            case KEY_6 -> controller.handleInput(InputAction.KEY_6);
            case KEY_7 -> controller.handleInput(InputAction.KEY_7);
            case KEY_8 -> controller.handleInput(InputAction.KEY_8);
            case KEY_9 -> controller.handleInput(InputAction.KEY_9);
            case CORRIGE -> controller.handleInput(InputAction.CORRIGE);
            case CONFIRMA -> controller.handleInput(InputAction.CONFIRMA);
        }
    }

    // ==================== JANELA E COMPONENTES VISUAIS ORIGINAIS ====================

    private void configurarJanela() {
        setTitle("Terminal do Mesário - Justiça Eleitoral");
        setSize(650, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(215, 215, 210));

        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/mesario-icon.png")));
        setIconImage(icon.getImage());
    }

    private void inicializarComponentes() {
        // ==================== TOPO: LCD ====================

        lcd = new LcdScreen(LCD_COLS, LCD_ROWS);
        lcd.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x2B2B2B), 8),
                BorderFactory.createLineBorder(new Color(0x111111), 2)
        ));

        JPanel panelTopo = new JPanel(new BorderLayout());
        panelTopo.setBackground(new Color(0xD7D7D2));
        panelTopo.setBorder(BorderFactory.createEmptyBorder(20, 25, 16, 25));
        panelTopo.add(lcd, BorderLayout.CENTER);

        // ==================== TECLADO ====================

        JPanel panelTeclado = new JPanel(new BorderLayout(0, 15));
        panelTeclado.setBackground(new Color(0xE8E9E5));
        panelTeclado.setBorder(BorderFactory.createEmptyBorder(15, 20, 20, 20));

        JLabel lblTopo = new JLabel("JUSTIÇA ELEITORAL", SwingConstants.CENTER);
        lblTopo.setForeground(Color.BLACK);
        lblTopo.setFont(new Font("Arial", Font.BOLD, 16));
        panelTeclado.add(lblTopo, BorderLayout.NORTH);

        JPanel panelNum = new JPanel(new GridLayout(4, 3, 8, 8));
        panelNum.setOpaque(false);
        panelNum.setPreferredSize(new Dimension(270, 240));

        for (int n = 1; n <= 9; n++) {
            panelNum.add(criarTeclaNumerica(n));
        }

        btnCorrige = criarBotaoAcao("CORRIGE", new Color(230, 80, 40), Color.BLACK, 11);
        btnConfirma = criarBotaoAcao("CONFIRMA", new Color(0, 180, 80), Color.BLACK, 10);
        btnCorrige.addActionListener(e -> controller.handleInput(InputAction.CORRIGE));
        btnConfirma.addActionListener(e -> controller.handleInput(InputAction.CONFIRMA));

        panelNum.add(btnCorrige);
        panelNum.add(criarTeclaNumerica(0));
        panelNum.add(btnConfirma);

        // Linha do dispositivo: bateria | teclado | indicadores
        JPanel rowDispositivo = new JPanel(new BorderLayout(20, 0));
        rowDispositivo.setOpaque(false);
        rowDispositivo.add(centralizarVertical(new BatteryDisplay()), BorderLayout.WEST);
        rowDispositivo.add(panelNum, BorderLayout.CENTER);
        rowDispositivo.add(centralizarVertical(criarPainelIndicadores()), BorderLayout.EAST);

        JPanel wrapperDispositivo = new JPanel(new GridBagLayout());
        wrapperDispositivo.setOpaque(false);
        wrapperDispositivo.add(rowDispositivo);

        panelTeclado.add(wrapperDispositivo, BorderLayout.CENTER);

        // ==================== LAYOUT PRINCIPAL ====================

        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.add(panelTopo, BorderLayout.NORTH);
        mainPane.add(panelTeclado, BorderLayout.CENTER);

        setContentPane(mainPane);
    }

    private JPanel centralizarVertical(JComponent componente) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.add(componente);
        return p;
    }

    private JPanel criarPainelIndicadores() {
        JPanel plate = new JPanel(new GridLayout(3, 1, 0, 6));
        plate.setBackground(new Color(0xF4F4F0));
        plate.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x9A9A96)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        ledBateria = new Led(new Color(0xE0A020));
        ledAguarde = new Led(new Color(0xE0A020));
        ledLiberado = new Led(new Color(0x2ECC71));

        plate.add(criarLinhaLed("BATERIA INTERNA", ledBateria));
        plate.add(criarLinhaLed("AGUARDE", ledAguarde));
        plate.add(criarLinhaLed("LIBERADO", ledLiberado));
        return plate;
    }

    private JPanel criarLinhaLed(String texto, Led led) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.BOLD, 11));
        lbl.setForeground(Color.BLACK);

        row.add(lbl, BorderLayout.WEST);
        row.add(led, BorderLayout.EAST);
        return row;
    }

    private JButton criarTeclaNumerica(final int num) {
        JButton btn = criarBotaoNumerico(String.valueOf(num));
        btn.addActionListener(e -> controller.handleInput(InputAction.fromInteger(num)));
        btnsNumericos[num] = btn;
        return btn;
    }

    private JButton criarBotaoNumerico(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setBackground(new Color(20, 20, 20));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        return btn;
    }

    private JButton criarBotaoAcao(String texto, Color background, Color foreground, int fontSize) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Arial", Font.BOLD, fontSize));
        btn.setBackground(background);
        btn.setForeground(foreground);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        return btn;
    }

    // ==================== COMPONENTE: LCD DE CARACTERES ORIGINAL ====================

    private static class LcdScreen extends JPanel {

        private static final Color LCD_TOP = new Color(0xB9C7AB);
        private static final Color LCD_BOTTOM = new Color(0xA9B89B);
        private static final Color LCD_INK = new Color(0x26301F);
        private static final Color LCD_GHOST = new Color(0, 0, 0, 14);
        private static final int PAD_X = 16;
        private static final int PAD_Y = 10;

        private final int cols;
        private final int rows;
        private final char[][] cells;

        LcdScreen(int cols, int rows) {
            this.cols = cols;
            this.rows = rows;
            this.cells = new char[rows][cols];
            clearCells();
            setOpaque(true);
            setBackground(LCD_TOP);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(600, 120);
        }

        void setLines(String[] lines) {
            clearCells();
            for (int r = 0; r < rows; r++) {
                String s = (lines != null && r < lines.length && lines[r] != null) ? lines[r] : "";
                for (int c = 0; c < cols && c < s.length(); c++) {
                    cells[r][c] = s.charAt(c);
                }
            }
            repaint();
        }

        private void clearCells() {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    cells[r][c] = ' ';
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Insets in = getInsets();
                int ax = in.left;
                int ay = in.top;
                int aw = getWidth() - in.left - in.right;
                int ah = getHeight() - in.top - in.bottom;
                if (aw <= 0 || ah <= 0) {
                    return;
                }

                g2.setPaint(new GradientPaint(0, ay, LCD_TOP, 0, ay + ah, LCD_BOTTOM));
                g2.fillRect(ax, ay, aw, ah);

                float cellW = (aw - 2f * PAD_X) / cols;
                float cellH = Math.min((ah - 2f * PAD_Y) / rows, cellW * 2.1f);
                float gx = ax + PAD_X;
                float gy = ay + (ah - cellH * rows) / 2f;

                float size = cellH * 0.72f;
                Font font = new Font(Font.MONOSPACED, Font.BOLD, 12).deriveFont(size);
                FontMetrics fm = g2.getFontMetrics(font);
                while (size > 6f && fm.charWidth('M') > cellW * 0.9f) {
                    size -= 1f;
                    font = font.deriveFont(size);
                    fm = g2.getFontMetrics(font);
                }
                g2.setFont(font);

                for (int r = 0; r < rows; r++) {
                    float cy = gy + r * cellH;

                    for (int c = 0; c < cols; c++) {
                        float cx = gx + c * cellW;

                        g2.setColor(LCD_GHOST);
                        g2.fillRect((int) cx + 1, (int) cy + 1, (int) cellW - 2, (int) cellH - 2);

                        char ch = cells[r][c];
                        if (ch == ' ') {
                            continue;
                        }

                        g2.setColor(LCD_INK);
                        int tx = Math.round(cx + (cellW - fm.charWidth(ch)) / 2f);
                        int ty = Math.round(cy + (cellH - fm.getHeight()) / 2f + fm.getAscent());
                        g2.drawString(String.valueOf(ch), tx, ty);
                    }
                }
            } finally {
                g2.dispose();
            }
        }
    }

    // ==================== COMPONENTE: LED ORIGINAL ====================

    private static class Led extends JComponent {

        private final Color onColor;
        private boolean on = false;

        Led(Color onColor) {
            this.onColor = onColor;
            setPreferredSize(new Dimension(16, 16));
        }

        void setOn(boolean on) {
            this.on = on;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(on ? onColor : new Color(0x6B6B6B));
                g2.fillOval(1, 1, getWidth() - 2, getHeight() - 2);
                g2.setColor(new Color(0x333333));
                g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
            } finally {
                g2.dispose();
            }
        }
    }

    // ==================== COMPONENTE: MOSTRADOR DE BATERIA ORIGINAL ====================

    private static class BatteryDisplay extends JPanel {

        BatteryDisplay() {
            setPreferredSize(new Dimension(84, 100));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(0x2B2B2B), 5));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int bw = (int) (w * 0.46);
                int bh = (int) (h * 0.60);
                int bx = (w - bw) / 2;
                int by = (int) (h * 0.24);

                Color outline = new Color(0x1E7B4A);

                g2.setColor(outline);
                g2.fillRoundRect(w / 2 - bw / 6, by - 6, bw / 3, 8, 3, 3);

                g2.setColor(new Color(0x7ED9A0));
                g2.fillRoundRect(bx, by, bw, bh, 10, 10);
                g2.setStroke(new BasicStroke(3f));
                g2.setColor(outline);
                g2.drawRoundRect(bx, by, bw, bh, 10, 10);

                int cx = bx + bw / 2;
                int top = by + 8;
                int bot = by + bh - 8;
                int mid = (top + bot) / 2;
                Polygon bolt = new Polygon(
                        new int[]{cx + 5, cx - 9, cx - 1, cx - 5, cx + 9, cx + 1},
                        new int[]{top, mid + 3, mid + 3, bot, mid - 3, mid - 3},
                        6
                );
                g2.setColor(new Color(0x1F4FD8));
                g2.fillPolygon(bolt);
            } finally {
                g2.dispose();
            }
        }
    }
}
