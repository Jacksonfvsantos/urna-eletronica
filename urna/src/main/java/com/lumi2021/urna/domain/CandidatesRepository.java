package com.lumi2021.urna.domain;

import com.lumi2021.urna.domain.models.CandidateEntity;
import com.lumi2021.urna.domain.models.ElectoralPartyEntity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class CandidatesRepository implements CandidateRepository {
    private final Map<String, CandidateEntity> candidatos;
    private final Map<String, ElectoralPartyEntity> partidos;
    private final List<CandidateEntity> todos;

    public CandidatesRepository(List<CandidateEntity> candidatos, List<ElectoralPartyEntity> partidos) {
        Objects.requireNonNull(candidatos, "candidatos");
        Objects.requireNonNull(partidos, "partidos");
        Map<String, ElectoralPartyEntity> mapaPartidos = new HashMap<>();
        for (ElectoralPartyEntity partido : partidos) {
            if (partido == null || partido.numero() == null || !partido.numero().matches("[0-9]{2}")) {
                throw new IllegalArgumentException("Partido com numero invalido");
            }
            if (mapaPartidos.putIfAbsent(partido.numero(), partido) != null) {
                throw new IllegalArgumentException("Numero de partido duplicado: " + partido.numero());
            }
        }
        Map<String, CandidateEntity> mapaCandidatos = new HashMap<>();
        for (CandidateEntity candidato : candidatos) {
            if (candidato == null || !cargoEValido(candidato.cargo(), candidato.numero())) {
                throw new IllegalArgumentException("Candidato com cargo ou numero invalido");
            }
            String chave = chave(candidato.cargo(), candidato.numero());
            if (mapaCandidatos.putIfAbsent(chave, candidato) != null) {
                throw new IllegalArgumentException("Candidato duplicado para " + chave);
            }
            ElectoralPartyEntity partido = mapaPartidos.get(candidato.numero().substring(0, 2));
            if (partido == null || !partido.id().equals(candidato.partidoId())) {
                throw new IllegalArgumentException("Partido inconsistente para candidato " + chave);
            }
        }
        this.partidos = Map.copyOf(mapaPartidos);
        this.candidatos = Map.copyOf(mapaCandidatos);
        this.todos = List.copyOf(candidatos);
    }

    @Override
    public String getPartyByNumber(String number) {
        if (number == null || !number.matches("[0-9]{2,5}")) return null;
        ElectoralPartyEntity partido = partidos.get(number.substring(0, 2));
        return partido != null && partido.ativo() ? partido.nome() : null;
    }

    @Override
    public String getCandidateByNumber(String number) {
        if (number == null) return null;
        String cargo = switch (number.length()) {
            case 2 -> "PREFEITO";
            case 5 -> "VEREADOR";
            default -> null;
        };
        return cargo == null ? null : buscarPorNumero(cargo, number)
                .map(CandidateEntity::nome).orElse(null);
    }

    @Override
    public Optional<CandidateEntity> buscarPorNumero(String cargo, String numero) {
        if (!cargoEValido(cargo, numero)) return Optional.empty();
        CandidateEntity candidato = candidatos.get(chave(cargo, numero));
        if (candidato == null || !candidato.ativo()) return Optional.empty();
        ElectoralPartyEntity partido = partidos.get(numero.substring(0, 2));
        return partido != null && partido.ativo() ? Optional.of(candidato) : Optional.empty();
    }

    @Override
    public List<CandidateEntity> listarPorCargo(String cargo) {
        if (!"VEREADOR".equals(cargo) && !"PREFEITO".equals(cargo)) return List.of();
        List<CandidateEntity> resultado = new ArrayList<>();
        for (CandidateEntity candidato : todos) {
            if (cargo.equals(candidato.cargo()) && candidato.ativo() &&
                    partidos.get(candidato.numero().substring(0, 2)).ativo()) {
                resultado.add(candidato);
            }
        }
        resultado.sort(Comparator.comparing(CandidateEntity::numero));
        return List.copyOf(resultado);
    }

    public int totalCarregados() {
        return todos.size();
    }

    private static boolean cargoEValido(String cargo, String numero) {
        if (numero == null) return false;
        return ("PREFEITO".equals(cargo) && numero.matches("[0-9]{2}")) ||
                ("VEREADOR".equals(cargo) && numero.matches("[0-9]{5}"));
    }

    private static String chave(String cargo, String numero) {
        return cargo + ":" + numero;
    }
}
