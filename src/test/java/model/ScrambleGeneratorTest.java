package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ScrambleGeneratorTest {

    @Test
    void scrambleHasTwentyMoves() {
        String scramble = ScrambleGenerator.generateScramble();
        assertEquals(20, countMoves(scramble));
    }

    @Test
    void scrambleTokensAreValidFormat() {
        String scramble = ScrambleGenerator.generateScramble();
        String[] tokens = scramble.split(" ");
        for (String token : tokens) {
            assertTrue(isValidToken(token), "Invalid token: " + token);
        }
    }

    @Test
    void scrambleContainsNoRawRepeatedLetters() {
        String scramble = ScrambleGenerator.generateScramble();
        String[] tokens = scramble.split(" ");
        for (String token : tokens) {
            assertTrue(token.length() <= 2, "Token contains raw repeated letters: " + token);
        }
    }

    @Test
    void scrambleHasNoConsecutiveSameFaceMoves() {
        String scramble = ScrambleGenerator.generateScramble();
        String[] tokens = scramble.split(" ");
        for (int i = 0; i < tokens.length - 1; i++) {
            char currentFace = tokens[i].charAt(0);
            char nextFace = tokens[i + 1].charAt(0);
            assertTrue(currentFace != nextFace,
                "Consecutive same face moves found: " + tokens[i] + " " + tokens[i + 1]);
        }
    }

    @Test
    void scrambleHasNoUAfterDInAdjacentBlock() {
        for (int i = 0; i < 100; i++) {
            String scramble = ScrambleGenerator.generateScramble();
            String[] tokens = scramble.split(" ");
            for (int j = 0; j < tokens.length - 1; j++) {
                char current = tokens[j].charAt(0);
                char next = tokens[j + 1].charAt(0);
                if (current == 'D') {
                    assertTrue(next != 'U',
                        "U appeared after D in scramble: " + scramble);
                }
            }
        }
    }

    @Test
    void scrambleHasNoRAfterLInAdjacentBlock() {
        for (int i = 0; i < 100; i++) {
            String scramble = ScrambleGenerator.generateScramble();
            String[] tokens = scramble.split(" ");
            for (int j = 0; j < tokens.length - 1; j++) {
                char current = tokens[j].charAt(0);
                char next = tokens[j + 1].charAt(0);
                if (current == 'L') {
                    assertTrue(next != 'R',
                        "R appeared after L in scramble: " + scramble);
                }
            }
        }
    }

    @Test
    void scrambleHasNoBAfterFInAdjacentBlock() {
        for (int i = 0; i < 100; i++) {
            String scramble = ScrambleGenerator.generateScramble();
            String[] tokens = scramble.split(" ");
            for (int j = 0; j < tokens.length - 1; j++) {
                char current = tokens[j].charAt(0);
                char next = tokens[j + 1].charAt(0);
                if (current == 'F') {
                    assertTrue(next != 'B',
                        "B appeared after F in scramble: " + scramble);
                }
            }
        }
    }

    @Test
    void scrambleContainsBMoves() {
        boolean found = false;
        for (int i = 0; i < 100; i++) {
            String scramble = ScrambleGenerator.generateScramble();
            if (scramble.contains("B")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "B move never appeared across 100 scrambles");
    }

    @Test
    void allSixFacesAppearAcrossMultipleScrambles() {
        boolean foundU = false, foundD = false, foundR = false;
        boolean foundL = false, foundF = false, foundB = false;
        for (int i = 0; i < 100; i++) {
            String scramble = ScrambleGenerator.generateScramble();
            if (scramble.contains("U")) foundU = true;
            if (scramble.contains("D")) foundD = true;
            if (scramble.contains("R")) foundR = true;
            if (scramble.contains("L")) foundL = true;
            if (scramble.contains("F")) foundF = true;
            if (scramble.contains("B")) foundB = true;
        }
        assertTrue(foundU, "U never appeared across 100 scrambles");
        assertTrue(foundD, "D never appeared across 100 scrambles");
        assertTrue(foundR, "R never appeared across 100 scrambles");
        assertTrue(foundL, "L never appeared across 100 scrambles");
        assertTrue(foundF, "F never appeared across 100 scrambles");
        assertTrue(foundB, "B never appeared across 100 scrambles");
    }

    private int countMoves(String scramble) {
        return scramble.split(" ").length;
    }

    private boolean isValidToken(String token) {
        if (token.length() == 1) {
            return "UDRLFD".contains(token) || token.equals("B");
        }
        if (token.length() == 2) {
            char face = token.charAt(0);
            char modifier = token.charAt(1);
            boolean validFace = "UDRLFB".indexOf(face) >= 0;
            boolean validModifier = modifier == '2' || modifier == '\'';
            return validFace && validModifier;
        }
        return false;
    }
}