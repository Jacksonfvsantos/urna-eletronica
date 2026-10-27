package com.lumi2021.urna.firebase.backend;

import java.util.List;

/** Uma sessao deve conter exatamente um voto para cada cargo municipal. */
public record VotacaoRequest(String sessaoId, List<VotoRequest> votos) { }
