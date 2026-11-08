package com.lumi2021.urna.view;

import com.lumi2021.urna.platform.InputService;

import javax.swing.*;
import java.awt.*;
import java.time.Clock;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

public class PoolWorkerView extends JFrame {

    private static final int ID_LENGTH = 12;

    // Grade do LCD (igual ao do terminal real: 2 linhas x 40 colunas)
    private static final int LCD_COLS = 40;
    private static final int LCD_ROWS = 2;

    // A votação só pode ser encerrada a partir das 17h, horário de Brasília
    private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");
    private static final LocalTime CLOSING_TIME = LocalTime.of(17, 0);

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Telas (passos) do terminal do mesário. */
    private enum Step {
        ENTER_ID,       // digitando o título do eleitor
        CONFIRM_ID,     // conferindo o título antes de liberar
        AUTHORIZED,     // urna liberada, aguardando o voto
        MENU,           // "Outras opções"
        CLOSE_ID,       // digitando o título para encerrar a votação
        CLOSED          // votação encerrada
    }

    private final InputService inputService;

    private final StringBuilder idInput = new StringBuilder();
    private final Set<String> votedIds = new HashSet<>();
    private Step step = Step.ENTER_ID;
    private String currentVoter = null;
    private int votesCount = 0;
    private int totalVoters = 0;
    private String presidentId = null;
    private Clock clock = Clock.system(BRASILIA);

    private String noticeText = null;

    private Consumer<String> voterAuthorizedListener;

    // Visor e indicadores
    private LcdScreen lcd;
    private Led ledBateria;
    private Led ledAguarde;
    private Led ledLiberado;
    private Timer clockTimer;

    // Teclado
    private final JButton[] btnsNumericos = new JButton[10];
    private JButton btnCorrige;
    private JButton btnConfirma;

    public PoolWorkerView(InputService inputService) {
        super("Terminal do mesário");
        this.inputService = inputService;
        inputService.install(this);

        configurarJanela();
        inicializarComponentes();
        refreshScreen();

        clockTimer = new Timer(1000, e -> updateLcd());
        clockTimer.start();
    }

    // ==================== API PÚBLICA PARA INTEGRAÇÃO ====================

    /** Chamado quando o mesário confirma a liberação da urna (recebe o título). */
    public void setVoterAuthorizedListener(Consumer<String> listener) {
        this.voterAuthorizedListener = listener;
    }

    /** Define o total de eleitores aptos (mostrado como votos/total no canto do LCD). */
    public void setTotalVoters(int total) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setTotalVoters(total));
            return;
        }
        this.totalVoters = Math.max(0, total);
        updateLcd();
    }

    /**
     * Define o título do presidente da mesa, exigido para encerrar a votação.
     * Se não for definido, qualquer título completo (12 dígitos) encerra a votação.
     */
    public void setPresidentId(String id) {
        this.presidentId = (id == null) ? null : id.replaceAll("\\D", "");
    }

    /** Troca o relógio do terminal (útil para testar o encerramento antes das 17h). */
    public void setClock(Clock clock) {
        if (clock == null) {
            return;
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setClock(clock));
            return;
        }
        this.clock = clock;
        updateLcd();
    }

    /** A urna deve chamar este método quando o voto do eleitor atual for registrado. */
    public void notifyVoteCompleted() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::notifyVoteCompleted);
            return;
        }
        if (step != Step.AUTHORIZED || currentVoter == null) {
            return;
        }
        votedIds.add(currentVoter);
        votesCount++;
        currentVoter = null;
        idInput.setLength(0);
        step = Step.ENTER_ID;
        setNotice("Voto registrado.");
        refreshScreen();
    }

    // ==================== JANELA ====================

    private void configurarJanela() {
        setTitle("Terminal do Mesário - Justiça Eleitoral");
        setSize(650, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(215, 215, 210));
    }

    private void inicializarComponentes() {
        // ==================== TOPO: LCD ====================

        lcd = new LcdScreen(LCD_COLS, LCD_ROWS);
        // Moldura (bezel) escura ao redor do LCD
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
        panelTeclado.setBorder(
                BorderFactory.createEmptyBorder(15, 20, 20, 20)
        );

        JLabel lblTopo = new JLabel("JUSTIÇA ELEITORAL", SwingConstants.CENTER);
        lblTopo.setForeground(Color.BLACK);
        lblTopo.setFont(new Font("Arial", Font.BOLD, 16));
        panelTeclado.add(lblTopo, BorderLayout.NORTH);

        // Teclado como no aparelho físico:
        //   1 2 3
        //   4 5 6
        //   7 8 9
        //   CORRIGE 0 CONFIRMA
        JPanel panelNum = new JPanel(new GridLayout(4, 3, 8, 8));
        panelNum.setOpaque(false);
        panelNum.setPreferredSize(new Dimension(270, 240));

        for (int n = 1; n <= 9; n++) {
            panelNum.add(criarTeclaNumerica(n));
        }

        btnCorrige = criarBotaoAcao("CORRIGE", new Color(230, 80, 40), Color.BLACK, 11);
        btnConfirma = criarBotaoAcao("CONFIRMA", new Color(0, 180, 80), Color.BLACK, 10);
        btnCorrige.addActionListener(e -> pressCorrige());
        btnConfirma.addActionListener(e -> pressConfirma());

        panelNum.add(btnCorrige);
        panelNum.add(criarTeclaNumerica(0));
        panelNum.add(btnConfirma);

        // Linha do dispositivo: bateria | teclado | indicadores (alinhados ao teclado)
        JPanel rowDispositivo = new JPanel(new BorderLayout(20, 0));
        rowDispositivo.setOpaque(false);
        rowDispositivo.add(centralizarVertical(new BatteryDisplay()), BorderLayout.WEST);
        rowDispositivo.add(panelNum, BorderLayout.CENTER);
        rowDispositivo.add(centralizarVertical(criarPainelIndicadores()), BorderLayout.EAST);

        // Centraliza a linha sem esticá-la
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

    /** Envolve o componente num painel que o mantém centralizado na vertical, sem esticá-lo. */
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

    // ==================== FACTORY METHODS ====================

    private JButton criarTeclaNumerica(final int num) {
        JButton btn = criarBotaoNumerico(String.valueOf(num));
        btn.addActionListener(e -> pressDigit(num));
        btnsNumericos[num] = btn;
        return btn;
    }

    private JButton criarBotaoNumerico(String texto) {
        JButton btn = new JButton(texto);

        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setBackground(new Color(20, 20, 20));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
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
        btn.setOpaque(true);
        btn.setBorderPainted(false);

        return btn;
    }

    // ==================== TECLAS ====================

    private void pressDigit(int digit) {
        clearNotice();

        switch (step) {
            case ENTER_ID:
            case CLOSE_ID:
                if (idInput.length() < ID_LENGTH) {
                    idInput.append(digit);
                }
                break;
            case MENU:
                if (digit == 1) {
                    step = Step.ENTER_ID;
                } else if (digit == 2) {
                    if (isBeforeClosingTime()) {
                        setNotice("Só após as 17h (Brasília).");
                    } else {
                        idInput.setLength(0);
                        step = Step.CLOSE_ID;
                    }
                } else {
                    setNotice("Opção inválida.");
                }
                break;
            default:
                return;
        }
        refreshScreen();
    }

    private void pressCorrige() {
        clearNotice();

        switch (step) {
            case ENTER_ID:
                if (idInput.length() == 0) {
                    // Nada digitado: CORRIGE abre "Outras opções"
                    step = Step.MENU;
                } else {
                    idInput.setLength(0);
                }
                break;
            case CONFIRM_ID:
                idInput.setLength(0);
                step = Step.ENTER_ID;
                break;
            case AUTHORIZED:
                currentVoter = null;
                idInput.setLength(0);
                step = Step.ENTER_ID;
                setNotice("Liberação cancelada.");
                break;
            case MENU:
                step = Step.ENTER_ID;
                break;
            case CLOSE_ID:
                if (idInput.length() == 0) {
                    step = Step.MENU;
                } else {
                    idInput.setLength(0);
                }
                break;
            default:
                return;
        }
        refreshScreen();
    }

    private void pressConfirma() {
        clearNotice();

        switch (step) {
            case ENTER_ID:
                if (idInput.length() < ID_LENGTH) {
                    setNotice("Título incompleto.");
                } else if (votedIds.contains(idInput.toString())) {
                    setNotice("Este eleitor já votou.");
                } else {
                    step = Step.CONFIRM_ID;
                }
                break;
            case CONFIRM_ID:
                currentVoter = idInput.toString();
                step = Step.AUTHORIZED;
                refreshScreen();
                if (voterAuthorizedListener != null) {
                    voterAuthorizedListener.accept(currentVoter);
                }
                return;
            case CLOSE_ID:
                if (idInput.length() < ID_LENGTH) {
                    setNotice("Título incompleto.");
                } else if (presidentId != null && !presidentId.equals(idInput.toString())) {
                    setNotice("Título não autorizado.");
                } else {
                    idInput.setLength(0);
                    step = Step.CLOSED;
                }
                break;
            default:
                return;
        }
        refreshScreen();
    }

    private boolean isBeforeClosingTime() {
        return LocalTime.now(clock).isBefore(CLOSING_TIME);
    }

    // ==================== ATUALIZAÇÃO DO VISOR ====================

    private void refreshScreen() {
        updateLcd();

        ledLiberado.setOn(step == Step.AUTHORIZED);

        // Teclado inativo após o encerramento
        boolean enabled = step != Step.CLOSED;
        for (JButton btn : btnsNumericos) {
            if (btn != null) {
                btn.setEnabled(enabled);
            }
        }
        btnCorrige.setEnabled(enabled);
        btnConfirma.setEnabled(enabled);
    }

    /**
     * Monta as duas linhas do LCD.
     * Linha 1: mensagem à esquerda + relógio à direita.
     * Linha 2: informação à esquerda + contador votos/total à direita (quando cabe).
     */
    private void updateLcd() {
        String time = LocalTime.now(clock).format(TIME_FORMAT);
        String counter = String.format(Locale.ROOT, "%04d/%04d", votesCount, totalVoters);

        String prompt = "";
        String line2 = "";
        String right2 = counter;

        switch (step) {
            case ENTER_ID:
                prompt = "Informe o Título de Eleitor";
                line2 = idMask();
                break;

            case CONFIRM_ID:
                prompt = "Título: " + formatId(idInput.toString());
                line2 = "CONFIRMA=Liberar  CORRIGE=Refazer";
                right2 = "";
                break;

            case AUTHORIZED:
                prompt = "Urna liberada: " + formatId(currentVoter == null ? "" : currentVoter);
                line2 = "Aguardando voto (CORRIGE cancela)";
                right2 = "";
                break;

            case MENU:
                prompt = "Outras opções";
                line2 = "1-Voltar  2-Encerrar votação";
                break;

            case CLOSE_ID:
                prompt = "Título do Presidente da mesa";
                line2 = idMask();
                break;

            case CLOSED:
                prompt = "VOTAÇÃO ENCERRADA";
                line2 = "Total de votos registrados: " + votesCount;
                right2 = "";
                break;
        }

        // Avisos substituem a mensagem da linha 1
        if (noticeText != null) {
            prompt = noticeText;
        }

        lcd.setLines(new String[]{
                compose(prompt, time),
                compose(line2, right2)
        });
    }

    // ==================== UTILITÁRIOS ====================

    private void setNotice(String text) {
        noticeText = text;
    }

    private void clearNotice() {
        noticeText = null;
    }

    /** Monta uma linha de LCD: texto à esquerda, texto à direita, preenchida até LCD_COLS. */
    private static String compose(String left, String right) {
        int max = LCD_COLS - right.length() - 1;
        String l = left.length() > max ? left.substring(0, Math.max(0, max)) : left;

        StringBuilder sb = new StringBuilder(l);
        while (sb.length() < LCD_COLS - right.length()) {
            sb.append(' ');
        }
        return sb.append(right).toString();
    }

    /** Título em digitação, com "_" nas posições que faltam: 01296650____. */
    private String idMask() {
        StringBuilder sb = new StringBuilder(idInput);
        while (sb.length() < ID_LENGTH) {
            sb.append('_');
        }
        return sb.toString();
    }

    /** Formata o título em grupos de 4 dígitos: 1234 5678 9012. */
    private static String formatId(String id) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < id.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                sb.append(' ');
            }
            sb.append(id.charAt(i));
        }
        return sb.toString();
    }

    // ==================== COMPONENTE: LCD DE CARACTERES ====================

    /** Visor LCD com grade fixa de caracteres, luz de fundo e células "apagadas". */
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

        /** Define o conteúdo do visor (linhas nulas ficam em branco). */
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
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Insets in = getInsets();
                int ax = in.left;
                int ay = in.top;
                int aw = getWidth() - in.left - in.right;
                int ah = getHeight() - in.top - in.bottom;
                if (aw <= 0 || ah <= 0) {
                    return;
                }

                // Luz de fundo do LCD
                g2.setPaint(new GradientPaint(0, ay, LCD_TOP, 0, ay + ah, LCD_BOTTOM));
                g2.fillRect(ax, ay, aw, ah);

                // Grade: a célula é mais alta que larga, como no display real
                float cellW = (aw - 2f * PAD_X) / cols;
                float cellH = Math.min((ah - 2f * PAD_Y) / rows, cellW * 2.1f);
                float gx = ax + PAD_X;
                float gy = ay + (ah - cellH * rows) / 2f;

                // Ajusta a fonte para caber na célula
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

                        // Célula apagada (efeito de grade do LCD)
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

    // ==================== COMPONENTE: LED ====================

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

    // ==================== COMPONENTE: MOSTRADOR DE BATERIA ====================

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

                // Terminal (ponta) da bateria
                g2.setColor(outline);
                g2.fillRoundRect(w / 2 - bw / 6, by - 6, bw / 3, 8, 3, 3);

                // Corpo
                g2.setColor(new Color(0x7ED9A0));
                g2.fillRoundRect(bx, by, bw, bh, 10, 10);
                g2.setStroke(new BasicStroke(3f));
                g2.setColor(outline);
                g2.drawRoundRect(bx, by, bw, bh, 10, 10);

                // Raio
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