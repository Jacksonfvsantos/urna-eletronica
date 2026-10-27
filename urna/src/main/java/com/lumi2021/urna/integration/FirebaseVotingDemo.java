package com.lumi2021.urna.integration;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class FirebaseVotingDemo {

    public static void main(String[] args) throws Exception {

        String enderecoApi = "http://127.0.0.1:7070";

        FirebaseVotingClient clienteVotacao =
                new FirebaseVotingClient(enderecoApi);

        System.out.println("Verificando a API...");
        System.out.println(clienteVotacao.verificarApi());

        FirebaseCandidateClient clienteCandidatos =
                new FirebaseCandidateClient(enderecoApi);

        Optional<CandidateData> resultado =
                clienteCandidatos.buscarCandidato(
                        "VEREADOR",
                        "12001"
                );

        if (resultado.isPresent()) {

            CandidateData candidato = resultado.get();

            System.out.println("\nCandidato encontrado!");
            System.out.println("Nome: " + candidato.nome());
            System.out.println("Numero: " + candidato.numero());
            System.out.println("Cargo: " + candidato.cargo());
            System.out.println("Partido: " + candidato.partido());
            System.out.println("Sigla: " + candidato.sigla());
            System.out.println("Imagem: " + candidato.imagemUrl());

        } else {
            System.out.println("Candidato nao encontrado.");
        }

        VotoData votoVereador = new VotoData(
                "VEREADOR",
                "VALIDO",
                "12001"
        );

        VotoData votoPrefeito = new VotoData(
                "PREFEITO",
                "BRANCO",
                null
        );

        VotacaoData votacao = new VotacaoData(
                "SESSAO_APENAS_PARA_TESTE_DE_JSON",
                List.of(votoVereador, votoPrefeito)
        );

        ObjectMapper json = new ObjectMapper();

        String corpoJson = json.writeValueAsString(votacao);

        System.out.println("\nJSON preparado:");
        System.out.println(corpoJson);

    }

}
