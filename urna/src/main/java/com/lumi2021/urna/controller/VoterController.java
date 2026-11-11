package com.lumi2021.urna.controller;

import com.lumi2021.urna.application.VotingServer;
import com.lumi2021.urna.application.VotingServer.RejectedException;
import com.lumi2021.urna.application.VotingServer.Session;
import com.lumi2021.urna.domain.models.CandidateEntity;
import com.lumi2021.urna.infra.firebase.VotoRequest;
import com.lumi2021.urna.view.models.VotingScreenModel;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Terminal do eleitor ("VotingTerminal" do diagrama): tela de vereador, tela de prefeito,
 * confirmação e tela FIM. Todos os métodos públicos devem ser chamados na thread da interface.
 */
public class VoterController {

    public enum State { IDLE, VEREADOR, PREFEITO, SUBMITTING, FINISHED }

    /** Notificações para o terminal do mesário (resposta "Notifica conclusão"). */
    public interface Listener {
        void onVoteCompleted();
        void onVoteFailed(String message);
    }

    private static final int FINISH_SCREEN_MILLIS = 3000;
    private static final int VEREADOR_DIGITS = 5;
    private static final int PREFEITO_DIGITS = 2;

    private final VotingServer server;
    private final Executor background;
    private final Executor ui;

    private Consumer<VotingScreenModel> display = model -> { };
    private Listener listener;

    private State state = State.IDLE;
    private Session session;
    private VotoRequest vereadorVote;

    // Entrada em andamento no cargo atual
    private final StringBuilder digits = new StringBuilder();
    private boolean blank;
    private String partyName;
    private CandidateEntity candidate;

    private Timer finishTimer;

    public VoterController(VotingServer server) {
        this(server, Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "voting-server-io");
            t.setDaemon(true);
            return t;
        }), SwingUtilities::invokeLater);
    }

    /** Construtor com executores injetados (usado nos testes). */
    public VoterController(VotingServer server, Executor background, Executor ui) {
        this.server = server;
        this.background = background;
        this.ui = ui;
    }

    public void setDisplay(Consumer<VotingScreenModel> display) {
        this.display = display;
        render();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public State state() {
        return state;
    }

    // ================================================================ liberação

    /** "Liberação": o servidor libera o terminal para um eleitor. Mostra a tela de vereador. */
    public void release(Session newSession) {
        stopFinishTimer();
        session = newSession;
        vereadorVote = null;
        state = State.VEREADOR;
        resetEntry();
        render();
    }

    /**
     * Cancela uma liberação ainda não confirmada pelo eleitor.
     *
     * @return false se não é mais possível cancelar (voto já enviado ao servidor)
     */
    public boolean cancelRelease() {
        if (state == State.SUBMITTING) {
            return false;
        }
        if (state == State.VEREADOR || state == State.PREFEITO) {
            server.cancelSession(session);
            session = null;
            vereadorVote = null;
            state = State.IDLE;
            resetEntry();
            render();
        }
        return true;
    }

    // ================================================================ teclas

    public void digit(int d) {
        if (!isEntryState() || blank || d < 0 || d > 9) return;
        if (digits.length() >= digitsNeeded()) return;
        digits.append(d);
        lookup();
        render();
    }

    public void blank() {
        if (!isEntryState() || digits.length() > 0) return;
        blank = true;
        render();
    }

    public void correct() {
        if (!isEntryState()) return;
        resetEntry();
        render();
    }

    public void confirm() {
        if (!isEntryState()) return;

        String cargo = currentCargo();
        VotoRequest vote;
        if (blank) {
            vote = new VotoRequest(cargo, "BRANCO", null);
        } else if (digits.length() == digitsNeeded()) {
            vote = candidate != null
                    ? new VotoRequest(cargo, "VALIDO", digits.toString())
                    : new VotoRequest(cargo, "NULO", null);
        } else {
            return; // número incompleto: CONFIRMA não faz nada
        }

        if (state == State.VEREADOR) {
            vereadorVote = vote;
            state = State.PREFEITO;
            resetEntry();
            render();
        } else {
            submit(vote);
        }
    }

    // ================================================================ envio

    private void submit(VotoRequest prefeitoVote) {
        final Session current = session;
        final VotoRequest vereador = vereadorVote;
        state = State.SUBMITTING;
        render();

        background.execute(() -> {
            try {
                server.confirm(current, vereador, prefeitoVote);
                ui.execute(this::onConfirmed);
            } catch (RejectedException e) {
                ui.execute(() -> onFailed(e.getMessage()));
            } catch (RuntimeException e) {
                ui.execute(() -> onFailed("Erro inesperado: " + e.getMessage()));
            }
        });
    }

    /** "Notifica conclusão" + "Mostra FIM". */
    private void onConfirmed() {
        state = State.FINISHED;
        session = null;
        vereadorVote = null;
        render();

        finishTimer = new Timer(FINISH_SCREEN_MILLIS, e -> {
            if (state == State.FINISHED) {
                state = State.IDLE;
                render();
            }
        });
        finishTimer.setRepeats(false);
        finishTimer.start();

        if (listener != null) listener.onVoteCompleted();
    }

    private void onFailed(String message) {
        state = State.IDLE;
        session = null;
        vereadorVote = null;
        resetEntry();
        display.accept(new VotingScreenModel.Waiting(message));
        if (listener != null) listener.onVoteFailed(message);
    }

    // ================================================================ consultas ao servidor

    /** Partido após 2 dígitos (vereador) e candidato quando o número está completo. */
    private void lookup() {
        partyName = null;
        candidate = null;
        int n = digits.length();
        if (state == State.VEREADOR) {
            if (n >= 2) {
                partyName = server.requestParty(digits.substring(0, 2)).orElse(null);
            }
            if (n == VEREADOR_DIGITS) {
                candidate = server.requestCandidate(VotingServer.VEREADOR, digits.toString()).orElse(null);
            }
        } else if (n == PREFEITO_DIGITS) {
            partyName = server.requestParty(digits.toString()).orElse(null);
            candidate = server.requestCandidate(VotingServer.PREFEITO, digits.toString()).orElse(null);
        }
    }

    // ================================================================ auxiliares

    private boolean isEntryState() {
        return state == State.VEREADOR || state == State.PREFEITO;
    }

    private String currentCargo() {
        return state == State.VEREADOR ? VotingServer.VEREADOR : VotingServer.PREFEITO;
    }

    private int digitsNeeded() {
        return state == State.VEREADOR ? VEREADOR_DIGITS : PREFEITO_DIGITS;
    }

    private void resetEntry() {
        digits.setLength(0);
        blank = false;
        partyName = null;
        candidate = null;
    }

    private void stopFinishTimer() {
        if (finishTimer != null) {
            finishTimer.stop();
            finishTimer = null;
        }
    }

    private void render() {
        display.accept(buildModel());
    }

    private VotingScreenModel buildModel() {
        return switch (state) {
            case IDLE -> new VotingScreenModel.Waiting(null);
            case SUBMITTING -> new VotingScreenModel.Submitting();
            case FINISHED -> new VotingScreenModel.Finished();

            case VEREADOR, PREFEITO -> {
                boolean complete = digits.length() == digitsNeeded();
                String headline = null;
                String detail = null;

                if (blank) {
                    headline = "VOTO EM BRANCO";
                }
                else if (complete && candidate == null) {
                    headline = "VOTO NULO";
                    detail = partyName != null ? "CANDIDATO INEXISTENTE" : "NÚMERO ERRADO";
                }

                yield new VotingScreenModel.Voting(
                        state == State.VEREADOR ? "Vereador" : "Prefeito",
                        digitsNeeded(),
                        digits.toString(),
                        candidate != null ? candidate.nome() : null,
                        partyName,
                        candidate != null ? candidate.imagemUrl() : null,
                        headline,
                        detail
                );
            }
        };
    }
}
