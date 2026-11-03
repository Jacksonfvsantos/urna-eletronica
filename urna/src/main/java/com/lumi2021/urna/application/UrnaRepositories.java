package com.lumi2021.urna.application;

import com.lumi2021.urna.domain.CandidateRepository;
import com.lumi2021.urna.domain.CandidatesRepository;
import com.lumi2021.urna.domain.ElectorRepository;
import com.lumi2021.urna.domain.InMemoryElectorRepository;
import com.lumi2021.urna.domain.models.CandidateEntity;
import com.lumi2021.urna.domain.models.ElectoralPartyEntity;
import com.lumi2021.urna.domain.models.ElectorEntity;
import com.lumi2021.urna.integration.FirebaseSnapshotClient;
import com.lumi2021.urna.integration.UrnaSnapshotData;
import java.io.IOException;
import java.util.List;

public final class UrnaRepositories {
    private final String eleicaoId;
    private final CandidatesRepository candidatos;
    private final InMemoryElectorRepository eleitores;

    private UrnaRepositories(String eleicaoId, CandidatesRepository candidatos,
                             InMemoryElectorRepository eleitores) {
        this.eleicaoId = eleicaoId;
        this.candidatos = candidatos;
        this.eleitores = eleitores;
    }

    public static UrnaRepositories carregarNaInicializacao()
            throws IOException, InterruptedException {
        String endereco = System.getenv().getOrDefault(
                "URNA_API_URL", "http://127.0.0.1:7070");
        String token = System.getenv("URN_SNAPSHOT_TOKEN");
        UrnaSnapshotData dados = new FirebaseSnapshotClient(endereco).carregar(token);
        String esperado = System.getenv("ELEICAO_ID");
        if (esperado != null && !esperado.isBlank() && !esperado.equals(dados.eleicaoId())) {
            throw new IOException("A API retornou uma eleicao diferente da configurada");
        }
        return aPartirDoSnapshot(dados);
    }

    public static UrnaRepositories aPartirDoSnapshot(UrnaSnapshotData dados) {
        if (dados == null || dados.eleicaoId() == null || dados.eleicaoId().isBlank() ||
                dados.candidatos() == null || dados.partidos() == null || dados.eleitores() == null) {
            throw new IllegalArgumentException("Dados de inicializacao incompletos");
        }
        List<CandidateEntity> candidatos = dados.candidatos().stream().map(c -> new CandidateEntity(
                c.numero(), c.cargo(), c.pokemonId(), c.nome(), c.imagemUrl(),
                c.partidoId(), c.partido(), c.sigla(), c.ativo())).toList();
        List<ElectoralPartyEntity> partidos = dados.partidos().stream().map(p ->
                new ElectoralPartyEntity(p.id(), p.numero(), p.nome(), p.sigla(), p.ativo())).toList();
        List<ElectorEntity> eleitores = dados.eleitores().stream().map(e ->
                new ElectorEntity(e.id(), e.ativo())).toList();
        if (candidatos.isEmpty() || partidos.isEmpty() || eleitores.isEmpty()) {
            throw new IllegalStateException("Cadastro de candidatos, partidos ou eleitores vazio");
        }
        return new UrnaRepositories(dados.eleicaoId(),
                new CandidatesRepository(candidatos, partidos),
                new InMemoryElectorRepository(eleitores));
    }

    public CandidateRepository candidatos() { return candidatos; }
    public ElectorRepository eleitores() { return eleitores; }
    public String eleicaoId() { return eleicaoId; }
    public int totalCandidatos() { return candidatos.totalCarregados(); }
    public int totalEleitores() { return eleitores.totalCarregados(); }
}
