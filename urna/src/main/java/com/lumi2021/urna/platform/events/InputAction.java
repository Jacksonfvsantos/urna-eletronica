package com.lumi2021.urna.platform.events;

public enum InputAction {
    NONE,
    KEY_1, KEY_2, KEY_3,
    KEY_4, KEY_5, KEY_6,
    KEY_7, KEY_8, KEY_9,
    KEY_0,
    CONFIRMA, CORRIGE, BRANCO;

    public static InputAction fromInteger(int x) {
        return switch (x) {
            case 0 -> KEY_0;
            case 1 -> KEY_1;
            case 2 -> KEY_2;
            case 3 -> KEY_3;
            case 4 -> KEY_4;
            case 5 -> KEY_5;
            case 6 -> KEY_6;
            case 7 -> KEY_7;
            case 8 -> KEY_8;
            case 9 -> KEY_9;
            default -> NONE;
        };
    }
    public static int toInteger(InputAction x) {
        return switch (x) {
            case KEY_0 -> 0;
            case KEY_1 -> 1;
            case KEY_2 -> 2;
            case KEY_3 -> 3;
            case KEY_4 -> 4;
            case KEY_5 -> 5;
            case KEY_6 -> 6;
            case KEY_7 -> 7;
            case KEY_8 -> 8;
            case KEY_9 -> 9;
            default -> 0;
        };
    }

}