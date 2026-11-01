package ir.gahshomar.parsi.notify

import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.service.dreams.DreamService
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import ir.gahshomar.parsi.R
import ir.gahshomar.parsi.core.IranianCalendar
import ir.gahshomar.parsi.core.PersianText

/**
 * صفحهٔ همیشه‌روشنِ گاه‌شمار — تاریخ با حروفِ بسیار درشت.
 *
 * صفحهٔ همیشه‌روشن (AOD) را سامانه می‌کشد و هیچ برنامه‌ای نمی‌تواند روی آن چیزی بنویسد؛
 * تنها چیزی که سامانه آنجا از برنامه‌ها می‌پذیرد نشانِ ریزِ اعلان است — همان که ناخوانا
 * دیده می‌شد. نزدیک‌ترین راهِ ممکن، «محافظ صفحه» (Dream / Screensaver) اندروید است:
 * در تنظیماتِ نمایش، «گاه‌شمار ایرانی» را محافظ صفحه کنید تا هنگام شارژ یا بی‌کاری،
 * تاریخ با حروفِ درشت و بر زمینهٔ سیاه — همانند صفحهٔ همیشه‌روشن — دیده شود.
 */
class DateDreamService : DreamService() {

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var dayText: TextView
    private lateinit var yearText: TextView
    private lateinit var weekdayText: TextView

    private val ticker = object : Runnable {
        override fun run() {
            render()
            handler.postDelayed(this, TICK_MILLIS)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isFullscreen = true
        isInteractive = false

        val root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
        }

        val display = ResourcesCompat.getFont(this, R.font.vazirmatn_bold)
            ?: Typeface.create("sans-serif-black", Typeface.BOLD)

        dayText = TextView(this).apply {
            setTextColor(Color.parseColor("#FFF3D9"))
            typeface = display
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 82f)
            setShadowLayer(10f, 0f, 4f, Color.argb(220, 0, 0, 0))
            includeFontPadding = false
        }
        yearText = TextView(this).apply {
            setTextColor(Color.parseColor("#F2A659"))
            typeface = display
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 46f)
            includeFontPadding = false
        }
        weekdayText = TextView(this).apply {
            setTextColor(Color.parseColor("#CFC6B6"))
            typeface = display
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
            includeFontPadding = false
        }

        column.addView(dayText)
        column.addView(yearText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 10 })
        column.addView(weekdayText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 14 })

        root.addView(
            column,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
        )
        setContentView(root)
        render()
        handler.post(ticker)
    }

    override fun onDreamingStopped() {
        handler.removeCallbacks(ticker)
        super.onDreamingStopped()
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacks(ticker)
        super.onDetachedFromWindow()
    }

    private fun render() {
        val prefs = OverlayStyle.prefs(this)
        val persian = prefs.getBoolean("persian_digits", true)
        val today = IranianCalendar.todayEpochDay()
        val date = IranianCalendar.fromEpochDay(today)
        dayText.text = PersianText.dayMonthLabel(date, persian)
        yearText.text = PersianText.pahlaviYear(date, persian)
        weekdayText.text = IranianCalendar.weekdayName(today)
    }

    companion object {
        private const val TICK_MILLIS = 20_000L
    }
}
