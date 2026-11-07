package com.lumi2021.urna.view;

import com.lumi2021.urna.controller.VoterController;
import com.lumi2021.urna.platform.InputService;

import javax.swing.*;
import java.awt.*;

public class VoterView extends JFrame {

    private VoterController voterController;

    private JLabel lblCargo;
    private JPanel panelDigitos;
    private JLabel[] lblDigitos;
    private JLabel lblNome;
    private JLabel lblPartido;
    private JLabel lblMensagem;
    private JLabel lblFoto;
    private JPanel panelFoto;
    private JPanel panelInstrucoes;

    private JButton[] btnsNumericos = new JButton[10];
    private JButton btnBranco;
    private JButton btnCorrige;
    private JButton btnConfirma;

    public VoterView(InputService inputService, VoterController voterController) {
        super("Terminal da urna");
        
        //this.inputService = inputService;
        //inputService.install(this);

        this.voterController = voterController;

        configurarJanela();
        inicializarComponentes();
    }

    private void configurarJanela() {
        setTitle("Urna Eletrônica - Justiça Eleitoral");
        setSize(650, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(215, 215, 210));
    }

    private void inicializarComponentes() {
        // ==================== VISOR ====================

        VotingScreen screen = new VotingScreen();
        screen.setBorder(BorderFactory.createLineBorder(Color.BLACK, 8));

        // Container que preserva a proporção 800:500 do visor.
        JPanel screenContainer = new JPanel(null) {
            @Override
            public void doLayout() {
                double aspectRatio = 800.0 / 500.0;

                int availableWidth = getWidth();
                int availableHeight = getHeight();

                int width = Math.min(
                        availableWidth,
                        (int) (availableHeight * aspectRatio)
                );

                int height = (int) (width / aspectRatio);

                int x = (availableWidth - width) / 2;
                int y = (availableHeight - height) / 2;

                screen.setBounds(x, y, width, height);
            }
        };

        screenContainer.setBackground(new Color(0xD7D7D2));
        screenContainer.add(screen);

        // ==================== TECLADO ====================

        JPanel panelTeclado = new JPanel(new BorderLayout(0, 15));
        panelTeclado.setBackground(new Color(0xE8E9E5));
        panelTeclado.setBorder(
                BorderFactory.createEmptyBorder(15, 30, 20, 30)
        );

        JLabel lblTopo = new JLabel(
                "JUSTIÇA ELEITORAL",
                SwingConstants.CENTER
        );

        lblTopo.setForeground(Color.BLACK);
        lblTopo.setFont(new Font("Arial", Font.BOLD, 16));

        panelTeclado.add(lblTopo, BorderLayout.NORTH);

        // ==================== ÁREA DOS BOTÕES ====================

        JPanel panelCentroTeclado = new JPanel(new BorderLayout(15, 0));
        panelCentroTeclado.setOpaque(false);

        panelCentroTeclado.setBorder(
                BorderFactory.createEmptyBorder(10, 40, 10, 40)
        );

        // Teclado numérico
        JPanel panelNum = new JPanel(new GridLayout(4, 3, 8, 8));
        panelNum.setOpaque(false);

        String[] layoutTeclado = {
                "1", "2", "3",
                "4", "5", "6",
                "7", "8", "9",
                "", "0", ""
        };

        for (String txt : layoutTeclado) {
            if (txt.isEmpty()) {
                panelNum.add(new JLabel(""));
                continue;
            }

            int num = Integer.parseInt(txt);
            JButton btn = criarBotaoNumerico(txt);

            btnsNumericos[num] = btn;
            panelNum.add(btn);
        }

        panelCentroTeclado.add(panelNum, BorderLayout.CENTER);

        // Botões de ação
        JPanel panelAcoes = new JPanel(new GridLayout(3, 1, 0, 8));
        panelAcoes.setOpaque(false);
        panelAcoes.setPreferredSize(new Dimension(130, 0));

        btnBranco = criarBotaoAcao(
                "BRANCO", Color.WHITE, Color.BLACK, 11
        );

        btnCorrige = criarBotaoAcao(
                "CORRIGE", new Color(230, 80, 40), Color.BLACK, 11
        );

        btnConfirma = criarBotaoAcao(
                "CONFIRMA", new Color(0, 180, 80), Color.BLACK, 10
        );

        panelAcoes.add(btnBranco);
        panelAcoes.add(btnCorrige);
        panelAcoes.add(btnConfirma);

        panelCentroTeclado.add(panelAcoes, BorderLayout.EAST);

        panelTeclado.add(
                panelCentroTeclado,
                BorderLayout.CENTER
        );

        // ==================== LAYOUT PRINCIPAL ====================

        JPanel mainPane = new JPanel(new GridLayout(2, 1, 0, 0));

        mainPane.add(screenContainer);
        mainPane.add(panelTeclado);

        setContentPane(mainPane);
    }

    // ==================== FACTORY METHODS ====================

    private JButton criarBotaoNumerico(String texto) {
        JButton btn = new JButton(texto);

        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setBackground(new Color(20, 20, 20));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);

        return btn;
    }

    private JButton criarBotaoAcao(
            String texto,
            Color background,
            Color foreground,
            int fontSize
    ) {
        JButton btn = new JButton(texto);

        btn.setFont(new Font("Arial", Font.BOLD, fontSize));
        btn.setBackground(background);
        btn.setForeground(foreground);
        btn.setFocusPainted(false);

        return btn;
    }

    // ==================== MÉTODOS PÚBLICOS DE INTERFACE ====================
    // (Outro membro do grupo chamará estes métodos para atualizar a tela)

    public void setCargo(String cargo) {
        lblCargo.setText(cargo.toUpperCase());
    }

    public void prepararCaixasDigitos(int quantidade) {
        panelDigitos.removeAll();
        lblDigitos = new JLabel[quantidade];

        for (int i = 0; i < quantidade; i++) {
            JLabel box = new JLabel("", SwingConstants.CENTER);
            box.setPreferredSize(new Dimension(35, 40));
            box.setFont(new Font("Arial", Font.BOLD, 22));
            box.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            box.setOpaque(true);
            box.setBackground(Color.WHITE);
            lblDigitos[i] = box;
            panelDigitos.add(box);
        }
        panelDigitos.revalidate();
        panelDigitos.repaint();
    }

    public void atualizarDigito(int indice, String valor) {
        if (indice >= 0 && indice < lblDigitos.length) {
            lblDigitos[indice].setText(valor);
        }
    }

    public void limparDigitos() {
        for (JLabel lbl : lblDigitos) {
            lbl.setText("");
        }
    }

    public void setInfoCandidato(String nome, String partido, String fotoOuEmoji) {
        lblNome.setText("Nome: " + nome);
        lblPartido.setText("Partido: " + partido);
        if (fotoOuEmoji != null && !fotoOuEmoji.isEmpty()) {
            lblFoto.setText(fotoOuEmoji);
            panelFoto.setVisible(true);
        } else {
            panelFoto.setVisible(false);
        }
    }

    public void setMensagemAviso(String mensagem) {
        lblMensagem.setText(mensagem);
    }

    public void exibirTelaFim() {
        panelDigitos.setVisible(false);
        lblCargo.setText("");
        lblNome.setText("");
        lblPartido.setText("");
        lblMensagem.setText("F I M");
        lblMensagem.setFont(new Font("Arial", Font.BOLD, 60));
        lblMensagem.setBounds(110, 80, 300, 80);
        panelFoto.setVisible(false);
        panelInstrucoes.setVisible(false);
    }

}
