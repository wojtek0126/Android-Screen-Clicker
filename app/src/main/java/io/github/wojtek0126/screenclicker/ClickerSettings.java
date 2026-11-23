package io.github.wojtek0126.screenclicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.view.WindowManager;

/** Persists the tap point and tap rate selected by the user. */
public final class ClickerSettings {

    private static final String PREFS = "clicker";
    private static final String KEY_PROFILE = "profile";
    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";
    private static final String KEY_RATE = "rate";

    public int profileIndex;
    /** Tap point in the coordinates of the selected {@link ScreenProfile}. */
    public float x;
    public float y;
    public double tapsPerSecond;

    public static ClickerSettings load(Context context) {
        SharedPreferences p = prefs(context);
        ClickerSettings s = new ClickerSettings();
        s.profileIndex = Math.max(0, Math.min(p.getInt(KEY_PROFILE, 0), ScreenProfile.PROFILES.length - 1));
        Point size = screenSize(context);
        s.x = p.getFloat(KEY_X, size.x / 2f);
        s.y = p.getFloat(KEY_Y, size.y / 2f);
        s.tapsPerSecond = TapRate.clamp(p.getFloat(KEY_RATE, 5f));
        return s;
    }

    public void save(Context context) {
        prefs(context).edit()
                .putInt(KEY_PROFILE, profileIndex)
                .putFloat(KEY_X, x)
                .putFloat(KEY_Y, y)
                .putFloat(KEY_RATE, (float) tapsPerSecond)
                .apply();
    }

    public ScreenProfile profile() {
        return ScreenProfile.PROFILES[profileIndex];
    }

    /** Tap point converted to real device pixels. */
    public float[] devicePoint(Context context) {
        Point size = screenSize(context);
        return profile().toDevice(x, y, size.x, size.y);
    }

    /** Sets the tap point from real device pixels. */
    public void setDevicePoint(Context context, float deviceX, float deviceY) {
        Point size = screenSize(context);
        float[] p = profile().fromDevice(deviceX, deviceY, size.x, size.y);
        x = p[0];
        y = p[1];
    }

    /** Full physical screen size in pixels (including system bars). */
    @SuppressWarnings("deprecation")
    public static Point screenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.graphics.Rect bounds = wm.getMaximumWindowMetrics().getBounds();
            return new Point(bounds.width(), bounds.height());
        }
        Point size = new Point();
        wm.getDefaultDisplay().getRealSize(size);
        return size;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
