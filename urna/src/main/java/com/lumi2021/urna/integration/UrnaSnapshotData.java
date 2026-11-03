package com.lumi2021.urna.integration;

import java.util.List;

public record UrnaSnapshotData(
        String eleicaoId,
        List<CandidateSnapshotData> candidatos,
        List<PartySnapshotData> partidos,
        List<ElectorSnapshotData> eleitores
) { }
