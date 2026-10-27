package com.lumi2021.urna.application;

import com.lumi2021.urna.domain.models.VoteEntry;
import com.lumi2021.urna.domain.models.bulletinDocument.BulletinDocument;
import com.lumi2021.urna.domain.models.bulletinDocument.BulletinElement;
import com.lumi2021.urna.domain.models.bulletinDocument.BulletinLine;
import com.lumi2021.urna.domain.models.bulletinDocument.BulletinTextMode;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BallotBox {

    // Atributos
    private List<VoteEntry> voteEntries;
    private String municipality; // Municipio em que a Urna será utilizada.
    private int municipalityCode; // Código do municipio no qual a urna será usada.
    private LocalDateTime openingDateTime; // Data e hora do inicio da votação.
    private LocalDateTime closingDataTime; // Data e hora do encerramento da votação.
    private int electoralZone; // Zona eleitoral em que a urna será usada.
    private int section; // Seção eleitoral onde a urna será usada.
    private int eligibleVoters; // Quantidade total de votos elegiveis.

    // Inicializa todos os atributos quando a urna for ativada
    public void initialize(
        String municipality,
        int municipalityCode,
        LocalDateTime openingDateTime,
        LocalDateTime closingDataTime,
        int electoralZon,
        int section,
        int eligibleVoters
    ) {

    }

    public String getMunicipality(){
        return municipality;
    }

    public int getMunicipalityCode(){
        return municipalityCode;
    }

    public LocalDateTime getOpeningDateTime() {
        return openingDateTime;
    }

    public int getElectoralZone(){
        return electoralZone;
    }

    public int getSection(){
        return section;
    }

    public int getEligibleVoters(){
        return eligibleVoters;
    }

    public BulletinDocument emitBulletin(){
        var builder = new BulletinBuilder();

        builder.center("1a. VIA", BulletinTextMode.Bold);
        builder.left("");
        builder.left("");

        builder.center("Justiça Eleitoral", BulletinTextMode.Bold);
        builder.center("Tribunal Regional Eleitoral [BA]");
        builder.left("");

        builder.center("Boletin de Urna");
        builder.left("");

        builder.center("Eleições gerais 2026");
        builder.center("1° Turno");
        builder.center("(25/10/2026)");
        builder.left("");

        builder.fillLine('=');
        builder.left("");

        builder.centerFill("PRESIDENTE", '-');

        builder.row(new int[] { 25, 1, 8, 1, 5 },
            "Nome do candidato", "",
            builder.padLeft("Num cand", 8), "",
            builder.padLeft("Votos", 5));

        builder.row(new int[] { 2, 30, 2, 6 }, "", "LULA", "13", builder.padLeft("0177", 6) );
        builder.row(new int[] { 2, 30, 2, 6 }, "", "FLAVIO BOLSONALRO", "22", builder.padLeft("0068", 6));

        builder.fillLine('-');


        return builder.toDocument();
    }

    private final class BulletinBuilder {
        private static final int BULLETIN_WIDTH = 40;

        private final List<BulletinElement> elements = new ArrayList<>();

        public void left(String text) { left(text, BulletinTextMode.Normal); }
        public void left(String text, BulletinTextMode textMode) {
            elements.add(new BulletinLine(padRight(text, BULLETIN_WIDTH), textMode));
        }

        public void right(String text) { right(text, BulletinTextMode.Normal); }
        public void right(String text, BulletinTextMode textMode) {
            elements.add(new BulletinLine(padLeft(text, BULLETIN_WIDTH), textMode));
        }

        public void center(String text) { centerFill(text, ' ', BulletinTextMode.Normal); }
        public void center(String text, BulletinTextMode textMode) { centerFill(text, ' ', textMode); }
        public void centerFill(String text, char fill) { centerFill(text, fill, BulletinTextMode.Normal); }
        public void centerFill(String text, char fill, BulletinTextMode textMode) {
            String value = truncate(text, BULLETIN_WIDTH);

            int totalPadding = BULLETIN_WIDTH - value.length();
            int leftPadding = totalPadding / 2;
            int rightPadding = totalPadding - leftPadding;

            var v = new StringBuilder()
                    .repeat(fill, leftPadding)
                    .append(value)
                    .repeat(fill, rightPadding)
                    .toString();

            elements.add(new BulletinLine(v, textMode));
        }

        public void fillLine(char c) {
            elements.add(new BulletinLine(Character.toString(c).repeat(BULLETIN_WIDTH), BulletinTextMode.Normal));
        }

        public void row(int[] widths, String... values) { row(BulletinTextMode.Normal, widths, values); }
        public void row(BulletinTextMode textMode, int[] widths, String... values) {
            if (widths.length != values.length) {
                throw new IllegalArgumentException("A quantidade de larguras deve corresponder à quantidade de valores.");
            }

            int totalWidth = 0;
            for (int width : widths) {
                if (width < 0) {
                    throw new IllegalArgumentException("A largura de uma coluna não pode ser negativa.");
                }
                totalWidth += width;
            }

            if (totalWidth > BULLETIN_WIDTH) {
                throw new IllegalArgumentException("A largura total das colunas excede o boletim.");
            }

            var buffer = new StringBuilder();

            for (int i = 0; i < values.length; i++) {
                buffer.append(padRight(values[i], widths[i]));
            }

            elements.add(new BulletinLine(buffer.toString(), textMode));
        }

        public String padLeft(String text, int width) {
            String value = truncate(text, width);
            return " ".repeat(width - value.length()) + value;
        }

        public String padRight(String text, int width) {
            String value = truncate(text, width);
            return value + " ".repeat(width - value.length());
        }

        public String truncate(String text, int width) {
            String value = text == null ? "null" : text;

            if (width <= 0) return "";
            if (value.length() <= width) return value;
            return value.substring(0, width);
        }

        public BulletinDocument toDocument() {
            return new BulletinDocument(List.copyOf(elements));
        }
    }
}
