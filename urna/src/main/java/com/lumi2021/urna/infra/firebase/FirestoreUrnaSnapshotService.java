package com.lumi2021.urna.infra.firebase;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.lumi2021.urna.integration.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Baixa do Firestore o cadastro que a urna usa offline durante a votacao. */
public final class FirestoreUrnaSnapshotService {
    private final Firestore db;
    private final String eleicaoId;

    public FirestoreUrnaSnapshotService(Firestore db, String eleicaoId) {
        this.db = db;
        this.eleicaoId = eleicaoId;
    }

    public UrnaSnapshotData carregar() throws Exception {
        DocumentSnapshot eleicao = db.collection("eleicoes").document(eleicaoId)
                .get().get(30, TimeUnit.SECONDS);
        if (!eleicao.exists() || !"MUNICIPAL".equals(eleicao.getString("modalidade"))) {
            throw new IllegalStateException("Eleicao municipal nao encontrada");
        }

        var todosPartidos = db.collection("partidos")
                .get().get(30, TimeUnit.SECONDS).getDocuments();
        Map<String, PartySnapshotData> partidosPorId = new HashMap<>();
        for (QueryDocumentSnapshot doc : todosPartidos) {
            PartySnapshotData partido = new PartySnapshotData(doc.getId(),
                    exigir(doc.getString("numero"), "partido.numero"),
                    exigir(doc.getString("nome"), "partido.nome"),
                    exigir(doc.getString("sigla"), "partido.sigla"),
                    Boolean.TRUE.equals(doc.getBoolean("ativo")));
            partidosPorId.put(doc.getId(), partido);
        }

        var todosPokemons = db.collection("pokemons")
                .get().get(30, TimeUnit.SECONDS).getDocuments();
        Map<String, DocumentSnapshot> pokemonsPorId = new HashMap<>();
        for (QueryDocumentSnapshot doc : todosPokemons) {
            pokemonsPorId.put(doc.getId(), doc);
        }

        var todosCandidatos = db.collection("eleicoes").document(eleicaoId)
                .collection("candidatos").get().get(30, TimeUnit.SECONDS).getDocuments();
        List<CandidateSnapshotData> candidatos = new ArrayList<>();
        for (QueryDocumentSnapshot doc : todosCandidatos) {
            String pokemonId = exigir(doc.getString("pokemonId"), "candidato.pokemonId");
            String partidoId = exigir(doc.getString("partidoId"), "candidato.partidoId");
            DocumentSnapshot pokemon = pokemonsPorId.get(pokemonId);
            PartySnapshotData partido = partidosPorId.get(partidoId);
            if (pokemon == null || partido == null || !partidoId.equals(pokemon.getString("partidoId"))) {
                throw new IllegalStateException("Referencias inconsistentes em candidato " + doc.getId());
            }
            candidatos.add(new CandidateSnapshotData(
                    exigir(doc.getString("numero"), "candidato.numero"),
                    exigir(doc.getString("cargo"), "candidato.cargo"),
                    pokemonId,
                    exigir(pokemon.getString("nome"), "pokemon.nome"),
                    pokemon.getString("imagemUrl"),
                    partidoId,
                    partido.nome(),
                    partido.sigla(),
                    Boolean.TRUE.equals(doc.getBoolean("ativo"))));
        }

        var todosEleitores = db.collection("eleicoes").document(eleicaoId)
                .collection("eleitores").get().get(30, TimeUnit.SECONDS).getDocuments();
        List<ElectorSnapshotData> eleitores = new ArrayList<>();
        for (QueryDocumentSnapshot doc : todosEleitores) {
            eleitores.add(new ElectorSnapshotData(doc.getId(),
                    Boolean.TRUE.equals(doc.getBoolean("ativo"))));
        }
        if (candidatos.isEmpty() || partidosPorId.isEmpty() || eleitores.isEmpty()) {
            throw new IllegalStateException("Cadastro de candidatos, partidos ou eleitores nao inicializado");
        }

        return new UrnaSnapshotData(eleicaoId, List.copyOf(candidatos),
                List.copyOf(partidosPorId.values()), List.copyOf(eleitores));
    }

    private static String exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Campo obrigatorio ausente: " + campo);
        }
        return valor;
    }
}
