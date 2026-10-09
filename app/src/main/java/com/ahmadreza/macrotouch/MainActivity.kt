package com.ahmadreza.macrotouch

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private val bg = Color.rgb(12, 16, 24)
    private val surface = Color.rgb(22, 29, 41)
    private val surfaceAlt = Color.rgb(28, 37, 52)
    private val accent = Color.rgb(102, 168, 255)
    private val purple = Color.rgb(159, 116, 255)
    private val muted = Color.rgb(151, 164, 184)
    private lateinit var status: TextView
    private lateinit var overlayDot: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = 0

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        val scroll = ScrollView(this).apply {
            clipToPadding = false
            isFillViewport = true
            setPadding(0, 0, 0, dp(12))
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(24))
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val mark = TextView(this).apply {
            text = "MT"
            textSize = 17f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            background = rounded(Color.rgb(49, 76, 119), 18)
        }
        header.addView(mark, LinearLayout.LayoutParams(dp(52), dp(52)))
        val titleColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(13), 0, 0, 0)
        }
        titleColumn.addView(text("MacroTouch", 23f, Color.WHITE, true))
        titleColumn.addView(text("استودیوی کنترل و ماکرو", 13f, muted))
        header.addView(titleColumn, LinearLayout.LayoutParams(0, -2, 1f))
        val version = TextView(this).apply {
            text = "BETA  •  0.2"
            textSize = 10f
            setTextColor(accent)
            setPadding(dp(10), dp(7), dp(10), dp(7))
            background = rounded(Color.rgb(25, 43, 66), 20)
        }
        header.addView(version)
        content.addView(header)
        content.addView(space(24))

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(34, 55, 86), Color.rgb(27, 35, 54), Color.rgb(34, 29, 58))
            ).apply { cornerRadius = dp(24).toFloat() }
        }
        hero.addView(text("کنترل‌ها، دقیقاً دست خودته", 20f, Color.WHITE, true))
        hero.addView(space(8))
        hero.addView(text("دکمه‌های شناور را بساز، جابه‌جا کن و اجرای لمس‌ها را از یک نقطه مدیریت کن.", 14f, Color.rgb(205, 216, 233)))
        hero.addView(space(16))
        val statusRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        overlayDot = TextView(this).apply {
            text = "●"
            textSize = 12f
            setTextColor(Color.rgb(255, 190, 90))
        }
        statusRow.addView(overlayDot)
        status = text("  در حال بررسی مجوز شناور…", 12f, Color.rgb(221, 229, 242))
        statusRow.addView(status)
        hero.addView(statusRow)
        content.addView(hero)
        content.addView(space(24))

        section(content, "شروع سریع", "دکمه‌های عملیاتی و دکمهٔ اجرای ماکرو")
        val pair = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        pair.addView(actionCard("＋", "دکمهٔ عملیات", "افزودن نقطهٔ لمس", Color.rgb(39, 91, 151)) {
            if (checkOverlay()) startService(Intent(this, FloatingButtonService::class.java).setAction(FloatingButtonService.ADD_ACTION))
        }, LinearLayout.LayoutParams(0, dp(130), 1f))
        val gap = View(this)
        pair.addView(gap, LinearLayout.LayoutParams(dp(12), 1))
        pair.addView(actionCard("▶", "دکمهٔ اجرا", "اجرای نقاط ساخته‌شده", Color.rgb(89, 60, 143)) {
            if (checkOverlay()) startService(Intent(this, FloatingButtonService::class.java).setAction(FloatingButtonService.ADD_TRIGGER))
        }, LinearLayout.LayoutParams(0, dp(130), 1f))
        content.addView(pair)
        content.addView(space(22))

        section(content, "مدیریت کنترل‌ها", "ظاهر و نمایش دکمه‌های شناور")
        val controls = cardColumn()
        addRow(controls, "◉", "مجوز نمایش شناور", "اجازهٔ نمایش روی بازی و برنامه‌های دیگر", accent) {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            } else {
                status.text = "مجوز نمایش شناور فعال است."
                overlayDot.setTextColor(Color.rgb(88, 214, 158))
            }
        }
        addDivider(controls)
        addRow(controls, "Aa", "اندازهٔ دکمه‌ها", "تغییر اندازه بین کوچک، متوسط و بزرگ", Color.rgb(107, 190, 255)) {
            startService(Intent(this, FloatingButtonService::class.java).setAction(FloatingButtonService.SIZE))
        }
        addDivider(controls)
        addRow(controls, "◐", "شفافیت دکمه‌ها", "تنظیم میزان دیده‌شدن کنترل‌ها", purple) {
            startService(Intent(this, FloatingButtonService::class.java).setAction(FloatingButtonService.OPACITY))
        }
        addDivider(controls)
        addRow(controls, "◌", "پنهان کردن دکمه‌ها", "برای برگشت، از گزینهٔ نمایش دوباره استفاده کن", Color.rgb(229, 174, 97)) {
            startService(Intent(this, FloatingButtonService::class.java).setAction(FloatingButtonService.HIDE))
            status.text = "دکمه‌ها پنهان شدند."
        }
        addDivider(controls)
        addRow(controls, "◎", "نمایش دوباره", "بازگرداندن کنترل‌های پنهان‌شده", Color.rgb(88, 214, 158)) {
            if (checkOverlay()) startService(Intent(this, FloatingButtonService::class.java).setAction(FloatingButtonService.SHOW))
        }
        content.addView(controls)
        content.addView(space(22))

        section(content, "دسترسی و نگهداری", "تنظیم اجرای خودکار و پاک‌سازی")
        val utilities = cardColumn()
        addRow(utilities, "⌁", "فعال‌سازی دسترس‌پذیری", "برای اجرای لمس‌های متوالی لازم است", purple) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        addDivider(utilities)
        addRow(utilities, "×", "حذف همهٔ دکمه‌ها", "پاک‌کردن کنترل‌های شناور ذخیره‌شده", Color.rgb(245, 115, 125)) {
            startService(Intent(this, FloatingButtonService::class.java).setAction(FloatingButtonService.CLEAR))
            status.text = "همهٔ دکمه‌ها حذف شدند؛ شماره‌گذاری از ۱ شروع می‌شود."
        }
        content.addView(utilities)
        content.addView(space(22))

        val note = TextView(this).apply {
            text = "نکته: اجرای لمس‌ها فقط با فعال‌سازی دستی سرویس دسترس‌پذیری اندروید انجام می‌شود. این نسخه هنوز ویرایشگر حرکت و پروفایل‌های مستقل ندارد."
            textSize = 12f
            setTextColor(muted)
            setPadding(dp(4), dp(4), dp(4), dp(12))
            gravity = Gravity.CENTER
        }
        content.addView(note)
        content.addView(text("MACROTOUCH  ·  BUILT FOR CONTROL", 10f, Color.rgb(91, 104, 124), true).apply { gravity = Gravity.CENTER })

        setContentView(root)
        updateStatus()
    }

    private fun section(parent: LinearLayout, title: String, subtitle: String) {
        parent.addView(text(title, 17f, Color.WHITE, true))
        parent.addView(space(4))
        parent.addView(text(subtitle, 12f, muted))
        parent.addView(space(13))
    }

    private fun cardColumn(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(14), dp(4), dp(14), dp(4))
        background = rounded(surface, 20)
    }

    private fun addRow(parent: LinearLayout, iconText: String, titleText: String, subText: String, tint: Int, action: () -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(2), dp(12), dp(2), dp(12))
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        val icon = TextView(this).apply {
            text = iconText
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(tint)
            background = rounded(Color.rgb(35, 44, 60), 13)
        }
        row.addView(icon, LinearLayout.LayoutParams(dp(42), dp(42)))
        val labels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(8), 0)
        }
        labels.addView(text(titleText, 14f, Color.WHITE, true))
        labels.addView(space(4))
        labels.addView(text(subText, 11f, muted))
        row.addView(labels, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(text("›", 25f, muted))
        parent.addView(row)
    }

    private fun actionCard(symbol: String, titleText: String, subText: String, tint: Int, action: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(15), dp(12), dp(15))
            background = rounded(tint, 20)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
            addView(text(symbol, 25f, Color.WHITE, true))
            addView(space(7))
            addView(text(titleText, 15f, Color.WHITE, true))
            addView(space(4))
            addView(text(subText, 11f, Color.rgb(224, 231, 243)))
        }
    }

    private fun addDivider(parent: LinearLayout) {
        parent.addView(View(this).apply { setBackgroundColor(Color.rgb(42, 51, 67)) },
            LinearLayout.LayoutParams(-1, dp(1)).apply { leftMargin = dp(4); rightMargin = dp(4) })
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean = false): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }

    private fun space(heightDp: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(heightDp))
    }

    private fun rounded(color: Int, radiusDp: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radiusDp).toFloat()
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun checkOverlay(): Boolean {
        if (Settings.canDrawOverlays(this)) return true
        status.text = "ابتدا مجوز نمایش شناور را فعال کن."
        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        return false
    }

    private fun updateStatus() {
        if (!::status.isInitialized) return
        val granted = Settings.canDrawOverlays(this)
        status.text = if (granted) "  مجوز نمایش شناور فعال است" else "  مجوز نمایش شناور غیرفعال است"
        overlayDot.setTextColor(if (granted) Color.rgb(88, 214, 158) else Color.rgb(255, 190, 90))
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) updateStatus()
    }
}