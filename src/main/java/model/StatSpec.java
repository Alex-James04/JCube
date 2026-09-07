package model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record StatSpec(Type type, int windowSize) {

    public enum Type { AVERAGE, MEAN, PB }

    public StatSpec {
        if (type == Type.AVERAGE && windowSize < 3) {
            throw new IllegalArgumentException("Average window must be at least 3 solves: " + windowSize);
        }
        if (type != Type.AVERAGE && windowSize != 0) {
            throw new IllegalArgumentException(type + " does not take a window size");
        }
    }

    public static StatSpec average(int windowSize) {
        return new StatSpec(Type.AVERAGE, windowSize);
    }

    public static StatSpec mean() {
        return new StatSpec(Type.MEAN, 0);
    }

    public static StatSpec pb() {
        return new StatSpec(Type.PB, 0);
    }

    public String label() {
        return switch (type) {
            case AVERAGE -> "ao" + windowSize;
            case MEAN -> "Mean";
            case PB -> "PB";
        };
    }

    public String encode() {
        return type == Type.AVERAGE ? "AO" + windowSize : type.name();
    }

    public static StatSpec parse(String token) {
        String trimmed = token.trim().toUpperCase();
        if (trimmed.equals("MEAN")) {
            return mean();
        }
        if (trimmed.equals("PB")) {
            return pb();
        }
        if (trimmed.startsWith("AO")) {
            try {
                return average(Integer.parseInt(trimmed.substring(2)));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unrecognized stat spec: " + token, e);
            }
        }
        throw new IllegalArgumentException("Unrecognized stat spec: " + token);
    }

    public static List<StatSpec> parseList(String csv) {
        List<StatSpec> specs = new ArrayList<>();
        for (String token : csv.split(",")) {
            if (!token.isBlank()) {
                specs.add(parse(token));
            }
        }
        return specs;
    }

    public static String encodeList(List<StatSpec> specs) {
        return specs.stream().map(StatSpec::encode).collect(Collectors.joining(","));
    }
}
