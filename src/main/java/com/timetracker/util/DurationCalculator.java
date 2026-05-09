package com.timetracker.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Pure-function utility for work-session duration arithmetic.
 * Kept UI-free so it can be unit-tested without a JavaFX runtime.
 */
public final class DurationCalculator {

    private DurationCalculator() {}

    /**
     * Calculate decimal hours between two timestamps, rounded to 2 decimal places.
     * Example: 90 minutes → 1.50
     */
    public static double calculateHours(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn == null || checkOut == null) return 0.0;
        long minutes = ChronoUnit.MINUTES.between(checkIn, checkOut);
        return Math.round(minutes / 60.0 * 100.0) / 100.0;
    }

    /**
     * Format decimal hours as "Xh YYm".
     * Example: 1.75 → "1h 45m"
     */
    public static String formatHoursShort(double hours) {
        long totalMinutes = (long) (hours * 60);
        long h = totalMinutes / 60;
        long m = totalMinutes % 60;
        return String.format("%dh %02dm", h, m);
    }

    /**
     * Format decimal hours as "Xh YYm (Z.ZZ hrs)".
     * Example: 1.75 → "1h 45m (1.75 hrs)"
     */
    public static String formatHoursFull(double hours) {
        return String.format("%s (%.2f hrs)", formatHoursShort(hours), hours);
    }
}
