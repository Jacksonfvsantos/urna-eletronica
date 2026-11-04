package com.lumi2021.urna.domain.models;

public record CandidateEntity(
        String numero,
        String cargo,
        String pokemonId,
        String nome,
        String imagemUrl,
        String partidoId,
        String partido,
        String sigla,
        boolean ativo
) { }
