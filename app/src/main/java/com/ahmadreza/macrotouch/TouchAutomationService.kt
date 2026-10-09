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

    fun performMacro(points: List<Pair<Float, Float>>, finished: () -> Unit) {
        fun tapAt(index: Int) {
            if (index >= points.size) {
                finished()
                return
            }
            val (x, y) = points[index]
            val path = Path().apply { moveTo(x, y); lineTo(x + 1f, y + 1f) }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 70))
                .build()
            val accepted = dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    handler.postDelayed({ tapAt(index + 1) }, 120)
                }
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    finished()
                }
            }, handler)
            if (!accepted) finished()
        }
        tapAt(0)
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