package model;

import java.util.Arrays;
import java.util.Random;

// Generates a random 3x3 scramble as a space-separated WCA-notation string (e.g. "U R2 F' ...").
// Internally a move is represented as its face letter repeated once per quarter-turn (U=90°,
// UU=180°, UUU=270°/U') rather than as "U"/"U2"/"U'" — that repeated-character form is what lets
// validateScramble() detect and remove redundant turns with plain string operations, before
// formatScramble() converts the survivors back into standard notation at the end.
public class ScrambleGenerator {

    private static final String[] MOVES = new String[]{"U", "UU", "UUU", "D", "DD", "DDD", "R", "RR", "RRR", "L", "LL", "LLL", "F", "FF", "FFF", "B", "BB", "BBB"};
    private static final Random RANDOM = new Random();

    public static String generateScramble() {
        String scramble = "";
        while (countMoves(scramble) < 20) {
            scramble = validateScramble(scramble + MOVES[RANDOM.nextInt(MOVES.length)]);
        }
        return formatScramble(scramble);
    }

    // A "move" is a maximal run of the same face character, not a single character — U/UU/UUU are
    // one move each, so this groups runs rather than just counting characters.
    private static int countMoves(String scramble) {
        if (scramble.isEmpty()) return 0;
        int count = 0;
        int i = 0;
        while (i < scramble.length()) {
            char face = scramble.charAt(i);
            while (i < scramble.length() && scramble.charAt(i) == face) {
                i++;
            }
            count++;
        }
        return count;
    }

    // Re-checked after every appended move (not just once at the end) so a redundant turn is
    // pruned as soon as it appears — sortOppositePairs() first, since it can expose new
    // four-in-a-row runs for cancelFours() to remove that weren't adjacent before sorting.
    private static String validateScramble(String scramble) {
        scramble = sortOppositePairs(scramble);
        scramble = cancelFours(scramble);
        return scramble;
    }

    // Opposite faces (U/D, R/L, F/B) don't share any layer, so turning one never disturbs the
    // other — a run made up of only those two faces' characters can be freely reordered without
    // changing the resulting cube state. Sorting each such run groups identical characters
    // together, which is what lets cancelFours() catch e.g. two U turns that were picked on
    // separate random draws with a D turn randomly landing between them (raw "UDU" becomes "DUU"),
    // not just ones that happened to be generated back-to-back.
    private static String sortOppositePairs(String scramble) {
        String[] pairs = {"UD", "RL", "FB"};
        for (String pair : pairs) {
            char a = pair.charAt(0);
            char b = pair.charAt(1);
            String[] blocks = scramble.split("(?<![" + a + b + "])(?=[" + a + b + "])|(?<=[" + a + b + "])(?![" + a + b + "])");
            String result = "";
            for (String block : blocks) {
                if (block.matches("[" + a + b + "]+")) {
                    char[] chars = block.toCharArray();
                    Arrays.sort(chars);
                    result += new String(chars);
                } else {
                    result += block;
                }
            }
            scramble = result;
        }
        return scramble;
    }

    // Four of the same face character in a row sum to a full 360° turn — a no-op — so that run is
    // deleted outright rather than reduced. Repeats because deleting one run can join two
    // previously-separated runs into a new four-in-a-row (e.g. "UU" + "" + "UU" after something
    // between them is removed).
    private static String cancelFours(String scramble) {
        String previous;
        do {
            previous = scramble;
            scramble = scramble.replaceAll("([UDRLFB])\\1{3}", "");
        } while (!scramble.equals(previous));
        return scramble;
    }

    // Converts the internal repeated-character form back into standard WCA notation: a lone
    // character is a quarter turn, two is a half turn ("2"), three is a reverse quarter turn ("'").
    private static String formatScramble(String scramble) {
        String formatted = "";
        int i = 0;
        while (i < scramble.length()) {
            char face = scramble.charAt(i);
            int count = 0;
            while (i < scramble.length() && scramble.charAt(i) == face) {
                count++;
                i++;
            }
            if (!formatted.isEmpty()) formatted += " ";
            switch (count) {
                case 1 -> formatted += face;
                case 2 -> formatted += face + "2";
                case 3 -> formatted += face + "'";
                default -> {
                }
            }
        }
        return formatted;
    }
}