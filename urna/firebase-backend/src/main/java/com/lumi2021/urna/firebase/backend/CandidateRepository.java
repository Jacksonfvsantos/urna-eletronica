package com.lumi2021.urna.firebase.backend;

import java.util.Optional;
import java.util.List;

/** Interface prevista pelo diagrama de classes do projeto da equipe. */
public interface CandidateRepository {
    Optional<CandidateInfo> buscarPorNumero(String cargo, String numero) throws Exception;
    List<CandidateInfo> listarPorCargo(String cargo) throws Exception;
}
