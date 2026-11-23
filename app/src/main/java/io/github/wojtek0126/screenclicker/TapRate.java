package io.github.wojtek0126.screenclicker;

/** Converts a tap rate (taps per second) into a delay between taps. */
public final class TapRate {

    public static final double MIN_TAPS_PER_SECOND = 0.1;
    public static final double MAX_TAPS_PER_SECOND = 20;
    /** How long the finger "touches" the screen for a single tap. */
    public static final long TAP_DURATION_MS = 10;

    private TapRate() {
    }

    public static double clamp(double tapsPerSecond) {
        if (Double.isNaN(tapsPerSecond)) {
            return MIN_TAPS_PER_SECOND;
        }
        return Math.max(MIN_TAPS_PER_SECOND, Math.min(tapsPerSecond, MAX_TAPS_PER_SECOND));
    }

    public static long intervalMs(double tapsPerSecond) {
        return Math.round(1000.0 / clamp(tapsPerSecond));
    }
}
