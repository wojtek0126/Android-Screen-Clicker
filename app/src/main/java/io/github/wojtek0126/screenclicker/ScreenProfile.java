package io.github.wojtek0126.screenclicker;

/**
 * A reference screen in which the user enters tap coordinates.
 * Coordinates are scaled proportionally to the real screen of the device,
 * so a point picked on e.g. an iPhone screenshot lands on the same relative
 * spot of the Android device. Sizes are portrait pixels; a width/height of 0
 * means "this device" (no scaling).
 */
public final class ScreenProfile {

    public static final ScreenProfile[] PROFILES = {
            new ScreenProfile("This device (native pixels)", 0, 0),
            new ScreenProfile("Generic Android HD (720 x 1600)", 720, 1600),
            new ScreenProfile("Generic Android FHD+ (1080 x 2400)", 1080, 2400),
            new ScreenProfile("Generic Android QHD+ (1440 x 3200)", 1440, 3200),
            new ScreenProfile("iPhone SE (750 x 1334)", 750, 1334),
            new ScreenProfile("iPhone 15 / 16 (1179 x 2556)", 1179, 2556),
            new ScreenProfile("iPhone 15 / 16 Pro Max (1290 x 2796)", 1290, 2796),
            new ScreenProfile("iPad 10th gen (1640 x 2360)", 1640, 2360),
            new ScreenProfile("Percent of screen (100 x 100)", 100, 100),
    };

    public final String name;
    public final int width;
    public final int height;

    public ScreenProfile(String name, int width, int height) {
        this.name = name;
        this.width = width;
        this.height = height;
    }

    public boolean isNative() {
        return width <= 0 || height <= 0;
    }

    /** Converts a point given in this profile to real device pixels. */
    public float[] toDevice(float x, float y, int deviceWidth, int deviceHeight) {
        if (isNative()) {
            return new float[]{clamp(x, deviceWidth), clamp(y, deviceHeight)};
        }
        int[] size = orientedSize(deviceWidth, deviceHeight);
        return new float[]{
                clamp(x * deviceWidth / size[0], deviceWidth),
                clamp(y * deviceHeight / size[1], deviceHeight)};
    }

    /** Converts a point given in real device pixels to this profile. */
    public float[] fromDevice(float x, float y, int deviceWidth, int deviceHeight) {
        if (isNative()) {
            return new float[]{x, y};
        }
        int[] size = orientedSize(deviceWidth, deviceHeight);
        return new float[]{x * size[0] / deviceWidth, y * size[1] / deviceHeight};
    }

    /** Profile size matching the orientation (portrait/landscape) of the device. */
    private int[] orientedSize(int deviceWidth, int deviceHeight) {
        boolean deviceLandscape = deviceWidth > deviceHeight;
        boolean profileLandscape = width > height;
        return deviceLandscape == profileLandscape
                ? new int[]{width, height}
                : new int[]{height, width};
    }

    private static float clamp(float value, int size) {
        return Math.max(0f, Math.min(value, size - 1f));
    }

    @Override
    public String toString() {
        return name;
    }
}
