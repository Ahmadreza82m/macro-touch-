package com.ahmadreza.macrotouch

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.widget.TextView
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import kotlin.math.abs

class FloatingButtonService : Service() {
    private data class Item(val id: Int, val trigger: Boolean, val view: TextView, val params: WindowManager.LayoutParams)
    private lateinit var wm: WindowManager
    private val items = mutableListOf<Item>()
    private val prefs by lazy { getSharedPreferences("MacroTouch", MODE_PRIVATE) }
    private var visible = true

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val ids = prefs.getString("ids", "").orEmpty().split(",").mapNotNull { it.toIntOrNull() }
        ids.forEach { id -> createItem(id, prefs.getBoolean("trigger_$id", false)) }
        visible = prefs.getBoolean("visible", true)
        if (!visible) hideViews()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ADD_ACTION -> addButton(false)
            ADD_TRIGGER -> addButton(true)
            CLEAR -> clearButtons()
            HIDE -> { visible = false; prefs.edit().putBoolean("visible", false).apply(); hideViews() }
            SHOW -> { visible = true; prefs.edit().putBoolean("visible", true).apply(); showViews() }
            SIZE -> cycleSize()
            OPACITY -> cycleOpacity()
        }
        return START_NOT_STICKY
    }

    private fun addButton(trigger: Boolean) {
        val id = prefs.getInt("next_id", 1)
        prefs.edit().putInt("next_id", id + 1).putBoolean("trigger_$id", trigger)
            .putString("ids", (items.map { it.id } + id).joinToString(",")).apply()
        createItem(id, trigger)
    }

    private fun createItem(id: Int, trigger: Boolean) {
        val density = resources.displayMetrics.density
        val size = prefs.getInt("size_dp", 58)
        val opacity = prefs.getFloat("opacity", 0.90f)
        val label = TextView(this).apply {
            text = if (trigger) "▶" else id.toString()
            textSize = if (trigger) 19f else 17f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            alpha = opacity
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(if (trigger) Color.rgb(112, 72, 170) else Color.rgb(43, 112, 198))
                setStroke((2 * density).toInt().coerceAtLeast(1), Color.rgb(180, 210, 245))
            }
            elevation = 10f
        }
        val params = WindowManager.LayoutParams(
            (size * density).toInt(), (size * density).toInt(),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = prefs.getInt("x_$id", 40 + (id - 1) * 20)
            y = prefs.getInt("y_$id", 180 + (id - 1) * 20)
        }
        val item = Item(id, trigger, label, params)
        attachDrag(item)
        items.add(item)
        if (visible) addView(item)
    }

    private fun addView(item: Item) {
        try { if (item.view.parent == null) wm.addView(item.view, item.params) } catch (_: Exception) { }
    }

    private fun attachDrag(item: Item) {
        var startX = 0
        var startY = 0
        var downX = 0f
        var downY = 0f
        item.view.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = item.params.x; startY = item.params.y
                    downX = event.rawX; downY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    item.params.x = startX + (event.rawX - downX).toInt()
                    item.params.y = startY + (event.rawY - downY).toInt()
                    try { wm.updateViewLayout(view, item.params) } catch (_: Exception) { }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val moved = abs(event.rawX - downX) > 8 || abs(event.rawY - downY) > 8
                    prefs.edit().putInt("x_${item.id}", item.params.x).putInt("y_${item.id}", item.params.y).apply()
                    if (!moved) {
                        if (item.trigger) runMacro() else {
                            val old = item.view.text
                            item.view.text = "•"
                            item.view.postDelayed({ if (items.any { it.id == item.id }) item.view.text = old }, 250)
                        }
                    }
                    true
                }
                else -> true
            }
        }
    }

    private fun runMacro() {
        val points = items.filter { !it.trigger }.map {
            Pair(it.params.x + it.params.width / 2f, it.params.y + it.params.height / 2f)
        }
        if (points.isEmpty()) {
            Toast.makeText(this, "اول دکمه‌های عملیات را اضافه کن.", Toast.LENGTH_SHORT).show()
            return
        }
        val automation = TouchAutomationService.instance
        if (automation == null) {
            Toast.makeText(this, "در تنظیمات دسترس‌پذیری، MacroTouch را فعال کن.", Toast.LENGTH_LONG).show()
            return
        }
        hideViews()
        automation.performMacro(points) { if (visible) showViews() }
    }

    private fun hideViews() {
        items.forEach { try { if (it.view.parent != null) wm.removeView(it.view) } catch (_: Exception) { } }
    }

    private fun showViews() { items.forEach { addView(it) } }

    private fun cycleSize() {
        val next = when (prefs.getInt("size_dp", 58)) { 58 -> 68; 68 -> 48; else -> 58 }
        prefs.edit().putInt("size_dp", next).apply()
        items.forEach {
            it.params.width = (next * resources.displayMetrics.density).toInt()
            it.params.height = it.params.width
            try { if (it.view.parent != null) wm.updateViewLayout(it.view, it.params) } catch (_: Exception) { }
        }
        Toast.makeText(this, "اندازه: $next", Toast.LENGTH_SHORT).show()
    }

    private fun cycleOpacity() {
        val current = prefs.getFloat("opacity", 0.90f)
        val next = when { current > 0.85f -> 0.65f; current > 0.55f -> 0.40f; else -> 0.90f }
        prefs.edit().putFloat("opacity", next).apply()
        items.forEach { it.view.alpha = next }
        Toast.makeText(this, "شفافیت: ${(next * 100).toInt()}٪", Toast.LENGTH_SHORT).show()
    }

    private fun clearButtons() {
        hideViews()
        items.clear()
        prefs.edit().putString("ids", "").putBoolean("visible", true).apply()
        visible = true
    }

    override fun onDestroy() { hideViews(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ADD_ACTION = "com.ahmadreza.macrotouch.ADD_ACTION"
        const val ADD_TRIGGER = "com.ahmadreza.macrotouch.ADD_TRIGGER"
        const val CLEAR = "com.ahmadreza.macrotouch.CLEAR"
        const val HIDE = "com.ahmadreza.macrotouch.HIDE"
        const val SHOW = "com.ahmadreza.macrotouch.SHOW"
        const val SIZE = "com.ahmadreza.macrotouch.SIZE"
        const val OPACITY = "com.ahmadreza.macrotouch.OPACITY"
    }
}