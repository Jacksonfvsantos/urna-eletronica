package com.lumi2021.urna.domain.models;

public record ElectoralPartyEntity(
        String id,
        String numero,
        String nome,
        String sigla,
        boolean ativo
) { }
