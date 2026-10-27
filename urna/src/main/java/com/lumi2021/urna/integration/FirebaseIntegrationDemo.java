package com.lumi2021.urna.integration;

/** Comando de teste: ./gradlew demoFirebase */
public final class FirebaseIntegrationDemo {
    private FirebaseIntegrationDemo() { }
    public static void main(String[] args) throws Exception {
        String cargo = args.length > 0 ? args[0] : "VEREADOR";
        String numero = args.length > 1 ? args[1] : "12001";
        var candidato = new FirebaseCandidateClient().buscarCandidato(cargo, numero);
        if (candidato.isEmpty()) {
            System.out.println("Candidato nao encontrado: " + cargo + " / " + numero);
            return;
        }
        CandidateData c = candidato.get();
        System.out.printf("%s (%s) - %s / %s - Foto: %s%n",
                c.nome(), c.numero(), c.partido(), c.sigla(), c.imagemUrl());
    }
}
