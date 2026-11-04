package com.lumi2021.urna.domain;

import com.lumi2021.urna.domain.models.ElectorEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class InMemoryElectorRepository implements ElectorRepository {
    private final Map<String, ElectorEntity> eleitores;

    public InMemoryElectorRepository(List<ElectorEntity> eleitores) {
        Objects.requireNonNull(eleitores, "eleitores");
        Map<String, ElectorEntity> mapa = new HashMap<>();
        for (ElectorEntity eleitor : eleitores) {
            if (eleitor == null || eleitor.id() == null || eleitor.id().isBlank()) {
                throw new IllegalArgumentException("Eleitor sem identificador");
            }
            if (mapa.putIfAbsent(eleitor.id(), eleitor) != null) {
                throw new IllegalArgumentException("Identificador de eleitor duplicado");
            }
        }
        this.eleitores = Map.copyOf(mapa);
    }

    @Override
    public boolean isElectorIdValid(String electorId) {
        if (electorId == null || electorId.isBlank()) return false;
        ElectorEntity eleitor = eleitores.get(electorId);
        return eleitor != null && eleitor.ativo();
    }

    public int totalCarregados() {
        return eleitores.size();
    }
}
