package com.lumi2021.urna.firebase.backend;

/** Voto para um cargo. Nao guardar identificacao do eleitor. */
public record VotoRequest(String cargo, String tipo, String numero) { }
