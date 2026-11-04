package com.lumi2021.urna.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class FirebaseSnapshotClient {
    private final String baseUrl;
    private final HttpClient http;
    private final ObjectMapper json = new ObjectMapper();

    public FirebaseSnapshotClient(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Endereco da API obrigatorio");
        }
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    public UrnaSnapshotData carregar(String token) throws IOException, InterruptedException {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("URN_SNAPSHOT_TOKEN nao configurado");
        }
        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/admin/urna/dados-iniciais"))
                .timeout(Duration.ofSeconds(60))
                .header("X-Snapshot-Token", token)
                .GET()
                .build();
        HttpResponse<String> resposta = http.send(
                requisicao, HttpResponse.BodyHandlers.ofString());
        if (resposta.statusCode() != 200) {
            throw new IOException("Falha ao carregar dados iniciais: HTTP " + resposta.statusCode());
        }
        UrnaSnapshotData dados = json.readValue(resposta.body(), UrnaSnapshotData.class);
        if (dados == null || dados.eleicaoId() == null || dados.candidatos() == null ||
                dados.partidos() == null || dados.eleitores() == null) {
            throw new IOException("Resposta de inicializacao incompleta");
        }
        return dados;
    }
}
