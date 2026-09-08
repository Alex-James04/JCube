package controller;

// Shared m:ss.fff-style formatting for solve/stat times, used wherever a raw millisecond value
// needs to become the text the user sees. decimalPlaces (0-3) comes from Settings — 0 rounds to
// whole seconds, 3 shows raw milliseconds; the rounding unit changes accordingly so e.g. 0
// decimal places doesn't just truncate the ".XX" off a value that was actually rounded up.
public final class TimeFormatter {

    private TimeFormatter() {
    }

    public static String format(long ms, int decimalPlaces) {
        if (ms == Long.MAX_VALUE) {
            return "DNF";
        }

        long unitsPerSecond = (long) Math.pow(10, decimalPlaces);
        long msPerUnit = 1000 / unitsPerSecond;
        long totalUnits = Math.round(ms / (double) msPerUnit);

        long minutes = totalUnits / (unitsPerSecond * 60);
        long secondsWhole = (totalUnits / unitsPerSecond) % 60;
        long fraction = totalUnits % unitsPerSecond;

        StringBuilder text = new StringBuilder();
        if (minutes > 0) {
            text.append(minutes).append(':').append(String.format("%02d", secondsWhole));
        } else {
            text.append(secondsWhole);
        }
        if (decimalPlaces > 0) {
            text.append('.').append(String.format("%0" + decimalPlaces + "d", fraction));
        }
        return text.toString();
    }
}
