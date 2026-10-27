package com.lumi2021.urna.integration;

/** Dados retornados pela API, prontos para exibir na tela Swing. */
public record CandidateData(
        String numero,
        String cargo,
        String pokemonId,
        String nome,
        String imagemUrl,
        String partidoId,
        String partido,
        String sigla
) { }
