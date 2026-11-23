package io.github.wojtek0126.screenclicker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Accessibility service that shows a floating target + control panel and
 * performs continuous taps on the target using {@link #dispatchGesture}.
 */
public class ClickerService extends AccessibilityService {

    private static ClickerService instance;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private WindowManager windowManager;

    private CrosshairView crosshair;
    private WindowManager.LayoutParams crosshairParams;
    private LinearLayout panel;
    private WindowManager.LayoutParams panelParams;
    private Button startStopButton;
    private TextView counterText;

    private boolean running;
    private long intervalMs;
    private float tapX;
    private float tapY;
    private long tapCount;

    private final Runnable tapLoop = new Runnable() {
        @Override
        public void run() {
            if (!running) {
                return;
            }
            tap(tapX, tapY);
            tapCount++;
            if (counterText != null) {
                counterText.setText(getString(R.string.tap_count, tapCount));
            }
            handler.postDelayed(this, intervalMs);
        }
    };

    /** Returns the connected service or {@code null} if it is not enabled. */
    public static ClickerService getInstance() {
        return instance;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Events are not needed; the service is only used to dispatch gestures.
    }

    @Override
    public void onInterrupt() {
        stopTapping();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        hideOverlay();
        instance = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        hideOverlay();
        instance = null;
        super.onDestroy();
    }

    public boolean isOverlayShown() {
        return crosshair != null;
    }

    /** Shows (or refreshes) the floating target and the control panel. */
    public void showOverlay() {
        stopTapping();
        if (crosshair == null) {
            createOverlay();
        }
        float[] point = ClickerSettings.load(this).devicePoint(this);
        int size = crosshairParams.width;
        crosshairParams.x = Math.round(point[0] - size / 2f);
        crosshairParams.y = Math.round(point[1] - size / 2f);
        windowManager.updateViewLayout(crosshair, crosshairParams);
    }

    public void hideOverlay() {
        stopTapping();
        if (crosshair != null) {
            windowManager.removeView(crosshair);
            crosshair = null;
        }
        if (panel != null) {
            windowManager.removeView(panel);
            panel = null;
        }
    }

    public void startTapping() {
        if (running || crosshair == null) {
            return;
        }
        ClickerSettings settings = ClickerSettings.load(this);
        float[] point = settings.devicePoint(this);
        tapX = point[0];
        tapY = point[1];
        intervalMs = TapRate.intervalMs(settings.tapsPerSecond);
        tapCount = 0;
        running = true;
        // Let injected taps pass through the target marker to the app below.
        crosshairParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        windowManager.updateViewLayout(crosshair, crosshairParams);
        crosshair.setActive(true);
        startStopButton.setText(R.string.stop);
        handler.post(tapLoop);
    }

    public void stopTapping() {
        handler.removeCallbacks(tapLoop);
        if (!running) {
            return;
        }
        running = false;
        if (crosshair != null) {
            crosshairParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            windowManager.updateViewLayout(crosshair, crosshairParams);
            crosshair.setActive(false);
        }
        if (startStopButton != null) {
            startStopButton.setText(R.string.start);
        }
    }

    private void tap(float x, float y) {
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, TapRate.TAP_DURATION_MS))
                .build();
        dispatchGesture(gesture, null, null);
    }

    private void createOverlay() {
        int size = dp(48);
        crosshair = new CrosshairView(this);
        crosshairParams = overlayParams(size, size);
        crosshair.setOnTouchListener(new DragListener(crosshairParams, true));
        windowManager.addView(crosshair, crosshairParams);

        panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(8), dp(8), dp(8), dp(8));
        GradientDrawable background = new GradientDrawable();
        background.setColor(0xDD212121);
        background.setCornerRadius(dp(12));
        panel.setBackground(background);

        TextView handle = new TextView(this);
        handle.setText(R.string.panel_title);
        handle.setTextColor(Color.WHITE);
        handle.setPadding(0, 0, 0, dp(4));
        panel.addView(handle);

        startStopButton = new Button(this);
        startStopButton.setText(R.string.start);
        startStopButton.setOnClickListener(v -> {
            if (running) {
                stopTapping();
            } else {
                startTapping();
            }
        });
        panel.addView(startStopButton);

        Button closeButton = new Button(this);
        closeButton.setText(R.string.close);
        closeButton.setOnClickListener(v -> hideOverlay());
        panel.addView(closeButton);

        counterText = new TextView(this);
        counterText.setTextColor(Color.WHITE);
        counterText.setText(getString(R.string.tap_count, 0L));
        panel.addView(counterText);

        panelParams = overlayParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        panelParams.x = dp(16);
        panelParams.y = dp(120);
        handle.setOnTouchListener(new DragListener(panelParams, false));
        windowManager.addView(panel, panelParams);
    }

    private WindowManager.LayoutParams overlayParams(int width, int height) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                width, height,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    /** Moves an overlay window with the finger. */
    private class DragListener implements View.OnTouchListener {
        private final WindowManager.LayoutParams params;
        private final boolean isTarget;
        private int startX;
        private int startY;
        private float touchX;
        private float touchY;

        DragListener(WindowManager.LayoutParams params, boolean isTarget) {
            this.params = params;
            this.isTarget = isTarget;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            View window = isTarget ? crosshair : panel;
            if (window == null) {
                return false;
            }
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startX = params.x;
                    startY = params.y;
                    touchX = event.getRawX();
                    touchY = event.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    params.x = startX + Math.round(event.getRawX() - touchX);
                    params.y = startY + Math.round(event.getRawY() - touchY);
                    windowManager.updateViewLayout(window, params);
                    return true;
                case MotionEvent.ACTION_UP:
                    if (isTarget) {
                        saveTargetPosition();
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    private void saveTargetPosition() {
        ClickerSettings settings = ClickerSettings.load(this);
        settings.setDevicePoint(this,
                crosshairParams.x + crosshairParams.width / 2f,
                crosshairParams.y + crosshairParams.height / 2f);
        settings.save(this);
    }
}
