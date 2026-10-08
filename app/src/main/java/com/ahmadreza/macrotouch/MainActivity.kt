package com.ahmadreza.macrotouch

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
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
            setPadding(24, 34, 24, 24)
            setBackgroundColor(bg)
        }
        root.addView(TextView(this).apply {
            text = "MacroTouch"
            textSize = 30f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        })
        root.addView(TextView(this).apply {
            text = "FLOATING CONTROL LAB  •  v0.1"
            textSize = 12f
            setTextColor(accent)
            setPadding(0, 8, 0, 24)
        })
        val info = TextView(this).apply {
            text = "نسخه آزمایشی\n\nدکمه‌های شناور را روی صفحه اضافه کن، آن‌ها را با کشیدن جابه‌جا کن و برای هرکدام ظاهر و جای جداگانه داشته باش. این نسخه فعلاً کنترل‌های شناور را آزمایش می‌کند و لمس خودکار یا نشانه‌گیری انجام نمی‌دهد."
            textSize = 16f
            setTextColor(Color.rgb(220, 225, 235))
            setPadding(18, 18, 18, 18)
            setBackgroundColor(panel)
        }
        root.addView(info, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 22 })
        status = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 0, 0, 14)
        }
        root.addView(status)
        root.addView(makeButton("۱. اجازه نمایش روی برنامه‌های دیگر", accent) {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            } else updateStatus()
        })
        root.addView(makeButton("۲. افزودن دکمه شناور", Color.rgb(49, 59, 76)) {
            if (Settings.canDrawOverlays(this)) {
                startService(Intent(this, FloatingButtonService::class.java).setAction(FloatingButtonService.ADD))
                status.text = "یک دکمه اضافه شد. می‌توانی آن را روی صفحه نگه داری و جابه‌جا کنی."
            } else {
                status.text = "ابتدا اجازه نمایش روی برنامه‌های دیگر را فعال کن."
            }
        })
        root.addView(makeButton("حذف همه دکمه‌های شناور", Color.rgb(49, 59, 76)) {
            startService(Intent(this, FloatingButtonService::class.java).setAction(FloatingButtonService.CLEAR))
            status.text = "درخواست حذف دکمه‌ها ارسال شد."
        })
        root.addView(TextView(this).apply {
            text = "نکته: برای جابه‌جایی، دکمه را لمس کن و بکش. برای بستن، از داخل همین برنامه «حذف همه» را بزن."
            textSize = 13f
            setTextColor(Color.rgb(160, 170, 188))
            setPadding(0, 24, 0, 0)
        })
        setContentView(root)
        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) updateStatus()
    }

    private fun updateStatus() {
        status.text = if (Settings.canDrawOverlays(this))
            "وضعیت مجوز: فعال"
        else "وضعیت مجوز: هنوز فعال نشده"
    }

    private fun makeButton(label: String, color: Int, action: () -> Unit): Button =
        Button(this).apply {
            text = label
            textSize = 14f
            setTextColor(Color.WHITE)
            setBackgroundColor(color)
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 12 }
        }
}
