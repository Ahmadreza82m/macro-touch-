package com.ahmadreza.macrotouch

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import kotlin.math.abs

class FloatingButtonService : Service() {
    private lateinit var wm: WindowManager
    private val buttons = mutableListOf<Pair<View, WindowManager.LayoutParams>>()
    private var counter = 0

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ADD -> addButton()
            CLEAR -> clearButtons()
        }
        if (buttons.isEmpty()) stopSelf()
        return START_NOT_STICKY
    }

    private fun addButton() {
        counter++
        val size = (58 * resources.displayMetrics.density).toInt()
        val label = TextView(this).apply {
            text = counter.toString()
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(54, 118, 210))
                setStroke((2 * resources.displayMetrics.density).toInt(), Color.rgb(170, 210, 255))
            }
            elevation = 10f
            contentDescription = "دکمه شناور $counter؛ برای جابه‌جایی لمس و بکش"
        }
        val params = WindowManager.LayoutParams(
            size, size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40 + (counter - 1) * 24
            y = 180 + (counter - 1) * 24
        }
        attachDrag(label, params)
        try {
            wm.addView(label, params)
            buttons.add(label to params)
        } catch (_: Exception) {
            stopSelf()
        }
    }

    private fun attachDrag(view: View, params: WindowManager.LayoutParams) {
        var startX = 0
        var startY = 0
        var downX = 0f
        var downY = 0f
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = params.x
                    startY = params.y
                    downX = event.rawX
                    downY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = startX + (event.rawX - downX).toInt()
                    params.y = startY + (event.rawY - downY).toInt()
                    try { wm.updateViewLayout(v, params) } catch (_: Exception) {}
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val moved = abs(event.rawX - downX) > 8 || abs(event.rawY - downY) > 8
                    if (!moved) {
                        (v as? TextView)?.text = "✓"
                        v.postDelayed({ (v as? TextView)?.text = buttons.indexOfFirst { it.first === v }.let { if (it >= 0) (it + 1).toString() else "•" } }, 500)
                    }
                    true
                }
                else -> true
            }
        }
    }

    private fun clearButtons() {
        buttons.forEach { (view, _) -> try { wm.removeView(view) } catch (_: Exception) {} }
        buttons.clear()
    }

    override fun onDestroy() {
        clearButtons()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ADD = "com.ahmadreza.macrotouch.ADD"
        const val CLEAR = "com.ahmadreza.macrotouch.CLEAR"
    }
}
