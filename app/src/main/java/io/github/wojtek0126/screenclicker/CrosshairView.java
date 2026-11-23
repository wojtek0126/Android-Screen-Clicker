package io.github.wojtek0126.screenclicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/** Round target marker; its center is the tap point. */
public class CrosshairView extends View {

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean active;

    public CrosshairView(Context context) {
        super(context);
        fill.setStyle(Paint.Style.FILL);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(3 * getResources().getDisplayMetrics().density);
        stroke.setColor(Color.WHITE);
        setActive(false);
    }

    /** Red while tapping, blue while idle. */
    public void setActive(boolean active) {
        this.active = active;
        fill.setColor(active ? 0x88E53935 : 0x881E88E5);
        invalidate();
    }

    public boolean isActive() {
        return active;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float r = Math.min(cx, cy) - stroke.getStrokeWidth();
        canvas.drawCircle(cx, cy, r, fill);
        canvas.drawCircle(cx, cy, r, stroke);
        canvas.drawLine(cx - r, cy, cx + r, cy, stroke);
        canvas.drawLine(cx, cy - r, cx, cy + r, stroke);
    }
}
