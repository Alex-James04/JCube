package controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import model.Penalty;
import model.Solve;

// Reads and writes the semicolon-delimited "No.;Time;Comment;Scramble;Date;P.1" format used by
// common third-party cube timers, so sessions can be moved in and out of JCube. Time is the raw
// pre-penalty solve time in seconds; P.1 is the final time after any penalty (Time+2 for a +2,
// or the literal text "DNF" for a DNF). The Comment column isn't modeled by JCube and is always
// written empty; on import it's simply ignored.
public class SolveCsvService {

    private static final String HEADER = "No.;Time;Comment;Scramble;Date;P.1";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int PLUS2_MS = 2000;

    public String export(List<Solve> solves) {
        StringBuilder csv = new StringBuilder(HEADER).append('\n');
        int number = 1;
        for (Solve solve : solves) {
            csv.append(number++).append(';')
                    .append(formatSeconds(solve.getTimeMs())).append(';')
                    .append(';')
                    .append(solve.getScramble() == null ? "" : solve.getScramble()).append(';')
                    .append(solve.getCreatedAt() == null ? "" : solve.getCreatedAt().format(DATE_FORMAT)).append(';')
                    .append(formatFinal(solve))
                    .append('\n');
        }
        return csv.toString();
    }

    public List<Solve> parse(String csv, int sessionId) {
        List<Solve> solves = new ArrayList<>();
        String[] lines = csv.split("\r?\n");
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) {
                continue;
            }
            try {
                solves.add(parseRow(line, sessionId));
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Failed to parse line " + (i + 1) + ": " + line, e);
            }
        }
        return solves;
    }

    private Solve parseRow(String line, int sessionId) {
        String[] fields = line.split(";", -1);
        if (fields.length < 6) {
            throw new IllegalArgumentException("Expected at least 6 fields, found " + fields.length);
        }

        String timeField = fields[1].trim();
        String scramble = fields[3].trim();
        String dateField = fields[4].trim();
        String finalField = fields[5].trim();

        LocalDateTime createdAt = dateField.isEmpty() ? null : LocalDateTime.parse(dateField, DATE_FORMAT);

        // Time keeps the real pre-penalty attempt even when the row is a DNF (this is what our own
        // export writes), so only fall back to a zero time when Time itself has no numeric value.
        if (isDnf(timeField)) {
            return new Solve(-1, sessionId, 0L, Penalty.DNF, scramble, createdAt);
        }

        long rawMs = parseSecondsToMs(timeField);
        if (isDnf(finalField)) {
            return new Solve(-1, sessionId, rawMs, Penalty.DNF, scramble, createdAt);
        }

        long finalMs = parseSecondsToMs(finalField);
        Penalty penalty = finalMs - rawMs == PLUS2_MS ? Penalty.PLUS2 : Penalty.NONE;
        return new Solve(-1, sessionId, rawMs, penalty, scramble, createdAt);
    }

    private boolean isDnf(String field) {
        return "DNF".equalsIgnoreCase(field);
    }

    private long parseSecondsToMs(String seconds) {
        return Math.round(Double.parseDouble(seconds) * 1000.0);
    }

    private String formatSeconds(long ms) {
        return String.format(Locale.ROOT, "%.3f", ms / 1000.0);
    }

    private String formatFinal(Solve solve) {
        long effectiveMs = solve.getEffectiveTimeMs();
        return effectiveMs == Long.MAX_VALUE ? "DNF" : formatSeconds(effectiveMs);
    }
}
