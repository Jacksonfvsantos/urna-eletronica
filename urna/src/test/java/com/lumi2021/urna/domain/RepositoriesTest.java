package com.lumi2021.urna.domain;

import com.lumi2021.urna.domain.models.CandidateEntity;
import com.lumi2021.urna.domain.models.ElectoralPartyEntity;
import com.lumi2021.urna.domain.models.ElectorEntity;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositoriesTest {
    private final ElectoralPartyEntity planta =
            new ElectoralPartyEntity("planta", "12", "Partido Planta", "PPL", true);
    private final ElectoralPartyEntity fogo =
            new ElectoralPartyEntity("fogo", "13", "Partido Fogo", "PFO", true);
    private final CandidateEntity bulbasaur =
            new CandidateEntity("12001", "VEREADOR", "001", "Bulbasaur", "url", "planta", "Partido Planta", "PPL", true);
    private final CandidateEntity charmander =
            new CandidateEntity("13", "PREFEITO", "004", "Charmander", "url", "fogo", "Partido Fogo", "PFO", true);

    @Test
    void consultaPartidoPeloPrefixoDoNumeroEleitoral() {
        CandidateRepository repo = new CandidatesRepository(List.of(bulbasaur, charmander), List.of(planta, fogo));
        assertEquals("Partido Planta", repo.getPartyByNumber("12"));
        assertEquals("Partido Planta", repo.getPartyByNumber("12001"));
        assertNull(repo.getPartyByNumber("99"));
    }

    @Test
    void consultaCandidatosPeloNumeroEDistingueCargo() {
        CandidateRepository repo = new CandidatesRepository(List.of(bulbasaur, charmander), List.of(planta, fogo));
        assertEquals("Bulbasaur", repo.getCandidateByNumber("12001"));
        assertEquals("Charmander", repo.getCandidateByNumber("13"));
        assertNull(repo.getCandidateByNumber("99999"));
        assertEquals(1, repo.listarPorCargo("VEREADOR").size());
        assertTrue(repo.buscarPorNumero("PREFEITO", "13").isPresent());
    }

    @Test
    void naoRetornaCandidatoInativo() {
        CandidateEntity inativo = new CandidateEntity("12001", "VEREADOR", "001",
                "Bulbasaur", "url", "planta", "Partido Planta", "PPL", false);
        CandidateRepository repo = new CandidatesRepository(List.of(inativo), List.of(planta));
        assertNull(repo.getCandidateByNumber("12001"));
        assertTrue(repo.listarPorCargo("VEREADOR").isEmpty());
    }

    @Test
    void validaEleitoresApenasPelaMemoria() {
        ElectorRepository repo = new InMemoryElectorRepository(List.of(
                new ElectorEntity("ELEITOR-1", true),
                new ElectorEntity("ELEITOR-2", false)));
        assertTrue(repo.isElectorIdValid("ELEITOR-1"));
        assertFalse(repo.isElectorIdValid("ELEITOR-2"));
        assertFalse(repo.isElectorIdValid("AUSENTE"));
        assertFalse(repo.isElectorIdValid(null));
    }

    @Test
    void rejeitaCandidatoDuplicadoNoMesmoCargo() {
        assertThrows(IllegalArgumentException.class, () ->
                new CandidatesRepository(List.of(bulbasaur, bulbasaur), List.of(planta)));
    }
}
