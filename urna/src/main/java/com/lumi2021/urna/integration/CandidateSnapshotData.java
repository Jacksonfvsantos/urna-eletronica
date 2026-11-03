package com.lumi2021.urna.integration;

public record CandidateSnapshotData(
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
