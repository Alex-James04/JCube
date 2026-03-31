package model;

import java.util.Arrays;
import java.util.Random;

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

    private static String validateScramble(String scramble) {
        scramble = sortOppositePairs(scramble);
        scramble = cancelFours(scramble);
        return scramble;
    }

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

    private static String cancelFours(String scramble) {
        String previous;
        do {
            previous = scramble;
            scramble = scramble.replaceAll("([UDRLFB])\\1{3}", "");
        } while (!scramble.equals(previous));
        return scramble;
    }

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