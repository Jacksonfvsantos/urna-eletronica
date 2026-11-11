package com.lumi2021.urna.application;

import com.lumi2021.urna.infra.firebase.VotoRequest;

import java.util.List;

public interface VotingGateway {
    String abrirSessao() throws Exception;
    void abrirEleicao(String eleicaoId) throws Exception;
    void registrar(String sessaoId, List<VotoRequest> votos) throws Exception;
}
