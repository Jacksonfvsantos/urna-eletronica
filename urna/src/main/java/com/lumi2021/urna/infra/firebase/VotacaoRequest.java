package com.lumi2021.urna.infra.firebase;

import java.util.List;

public record VotacaoRequest(String sessaoId, List<VotoRequest> votos) { }
