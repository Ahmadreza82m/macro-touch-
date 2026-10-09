package com.ahmadreza.macrotouch

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

class TouchAutomationService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    fun performMacro(
        points: List<Pair<Float, Float>>,
        swipeUp: Boolean,
        distancePx: Float,
        durationMs: Long,
        delayMs: Long,
        finished: () -> Unit
    ) {
        fun performAt(index: Int) {
            if (index >= points.size) {
                finished()
                return
            }
            val (x, y) = points[index]
            val path = Path().apply {
                moveTo(x, y)
                if (swipeUp) lineTo(x, (y - distancePx).coerceAtLeast(1f))
                else lineTo(x + 1f, y + 1f)
            }
            val strokeDuration = if (swipeUp) durationMs.coerceIn(60L, 1500L) else 70L
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, strokeDuration))
                .build()
            val accepted = dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    handler.postDelayed({ performAt(index + 1) }, delayMs.coerceIn(50L, 2000L))
                }
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    finished()
                }
            }, handler)
            if (!accepted) finished()
        }
        performAt(0)
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    companion object {
        @Volatile var instance: TouchAutomationService? = null
            private set
    }
}