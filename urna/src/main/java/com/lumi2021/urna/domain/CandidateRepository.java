package com.lumi2021.urna.domain;

import com.lumi2021.urna.domain.models.CandidateEntity;
import java.util.List;
import java.util.Optional;

public interface CandidateRepository {
    String getPartyByNumber(String number);
    String getCandidateByNumber(String number);
    Optional<CandidateEntity> buscarPorNumero(String cargo, String numero);
    List<CandidateEntity> listarPorCargo(String cargo);
}
