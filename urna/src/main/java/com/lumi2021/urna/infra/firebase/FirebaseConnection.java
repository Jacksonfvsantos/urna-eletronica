package com.lumi2021.urna.infra.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.IOException;

/**
 * Conexao unica da urna com o Firestore. Substitui a antiga API HTTP local
 * (firebase-backend): a urna baixa e grava os dados diretamente.
 *
 * Variaveis de ambiente:
 *  - GOOGLE_APPLICATION_CREDENTIALS: JSON da conta de servico (obrigatoria)
 *  - FIREBASE_PROJECT_ID: id do projeto (padrao: av3-poo---urna)
 *  - ELEICAO_ID: documento em /eleicoes (padrao: eleicao-001)
 */
public final class FirebaseConnection implements AutoCloseable {
    public static final String PROJETO_PADRAO = "av3-poo---urna";
    public static final String ELEICAO_PADRAO = "eleicao-001";

    private final FirebaseApp app;
    private final Firestore db;
    private final String eleicaoId;

    private FirebaseConnection(FirebaseApp app, Firestore db, String eleicaoId) {
        this.app = app;
        this.db = db;
        this.eleicaoId = eleicaoId;
    }

    public static FirebaseConnection abrirDoAmbiente() throws IOException {
        String projeto = valor("FIREBASE_PROJECT_ID", PROJETO_PADRAO);
        String eleicaoId = valor("ELEICAO_ID", ELEICAO_PADRAO);
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setProjectId(projeto)
                .build();
        FirebaseApp app = FirebaseApp.initializeApp(options, "urna-" + System.nanoTime());
        return new FirebaseConnection(app, FirestoreClient.getFirestore(app), eleicaoId);
    }

    public Firestore firestore() { return db; }
    public String eleicaoId() { return eleicaoId; }

    @Override
    public void close() {
        try { db.close(); } catch (Exception ignored) { }
        app.delete();
    }

    private static String valor(String nome, String padrao) {
        String v = System.getenv(nome);
        return v == null || v.isBlank() ? padrao : v;
    }
}
