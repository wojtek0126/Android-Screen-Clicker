package io.github.wojtek0126.screenclicker;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ScreenProfileTest {

    private static final float DELTA = 0.01f;

    @Test
    public void nativeProfileKeepsCoordinates() {
        ScreenProfile profile = new ScreenProfile("native", 0, 0);
        assertTrue(profile.isNative());
        assertArrayEquals(new float[]{100f, 200f}, profile.toDevice(100, 200, 1080, 2400), DELTA);
        assertArrayEquals(new float[]{100f, 200f}, profile.fromDevice(100, 200, 1080, 2400), DELTA);
    }

    @Test
    public void scalesIphonePointToAndroidScreen() {
        ScreenProfile iphone = new ScreenProfile("iPhone", 1179, 2556);
        float[] device = iphone.toDevice(589.5f, 1278f, 1080, 2400);
        assertArrayEquals(new float[]{540f, 1200f}, device, DELTA);
    }

    @Test
    public void percentProfileMapsToFraction() {
        ScreenProfile percent = new ScreenProfile("%", 100, 100);
        assertArrayEquals(new float[]{270f, 1800f}, percent.toDevice(25, 75, 1080, 2400), DELTA);
    }

    @Test
    public void roundTripReturnsOriginalPoint() {
        ScreenProfile profile = new ScreenProfile("Android", 720, 1600);
        float[] device = profile.toDevice(360, 400, 1440, 3200);
        assertArrayEquals(new float[]{360f, 400f},
                profile.fromDevice(device[0], device[1], 1440, 3200), DELTA);
    }

    @Test
    public void portraitProfileFollowsLandscapeDevice() {
        ScreenProfile profile = new ScreenProfile("Android", 1080, 2400);
        // In landscape the profile is treated as 2400 x 1080.
        assertArrayEquals(new float[]{1200f, 540f}, profile.toDevice(1200, 540, 2400, 1080), DELTA);
    }

    @Test
    public void pointsOutsideScreenAreClamped() {
        ScreenProfile profile = new ScreenProfile("Android", 1080, 2400);
        assertArrayEquals(new float[]{1079f, 0f}, profile.toDevice(5000, -10, 1080, 2400), DELTA);
    }

    @Test
    public void builtInProfilesAreValid() {
        assertTrue(ScreenProfile.PROFILES[0].isNative());
        for (int i = 1; i < ScreenProfile.PROFILES.length; i++) {
            ScreenProfile p = ScreenProfile.PROFILES[i];
            assertTrue(p.name, p.width > 0 && p.height > 0);
        }
    }
}
