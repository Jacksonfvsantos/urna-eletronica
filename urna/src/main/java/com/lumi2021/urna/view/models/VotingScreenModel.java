package com.lumi2021.urna.view.models;

public sealed interface VotingScreenModel {

    Mode mode();

    enum Mode {
        WAITING, VOTING, SUBMITTING, FINISHED
    }

    record Waiting(String message) implements VotingScreenModel {
        @Override
        public Mode mode() {
            return Mode.WAITING;
        }
    }

    record Submitting(String message) implements VotingScreenModel {
        public Submitting() {
            this("Registrando o seu voto...");
        }

        @Override
        public Mode mode() {
            return Mode.SUBMITTING;
        }
    }

    record Finished() implements VotingScreenModel {
        @Override
        public Mode mode() {
            return Mode.FINISHED;
        }
    }

    record Voting(
            String cargoLabel,
            int digitCount,
            String digits,
            String candidateName,
            String partyName,
            String imageUrl,
            String headline,
            String detail
    ) implements VotingScreenModel {
        public Voting {
            digits = digits == null ? "" : digits;
        }

        @Override
        public Mode mode() {
            return Mode.VOTING;
        }
    }
}