package ir.gahshomar.parsi.notify

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import ir.gahshomar.parsi.R
import ir.gahshomar.parsi.core.IranianCalendar
import ir.gahshomar.parsi.core.PersianText

/**
 * ساخت و چیدنِ متنِ تاریخ روی صفحه — یک‌بار نوشته، دو راهِ نمایش:
 *  • پنجرهٔ سرویس دسترس‌پذیری (بالای نوار وضعیت و بر صفحهٔ قفل)
 *  • پنجرهٔ «روی برنامه‌ها» با اجازهٔ `SYSTEM_ALERT_WINDOW` (بی هیچ سرویس ویژه‌ای)
 */
object OverlayStyle {

    const val PREFS = "gahshomar_prefs"
    const val KEY_ENABLED = "overlay_enabled"
    const val KEY_POSITION = "overlay_position"
    const val KEY_SIZE = "overlay_size"
    const val KEY_LOCK_ONLY = "overlay_lock_only"
    const val KEY_SHOW_YEAR = "overlay_show_year"
    const val KEY_SHORT = "overlay_short"
    const val KEY_PLATE = "overlay_plate"
    const val KEY_COLOR = "overlay_color"

    /** آخرین رویدادِ پنجره، برای نمایشِ عیب‌یابی در تنظیمات */
    const val KEY_STATUS = "overlay_status"

    const val POS_BAR_START = "barStart"
    const val POS_BAR_CENTER = "barCenter"
    const val POS_BAR_END = "barEnd"
    const val POS_BELOW_START = "belowStart"
    const val POS_BELOW_END = "belowEnd"
    const val POS_CENTER = "center"
    const val POS_BOTTOM = "bottom"

    fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** ثبتِ رویداد/خطای پنجره تا کاربر (و ما) بدانیم کجا ایستاده است */
    fun report(context: Context, message: String) {
        runCatching { prefs(context).edit().putString(KEY_STATUS, message).apply() }
    }

    fun status(context: Context): String =
        prefs(context).getString(KEY_STATUS, "") ?: ""

    /** متنِ تاریخ بر پایهٔ تنظیمات کاربر */
    fun label(context: Context): String {
        val prefs = prefs(context)
        val persian = prefs.getBoolean("persian_digits", true)
        val date = IranianCalendar.fromEpochDay(IranianCalendar.todayEpochDay())
        val base = PersianText.statusLabel(date, persian, prefs.getBoolean(KEY_SHORT, false))
        return if (prefs.getBoolean(KEY_SHOW_YEAR, false)) {
            "$base • ${PersianText.pahlaviYear(date, persian)}"
        } else {
            base
        }
    }

    fun buildView(context: Context): TextView = TextView(context).apply {
        setTextColor(Color.WHITE)
        typeface = runCatching {
            ResourcesCompat.getFont(context, R.font.vazirmatn_bold)
        }.getOrNull() ?: Typeface.create("sans-serif-black", Typeface.BOLD)
        setShadowLayer(8f, 0f, 3f, Color.argb(230, 0, 0, 0))
        includeFontPadding = false
        text = label(context)
    }

    /**
     * متن، اندازه، رنگ، زمینه و جای تاریخ را از تنظیمات می‌خواند.
     *
     * @param aboveBar اگر پنجره بالای نوار وضعیت می‌نشیند (سرویس دسترس‌پذیری)، متن
     *  بر میانهٔ خودِ نوار مرکزچین می‌شود؛ وگرنه کمی زیر نوار می‌آید تا دیده شود.
     */
    fun configure(
        context: Context,
        view: TextView,
        params: WindowManager.LayoutParams,
        aboveBar: Boolean
    ) {
        val prefs = prefs(context)
        val size = prefs.getInt(KEY_SIZE, 36)
        view.text = label(context)
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size.toFloat())
        view.setTextColor(
            runCatching { Color.parseColor(prefs.getString(KEY_COLOR, "#FFF3D9")) }
                .getOrDefault(Color.WHITE)
        )
        val scaled = context.resources.displayMetrics.scaledDensity
        view.background = if (prefs.getBoolean(KEY_PLATE, true)) {
            GradientDrawable().apply {
                cornerRadius = size * 0.55f * scaled
                setColor(Color.argb(130, 0, 0, 0))
                setStroke(2, Color.argb(90, 255, 255, 255))
            }
        } else {
            null
        }
        val padX = (size * 0.34f * scaled).toInt()
        val padY = (size * 0.06f * scaled).toInt()
        view.setPadding(padX, padY, padX, padY)

        val metrics = context.resources.displayMetrics
        val statusBar = statusBarHeight(context)
        // بلندیِ واقعی متن، تا مرکزچینی درست باشد و متن از لبهٔ بالا بیرون نزند
        view.measure(
            View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val overBarY = ((statusBar - view.measuredHeight) / 2).coerceAtLeast(0)
        val underBarY = statusBar + (4f * metrics.density).toInt()
        val barY = if (aboveBar) overBarY else underBarY
        val sideX = (8f * metrics.density).toInt()

        when (prefs.getString(KEY_POSITION, POS_BAR_END)) {
            POS_BAR_START -> set(params, Gravity.TOP or Gravity.START, sideX, barY)
            POS_BAR_CENTER -> set(params, Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, barY)
            POS_BAR_END -> set(params, Gravity.TOP or Gravity.END, sideX, barY)
            POS_BELOW_START -> set(params, Gravity.TOP or Gravity.START, sideX, underBarY)
            POS_BELOW_END -> set(params, Gravity.TOP or Gravity.END, sideX, underBarY)
            POS_CENTER -> set(params, Gravity.CENTER, 0, 0)
            POS_BOTTOM -> set(
                params,
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
                0,
                (56f * metrics.density).toInt()
            )
            else -> set(params, Gravity.TOP or Gravity.END, sideX, barY)
        }
    }

    private fun set(params: WindowManager.LayoutParams, gravity: Int, x: Int, y: Int) {
        params.gravity = gravity
        params.x = x
        params.y = y
    }

    fun statusBarHeight(context: Context): Int {
        val id = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) {
            context.resources.getDimensionPixelSize(id)
        } else {
            (24f * context.resources.displayMetrics.density).toInt()
        }
    }
}
