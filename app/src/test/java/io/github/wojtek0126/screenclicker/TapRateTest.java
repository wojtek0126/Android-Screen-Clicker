package io.github.wojtek0126.screenclicker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TapRateTest {

    @Test
    public void convertsRateToInterval() {
        assertEquals(1000, TapRate.intervalMs(1));
        assertEquals(200, TapRate.intervalMs(5));
        assertEquals(400, TapRate.intervalMs(2.5));
    }

    @Test
    public void clampsRateToSupportedRange() {
        assertEquals(TapRate.MAX_TAPS_PER_SECOND, TapRate.clamp(1000), 0);
        assertEquals(TapRate.MIN_TAPS_PER_SECOND, TapRate.clamp(0), 0);
        assertEquals(TapRate.MIN_TAPS_PER_SECOND, TapRate.clamp(Double.NaN), 0);
        assertEquals(10000, TapRate.intervalMs(-3));
    }

    @Test
    public void intervalIsLongerThanTapDuration() {
        assertTrue(TapRate.intervalMs(TapRate.MAX_TAPS_PER_SECOND) > TapRate.TAP_DURATION_MS);
    }
}
