package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import model.Solve;
import model.StatSpec;

public class StatsService {

    // Dispatches to averageOf/mean/personalBest based on the spec's type, so callers can drive
    // an arbitrary, user-configured list of StatSpecs without a switch of their own.
    public Optional<Long> compute(StatSpec spec, List<Solve> solves) {
        return switch (spec.type()) {
            case AVERAGE -> averageOf(solves, spec.windowSize());
            case MEAN -> mean(solves);
            case PB -> personalBest(solves);
        };
    }

    // Average of the most recent windowSize solves (ao5 = 5, ao12 = 12, etc.); solves must be oldest-first, as SolveDB returns them.
    public Optional<Long> averageOf(List<Solve> solves, int windowSize) {
        if (windowSize < 3) {
            throw new IllegalArgumentException("Average window must be at least 3 solves: " + windowSize);
        }
        if (solves.size() < windowSize) {
            return Optional.empty();
        }

        List<Solve> window = solves.subList(solves.size() - windowSize, solves.size());
        List<Long> times = new ArrayList<>();
        for (Solve solve : window) {
            times.add(solve.getEffectiveTimeMs());
        }
        Collections.sort(times);

        List<Long> trimmed = times.subList(1, times.size() - 1);
        // DNF's effective time is Long.MAX_VALUE, so it's already the "worst" and gets trimmed above when there's
        // exactly one; a second DNF can't also be dropped, so its MAX_VALUE surviving here means the whole average is a DNF.
        if (trimmed.get(trimmed.size() - 1) == Long.MAX_VALUE) {
            return Optional.of(Long.MAX_VALUE);
        }

        long sum = 0;
        for (long time : trimmed) {
            sum += time;
        }
        return Optional.of(Math.round((double) sum / trimmed.size()));
    }

    // Arithmetic mean of all given solves; any DNF makes the mean itself a DNF.
    public Optional<Long> mean(List<Solve> solves) {
        if (solves.isEmpty()) {
            return Optional.empty();
        }

        long sum = 0;
        for (Solve solve : solves) {
            long time = solve.getEffectiveTimeMs();
            if (time == Long.MAX_VALUE) {
                return Optional.of(Long.MAX_VALUE);
            }
            sum += time;
        }
        return Optional.of(Math.round((double) sum / solves.size()));
    }

    // Best (lowest) effective time among the given solves; DNFs never qualify since their effective time is Long.MAX_VALUE.
    public Optional<Long> personalBest(List<Solve> solves) {
        long best = Long.MAX_VALUE;
        boolean found = false;
        for (Solve solve : solves) {
            long time = solve.getEffectiveTimeMs();
            if (time < best) {
                best = time;
                found = true;
            }
        }
        return found ? Optional.of(best) : Optional.empty();
    }
}
