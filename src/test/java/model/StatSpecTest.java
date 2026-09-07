package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class StatSpecTest {

    @Test
    void averageRejectsWindowsSmallerThanThree() {
        assertThrows(IllegalArgumentException.class, () -> StatSpec.average(2));
    }

    @Test
    void meanAndPbRejectAWindowSize() {
        assertThrows(IllegalArgumentException.class, () -> new StatSpec(StatSpec.Type.MEAN, 5));
        assertThrows(IllegalArgumentException.class, () -> new StatSpec(StatSpec.Type.PB, 5));
    }

    @Test
    void labelsReadableForms() {
        assertEquals("ao5", StatSpec.average(5).label());
        assertEquals("ao100", StatSpec.average(100).label());
        assertEquals("Mean", StatSpec.mean().label());
        assertEquals("PB", StatSpec.pb().label());
    }

    @Test
    void encodeAndParseRoundTripForAnyWindowSize() {
        StatSpec original = StatSpec.average(1000);
        assertEquals(original, StatSpec.parse(original.encode()));
    }

    @Test
    void parseIsCaseInsensitiveAndTrims() {
        assertEquals(StatSpec.average(12), StatSpec.parse("  ao12 "));
        assertEquals(StatSpec.mean(), StatSpec.parse("mean"));
        assertEquals(StatSpec.pb(), StatSpec.parse("pb"));
    }

    @Test
    void parseRejectsUnrecognizedTokens() {
        assertThrows(IllegalArgumentException.class, () -> StatSpec.parse("BEST"));
        assertThrows(IllegalArgumentException.class, () -> StatSpec.parse("AOX"));
    }

    @Test
    void parseListAndEncodeListRoundTrip() {
        List<StatSpec> specs = List.of(StatSpec.average(5), StatSpec.average(12), StatSpec.mean(), StatSpec.pb());
        String encoded = StatSpec.encodeList(specs);
        assertEquals(specs, StatSpec.parseList(encoded));
    }

    @Test
    void parseListIgnoresBlankTokens() {
        assertEquals(List.of(StatSpec.average(5), StatSpec.pb()), StatSpec.parseList("AO5,,PB,"));
    }
}
