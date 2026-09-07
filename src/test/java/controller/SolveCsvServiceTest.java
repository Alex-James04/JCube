package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Penalty;
import model.Solve;

class SolveCsvServiceTest {

    SolveCsvService csv;

    @BeforeEach
    void setUp() {
        csv = new SolveCsvService();
    }

    private Solve solveWithDate(long timeMs, String scramble, LocalDateTime createdAt) {
        return new Solve(-1, 1, timeMs, Penalty.NONE, scramble, createdAt);
    }

    // ---- export ----

    @Test
    void exportWritesTheExpectedHeader() {
        String result = csv.export(List.of());
        assertEquals("No.;Time;Comment;Scramble;Date;P.1\n", result);
    }

    @Test
    void exportFormatsANormalSolve() {
        Solve solve = solveWithDate(54048, "R U R' U'", LocalDateTime.of(2026, 3, 10, 8, 11, 44));
        String result = csv.export(List.of(solve));
        assertEquals("No.;Time;Comment;Scramble;Date;P.1\n1;54.048;;R U R' U';2026-03-10 08:11:44;54.048\n", result);
    }

    @Test
    void exportAppliesPlus2ToTheFinalColumnOnly() {
        Solve solve = solveWithDate(30000, "", LocalDateTime.of(2026, 1, 1, 0, 0, 0));
        solve.setPenalty(Penalty.PLUS2);
        String result = csv.export(List.of(solve));
        assertTrue(result.endsWith("1;30.000;;;2026-01-01 00:00:00;32.000\n"));
    }

    @Test
    void exportKeepsTheRawTimeEvenForADnf() {
        Solve solve = solveWithDate(45000, "", LocalDateTime.of(2026, 1, 1, 0, 0, 0));
        solve.setPenalty(Penalty.DNF);
        String result = csv.export(List.of(solve));
        assertTrue(result.endsWith("1;45.000;;;2026-01-01 00:00:00;DNF\n"));
    }

    @Test
    void exportNumbersRowsSequentiallyRegardlessOfSolveId() {
        Solve a = new Solve(99, 1, 10000, Penalty.NONE, "", LocalDateTime.of(2026, 1, 1, 0, 0, 0));
        Solve b = new Solve(5, 1, 20000, Penalty.NONE, "", LocalDateTime.of(2026, 1, 1, 0, 0, 1));
        String result = csv.export(List.of(a, b));
        assertTrue(result.contains("1;10.000;"));
        assertTrue(result.contains("2;20.000;"));
    }

    // ---- parse ----

    @Test
    void parseReadsANormalSolve() {
        String content = "No.;Time;Comment;Scramble;Date;P.1\n1;54.048;;R U R' U';2026-03-10 08:11:44;54.048\n";
        List<Solve> solves = csv.parse(content, 7);
        assertEquals(1, solves.size());
        Solve solve = solves.get(0);
        assertEquals(7, solve.getSessionId());
        assertEquals(54048L, solve.getTimeMs());
        assertEquals(Penalty.NONE, solve.getPenalty());
        assertEquals("R U R' U'", solve.getScramble());
        assertEquals(LocalDateTime.of(2026, 3, 10, 8, 11, 44), solve.getCreatedAt());
        assertEquals(-1, solve.getId());
    }

    @Test
    void parseDetectsPlus2FromATwoSecondGapToFinal() {
        String content = "No.;Time;Comment;Scramble;Date;P.1\n1;30.000;;;2026-01-01 00:00:00;32.000\n";
        Solve solve = csv.parse(content, 1).get(0);
        assertEquals(Penalty.PLUS2, solve.getPenalty());
        assertEquals(30000L, solve.getTimeMs());
    }

    @Test
    void parseDetectsDnfFromTheFinalColumnButKeepsTheRawTime() {
        String content = "No.;Time;Comment;Scramble;Date;P.1\n1;45.000;;;2026-01-01 00:00:00;DNF\n";
        Solve solve = csv.parse(content, 1).get(0);
        assertEquals(Penalty.DNF, solve.getPenalty());
        assertEquals(45000L, solve.getTimeMs());
    }

    @Test
    void parseDetectsDnfWhenTimeItselfIsDnfText() {
        String content = "No.;Time;Comment;Scramble;Date;P.1\n1;DNF;;;2026-01-01 00:00:00;DNF\n";
        Solve solve = csv.parse(content, 1).get(0);
        assertEquals(Penalty.DNF, solve.getPenalty());
        assertEquals(0L, solve.getTimeMs());
    }

    @Test
    void parseSkipsBlankLines() {
        String content = "No.;Time;Comment;Scramble;Date;P.1\n1;30.000;;;2026-01-01 00:00:00;30.000\n\n2;20.000;;;2026-01-01 00:00:01;20.000\n";
        assertEquals(2, csv.parse(content, 1).size());
    }

    @Test
    void parseRejectsALineWithTooFewFieldsAndNamesTheLine() {
        String content = "No.;Time;Comment;Scramble;Date;P.1\n1;30.000;;\n";
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> csv.parse(content, 1));
        assertTrue(e.getMessage().contains("line 2"));
    }

    @Test
    void parseHandlesTheProvidedSampleFormat() {
        String content = "No.;Time;Comment;Scramble;Date;P.1\n"
                + "1;54.048;;;2026-03-10 08:11:44;54.048\n"
                + "2;37.048;;;2026-03-10 08:13:16;37.048\n";
        List<Solve> solves = csv.parse(content, 3);
        assertEquals(2, solves.size());
        assertEquals(54048L, solves.get(0).getTimeMs());
        assertEquals(37048L, solves.get(1).getTimeMs());
        assertEquals(Penalty.NONE, solves.get(0).getPenalty());
    }

    // ---- round trip ----

    @Test
    void exportThenParseRoundTripsNormalPlus2AndDnfSolves() {
        Solve normal = solveWithDate(30000, "R U R'", LocalDateTime.of(2026, 1, 1, 12, 0, 0));
        Solve plus2 = solveWithDate(25000, "F R U", LocalDateTime.of(2026, 1, 1, 12, 1, 0));
        plus2.setPenalty(Penalty.PLUS2);
        Solve dnf = solveWithDate(40000, "L D B", LocalDateTime.of(2026, 1, 1, 12, 2, 0));
        dnf.setPenalty(Penalty.DNF);

        String exported = csv.export(List.of(normal, plus2, dnf));
        List<Solve> reimported = csv.parse(exported, 1);

        assertEquals(3, reimported.size());
        assertEquals(30000L, reimported.get(0).getTimeMs());
        assertEquals(Penalty.NONE, reimported.get(0).getPenalty());
        assertEquals(25000L, reimported.get(1).getTimeMs());
        assertEquals(Penalty.PLUS2, reimported.get(1).getPenalty());
        assertEquals(40000L, reimported.get(2).getTimeMs());
        assertEquals(Penalty.DNF, reimported.get(2).getPenalty());
    }
}
