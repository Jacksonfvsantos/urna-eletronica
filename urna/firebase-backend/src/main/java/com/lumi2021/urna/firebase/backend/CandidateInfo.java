package com.lumi2021.urna.firebase.backend;

/** Resposta publica para a tela da urna. Nunca contem informacoes do eleitor. */
public record CandidateInfo(
        String numero,
        String cargo,
        String pokemonId,
        String nome,
        String imagemUrl,
        String partidoId,
        String partido,
        String sigla
) {}
