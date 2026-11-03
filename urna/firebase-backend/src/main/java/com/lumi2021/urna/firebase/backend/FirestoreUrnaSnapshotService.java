package com.lumi2021.urna.firebase.backend;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class FirestoreUrnaSnapshotService {
    private final Firestore db;
    private final String eleicaoId;

    public FirestoreUrnaSnapshotService(Firestore db, String eleicaoId) {
        this.db = db;
        this.eleicaoId = eleicaoId;
    }

    public record CandidateSnapshot(
            String numero, String cargo, String pokemonId, String nome, String imagemUrl,
            String partidoId, String partido, String sigla, boolean ativo) { }

    public record PartySnapshot(String id, String numero, String nome, String sigla, boolean ativo) { }
    public record ElectorSnapshot(String id, boolean ativo) { }
    public record UrnaSnapshot(String eleicaoId, List<CandidateSnapshot> candidatos,
                               List<PartySnapshot> partidos, List<ElectorSnapshot> eleitores) { }

    public UrnaSnapshot carregar() throws Exception {
        DocumentSnapshot eleicao = db.collection("eleicoes").document(eleicaoId)
                .get().get(30, TimeUnit.SECONDS);
        if (!eleicao.exists() || !"MUNICIPAL".equals(eleicao.getString("modalidade"))) {
            throw new IllegalStateException("Eleicao municipal nao encontrada");
        }

        var todosPartidos = db.collection("partidos")
                .get().get(30, TimeUnit.SECONDS).getDocuments();
        Map<String, PartySnapshot> partidosPorId = new HashMap<>();
        for (QueryDocumentSnapshot doc : todosPartidos) {
            PartySnapshot partido = new PartySnapshot(doc.getId(),
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
        List<CandidateSnapshot> candidatos = new ArrayList<>();
        for (QueryDocumentSnapshot doc : todosCandidatos) {
            String pokemonId = exigir(doc.getString("pokemonId"), "candidato.pokemonId");
            String partidoId = exigir(doc.getString("partidoId"), "candidato.partidoId");
            DocumentSnapshot pokemon = pokemonsPorId.get(pokemonId);
            PartySnapshot partido = partidosPorId.get(partidoId);
            if (pokemon == null || partido == null || !partidoId.equals(pokemon.getString("partidoId"))) {
                throw new IllegalStateException("Referencias inconsistentes em candidato " + doc.getId());
            }
            candidatos.add(new CandidateSnapshot(
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
        List<ElectorSnapshot> eleitores = new ArrayList<>();
        for (QueryDocumentSnapshot doc : todosEleitores) {
            eleitores.add(new ElectorSnapshot(doc.getId(),
                    Boolean.TRUE.equals(doc.getBoolean("ativo"))));
        }
        if (candidatos.isEmpty() || partidosPorId.isEmpty() || eleitores.isEmpty()) {
            throw new IllegalStateException("Cadastro de candidatos, partidos ou eleitores nao inicializado");
        }

        return new UrnaSnapshot(eleicaoId, List.copyOf(candidatos),
                List.copyOf(partidosPorId.values()), List.copyOf(eleitores));
    }

    private static String exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Campo obrigatorio ausente: " + campo);
        }
        return valor;
    }
}
