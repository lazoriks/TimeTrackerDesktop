package com.timetracker.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DurationCalculator")
class DurationCalculatorTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 1, 10, 9, 0, 0);

    // ── calculateHours ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("exact 1 hour → 1.00")
    void calculateHours_exactOneHour() {
        double result = DurationCalculator.calculateHours(BASE, BASE.plusHours(1));
        assertEquals(1.00, result, 0.001);
    }

    @Test
    @DisplayName("30 minutes → 0.50")
    void calculateHours_halfHour() {
        double result = DurationCalculator.calculateHours(BASE, BASE.plusMinutes(30));
        assertEquals(0.50, result, 0.001);
    }

    @Test
    @DisplayName("15 minutes → 0.25")
    void calculateHours_quarterHour() {
        double result = DurationCalculator.calculateHours(BASE, BASE.plusMinutes(15));
        assertEquals(0.25, result, 0.001);
    }

    @Test
    @DisplayName("8 hours 30 minutes → 8.50")
    void calculateHours_8h30m() {
        double result = DurationCalculator.calculateHours(BASE, BASE.plusHours(8).plusMinutes(30));
        assertEquals(8.50, result, 0.001);
    }

    @Test
    @DisplayName("1 minute → 0.02")
    void calculateHours_oneMinute() {
        double result = DurationCalculator.calculateHours(BASE, BASE.plusMinutes(1));
        assertEquals(0.02, result, 0.001);
    }

    @Test
    @DisplayName("0 minutes → 0.00")
    void calculateHours_zeroMinutes() {
        double result = DurationCalculator.calculateHours(BASE, BASE);
        assertEquals(0.00, result, 0.001);
    }

    @Test
    @DisplayName("null checkIn → 0.00 (safe fallback)")
    void calculateHours_nullCheckIn() {
        assertEquals(0.0, DurationCalculator.calculateHours(null, BASE));
    }

    @Test
    @DisplayName("null checkOut → 0.00 (safe fallback)")
    void calculateHours_nullCheckOut() {
        assertEquals(0.0, DurationCalculator.calculateHours(BASE, null));
    }

    @ParameterizedTest(name = "{0} min → {1} hrs")
    @CsvSource({
        "60,  1.00",
        "90,  1.50",
        "120, 2.00",
        "45,  0.75",
        "480, 8.00",
        "510, 8.50"
    })
    @DisplayName("parameterised minute→hour conversion")
    void calculateHours_parameterised(int minutes, double expected) {
        double result = DurationCalculator.calculateHours(BASE, BASE.plusMinutes(minutes));
        assertEquals(expected, result, 0.001);
    }

    // ── formatHoursShort ───────────────────────────────────────────────────────

    @Test
    @DisplayName("2.00 hrs → \"2h 00m\"")
    void formatShort_wholeHours() {
        assertEquals("2h 00m", DurationCalculator.formatHoursShort(2.0));
    }

    @Test
    @DisplayName("1.50 hrs → \"1h 30m\"")
    void formatShort_withMinutes() {
        assertEquals("1h 30m", DurationCalculator.formatHoursShort(1.5));
    }

    @Test
    @DisplayName("0.25 hrs → \"0h 15m\"")
    void formatShort_underOneHour() {
        assertEquals("0h 15m", DurationCalculator.formatHoursShort(0.25));
    }

    @Test
    @DisplayName("8.50 hrs → \"8h 30m\"")
    void formatShort_8h30m() {
        assertEquals("8h 30m", DurationCalculator.formatHoursShort(8.5));
    }

    @Test
    @DisplayName("single-digit minutes are zero-padded")
    void formatShort_zeropadMinutes() {
        assertEquals("1h 05m", DurationCalculator.formatHoursShort(1.0 + 5.0 / 60.0));
    }

    // ── formatHoursFull ────────────────────────────────────────────────────────

    @Test
    @DisplayName("full format contains short format and decimal")
    void formatFull_containsBothParts() {
        String result = DurationCalculator.formatHoursFull(1.5);
        assertTrue(result.contains("1h 30m"),  "should contain short form");
        assertTrue(result.contains("1.50 hrs"), "should contain decimal form");
    }

    @Test
    @DisplayName("full format: 8.00 → \"8h 00m (8.00 hrs)\"")
    void formatFull_exactHours() {
        assertEquals("8h 00m (8.00 hrs)", DurationCalculator.formatHoursFull(8.0));
    }
}
