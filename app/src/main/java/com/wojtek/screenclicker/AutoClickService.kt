package com.wojtek.screenclicker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.TextView

/**
 * Accessibility service that shows a draggable crosshair overlay and
 * repeatedly taps at the crosshair position using dispatchGesture().
 */
class AutoClickService : AccessibilityService() {

    companion object {
        const val PREFS = "clicker_prefs"
        const val KEY_INTERVAL = "interval_ms"
        const val MIN_INTERVAL_MS = 50L
        const val DEFAULT_INTERVAL_MS = 1000L

        var instance: AutoClickService? = null
            private set
    }

    private lateinit var windowManager: WindowManager
    private var crosshair: View? = null
    private var crosshairParams: WindowManager.LayoutParams? = null
    private var panel: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    private val tick = object : Runnable {
        override fun run() {
            if (!running) return
            tapAtCrosshair()
            handler.postDelayed(this, intervalMs())
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        hideOverlay()
        instance = null
        super.onDestroy()
    }

    fun isOverlayShown(): Boolean = crosshair != null

    fun toggleOverlay() {
        if (isOverlayShown()) hideOverlay() else showOverlay()
    }

    @SuppressLint("InflateParams")
    fun showOverlay() {
        if (crosshair != null) return
        val inflater = LayoutInflater.from(this)

        // Draggable crosshair marking the tap point
        val target = inflater.inflate(R.layout.overlay_crosshair, null)
        val targetParams = newOverlayParams().apply {
            x = resources.displayMetrics.widthPixels / 2 - 60
            y = resources.displayMetrics.heightPixels / 2 - 60
        }
        makeDraggable(target, targetParams)
        windowManager.addView(target, targetParams)
        crosshair = target
        crosshairParams = targetParams

        // Floating control panel with start/stop and close buttons
        val controls = inflater.inflate(R.layout.overlay_controls, null)
        val controlsParams = newOverlayParams().apply {
            x = 24
            y = 150
        }
        makeDraggable(controls.findViewById(R.id.dragHandle), controlsParams, controls)
        controls.findViewById<Button>(R.id.btnStartStop).setOnClickListener {
            if (running) stopClicking() else startClicking()
        }
        controls.findViewById<Button>(R.id.btnClose).setOnClickListener {
            hideOverlay()
        }
        windowManager.addView(controls, controlsParams)
        panel = controls
        updatePanel()
    }

    fun hideOverlay() {
        stopClicking()
        crosshair?.let { runCatching { windowManager.removeView(it) } }
        panel?.let { runCatching { windowManager.removeView(it) } }
        crosshair = null
        crosshairParams = null
        panel = null
    }

    fun startClicking() {
        if (running || crosshair == null) return
        running = true
        // Let injected taps pass through the crosshair window
        crosshairParams?.let {
            it.flags = it.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            crosshair?.alpha = 0.5f
            runCatching { windowManager.updateViewLayout(crosshair, it) }
        }
        handler.post(tick)
        updatePanel()
    }

    fun stopClicking() {
        if (!running) return
        running = false
        handler.removeCallbacks(tick)
        crosshairParams?.let {
            it.flags = it.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            crosshair?.alpha = 1f
            if (crosshair != null) runCatching { windowManager.updateViewLayout(crosshair, it) }
        }
        updatePanel()
    }

    fun refreshPanel() = updatePanel()

    private fun tapAtCrosshair() {
        val view = crosshair ?: return
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0] + view.width / 2f
        val y = location[1] + view.height / 2f
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 20))
            .build()
        dispatchGesture(gesture, null, null)
    }

    private fun intervalMs(): Long =
        getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_INTERVAL, DEFAULT_INTERVAL_MS)
            .coerceAtLeast(MIN_INTERVAL_MS)

    private fun newOverlayParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun makeDraggable(
        handle: View,
        params: WindowManager.LayoutParams,
        windowView: View = handle
    ) {
        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f
        handle.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = params.x
                    startY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = startX + (event.rawX - touchX).toInt()
                    params.y = startY + (event.rawY - touchY).toInt()
                    runCatching { windowManager.updateViewLayout(windowView, params) }
                    true
                }
                else -> true
            }
        }
    }

    private fun updatePanel() {
        val view = panel ?: return
        view.findViewById<Button>(R.id.btnStartStop)
            .setText(if (running) R.string.stop else R.string.start)
        view.findViewById<TextView>(R.id.tvInterval).text =
            getString(R.string.interval_format, intervalMs())
    }
}
