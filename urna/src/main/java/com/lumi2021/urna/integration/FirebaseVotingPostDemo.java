
package com.lumi2021.urna.integration;

import java.util.List;

public class FirebaseVotingPostDemo {

    public static void main(String[] args) throws Exception {

        String sessaoId = System.getenv("URN_TEST_SESSION_ID");
        String token = System.getenv("URN_VOTING_TOKEN");

        if (sessaoId == null || sessaoId.isBlank()) {
            throw new IllegalStateException(
                    "URN_TEST_SESSION_ID nao configurada"
            );
        }

        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "URN_VOTING_TOKEN nao configurado"
            );
        }

        FirebaseVotingClient cliente =
                new FirebaseVotingClient(
                        "http://127.0.0.1:7070"
                );

        VotoData vereador = new VotoData(
                "VEREADOR",
                "VALIDO",
                "12001"
        );

        VotoData prefeito = new VotoData(
                "PREFEITO",
                "BRANCO",
                null
        );

        VotacaoData votacao = new VotacaoData(
                sessaoId,
                List.of(vereador, prefeito)
        );

        System.out.println("Enviando votacao...");

        ConfirmacaoVotacaoData resposta =
                cliente.registrarVotacao(votacao, token);

        System.out.println("Sessao: " + resposta.sessaoId());
        System.out.println("Status: " + resposta.status());
    }
}
