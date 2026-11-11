package com.lumi2021.urna.view.models;

public record MesarioScreenModel(
        String lcdLine1,
        String lcdLine2,
        boolean ledLiberadoOn,
        boolean ledAguardeOn,
        boolean tecladoAtivo,
        int votesCount,
        int totalVoters
) {}
