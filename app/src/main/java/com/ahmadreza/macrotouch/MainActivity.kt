package com.ahmadreza.macrotouch

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private val bg = Color.rgb(17, 20, 28)
    private val panel = Color.rgb(29, 34, 45)
    private val accent = Color.rgb(93, 167, 255)
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 30, 22, 22)
            setBackgroundColor(bg)
        }
        root.addView(TextView(this).apply {
            text = "MacroTouch"
            textSize = 30f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        })
        root.addView(TextView(this).apply {
            text = "FLOATING CONTROL STUDIO  •  v0.2"
            textSize = 12f
            setTextColor(accent)
            setPadding(0, 8, 0, 20)
        })
        root.addView(TextView(this).apply {
            text = "۱) دکمه‌های عملیات را اضافه کن و روی نقاط دلخواه بکش.\n۲) دکمهٔ بنفش «اجرا» را جداگانه اضافه کن.\n۳) با لمس اجرا، نقاط عملیات به‌ترتیب لمس می‌شوند. برای این قابلیت، سرویس دسترس‌پذیری MacroTouch را خودت فعال کن."
            textSize = 15f
            setTextColor(Color.rgb(220, 225, 235))
            setPadding(16, 16, 16, 16)
            setBackgroundColor(panel)
        }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 18 })
        status = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.rgb(180, 195, 215))
            setPadding(0, 0, 0, 12)
        }
        root.addView(status)
        addAction(root, "اجازهٔ نمایش روی برنامه‌های دیگر", accent) {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            } else status.text = "مجوز نمایش شناور فعال است."
        }
        addAction(root, "＋ افزودن دکمهٔ عملیات", Color.rgb(42, 65, 92)) {
            if (checkOverlay()) startService(Intent(this, FloatingButtonService::class.java).setAction(FloatingButtonService.ADD_ACTION))
        }
        addAction(root, "▶ افزودن دکمهٔ اجرا / محرک", Color.rgb(80, 54, 120)) {
            if (checkOverlay()) startService(Intent(this, FloatingButtonService::class.java).setAction(FloatingButtonService.ADD_TRIGGER))
        }
        addAction(root, "فعال‌سازی اجرای لمس‌ها", Color.rgb(49, 59, 76)) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        addAction(root, "تغییر اندازهٔ دکمه‌ها", Color.rgb(49, 59, 76)) {
            startService(Intent(this, FloatingButtonService::class.java).setAction(FloatingButtonService.SIZE))
        }
        addAction(root, "تغییر شفافیت دکمه‌ها", Color.rgb(49, 59, 76)) {
            startService(Intent(this, FloatingButtonService::class.java).setAction(FloatingButtonService.OPACITY))
        }
        addAction(root, "پنهان کردن دکمه‌های شناور", Color.rgb(49, 59, 76)) {
            startService(Intent(this, FloatingButtonService::class.java).setAction(FloatingButtonService.HIDE))
            status.text = "دکمه‌ها پنهان شدند؛ برنامه را باز کن و نمایش دوباره را بزن."
        }
        addAction(root, "نمایش دوبارهٔ دکمه‌ها", Color.rgb(49, 59, 76)) {
            startService(Intent(this, FloatingButtonService::class.java).setAction(FloatingButtonService.SHOW))
            status.text = "درخواست نمایش دوباره ارسال شد."
        }
        addAction(root, "حذف همهٔ دکمه‌ها", Color.rgb(95, 43, 52)) {
            startService(Intent(this, FloatingButtonService::class.java).setAction(FloatingButtonService.CLEAR))
            status.text = "همهٔ دکمه‌ها حذف شدند."
        }
        root.addView(TextView(this).apply {
            text = "جای دکمه‌ها پس از جابه‌جایی ذخیره می‌شود. اجرای لمس‌ها فقط پس از فعال‌سازی دستی دسترس‌پذیری انجام می‌شود."
            textSize = 12f
            setTextColor(Color.rgb(150, 160, 178))
            setPadding(2, 12, 2, 20)
        })
        setContentView(android.widget.ScrollView(this).apply { addView(root) })
        updateStatus()
    }

    private fun checkOverlay(): Boolean {
        if (Settings.canDrawOverlays(this)) return true
        status.text = "ابتدا اجازه نمایش روی برنامه‌های دیگر را فعال کن."
        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        return false
    }

    private fun updateStatus() {
        if (::status.isInitialized) status.text =
            if (Settings.canDrawOverlays(this)) "وضعیت مجوز شناور: فعال" else "وضعیت مجوز شناور: غیرفعال"
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun addAction(root: LinearLayout, label: String, color: Int, action: () -> Unit) {
        root.addView(Button(this).apply {
            text = label
            textSize = 14f
            isAllCaps = false
            setTextColor(Color.WHITE)
            setBackgroundColor(color)
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 9 })
    }
}