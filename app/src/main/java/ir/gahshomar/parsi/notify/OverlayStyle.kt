package ir.gahshomar.parsi.notify

import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import ir.gahshomar.parsi.R
import ir.gahshomar.parsi.core.IranianCalendar
import ir.gahshomar.parsi.core.PersianText
import kotlin.math.roundToInt

/**
 * ساخت، چیدن و کشیدنِ متنِ تاریخ روی صفحه.
 *
 * دو حالت، دو جای جداگانه که هر کدام به‌یاد می‌مانند:
 *  • [MODE_ACTIVE] — هنگام استفاده از گوشی
 *  • [MODE_LOCK] — هنگامی که صفحه قفل است (همان نمایی که پیش از صفحهٔ همیشه‌روشن دیده می‌شود)
 *
 * متن بی هیچ زمینه و کادری کشیده می‌شود و با انگشت جابه‌جا می‌شود؛ جای تازه در همان
 * حالت ذخیره می‌گردد.
 */
object OverlayStyle {

    const val PREFS = "gahshomar_prefs"
    const val KEY_ENABLED = "overlay_enabled"
    const val KEY_SIZE = "overlay_size"
    const val KEY_SHOW_YEAR = "overlay_show_year"
    const val KEY_SHORT = "overlay_short"
    const val KEY_COLOR = "overlay_color"
    const val KEY_STATUS = "overlay_status"

    /** دو حالتِ جای تاریخ */
    const val MODE_ACTIVE = "active"
    const val MODE_LOCK = "lock"

    const val POS_BAR_START = "barStart"
    const val POS_BAR_CENTER = "barCenter"
    const val POS_BAR_END = "barEnd"
    const val POS_BELOW_START = "belowStart"
    const val POS_BELOW_END = "belowEnd"
    const val POS_CENTER = "center"

    /** جای پیش‌فرضِ هر حالت */
    const val DEFAULT_SPOT_ACTIVE = POS_BAR_START
    const val DEFAULT_SPOT_LOCK = POS_CENTER

    fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** ثبتِ رویداد یا خطا تا در تنظیمات دیده شود */
    fun report(context: Context, message: String) {
        runCatching { prefs(context).edit().putString(KEY_STATUS, message).apply() }
    }

    fun status(context: Context): String =
        prefs(context).getString(KEY_STATUS, "") ?: ""

    // ───────────────────────────────────────── جای هر حالت

    /** حالتِ کنونی دستگاه: قفل است یا در حال استفاده */
    fun mode(context: Context): String = if (isLocked(context)) MODE_LOCK else MODE_ACTIVE

    fun isLocked(context: Context): Boolean = runCatching {
        (context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isKeyguardLocked
    }.getOrDefault(false)

    private fun spotKey(mode: String) = "overlay_spot_$mode"
    private fun dxKey(mode: String) = "overlay_dx_$mode"
    private fun dyKey(mode: String) = "overlay_dy_$mode"

    fun spot(context: Context, mode: String): String = prefs(context).getString(
        spotKey(mode),
        if (mode == MODE_LOCK) DEFAULT_SPOT_LOCK else DEFAULT_SPOT_ACTIVE
    ) ?: DEFAULT_SPOT_ACTIVE

    fun setSpot(context: Context, mode: String, spot: String) {
        prefs(context).edit()
            // با برگزیدنِ جای تازه، جابه‌جاییِ دستی صفر می‌شود
            .putString(spotKey(mode), spot)
            .putInt(dxKey(mode), 0)
            .putInt(dyKey(mode), 0)
            .apply()
    }

    fun offset(context: Context, mode: String): Pair<Int, Int> {
        val p = prefs(context)
        return p.getInt(dxKey(mode), 0) to p.getInt(dyKey(mode), 0)
    }

    fun setOffset(context: Context, mode: String, dxDp: Int, dyDp: Int) {
        prefs(context).edit()
            .putInt(dxKey(mode), dxDp)
            .putInt(dyKey(mode), dyDp)
            .apply()
    }

    fun resetPosition(context: Context, mode: String) {
        prefs(context).edit()
            .remove(spotKey(mode))
            .putInt(dxKey(mode), 0)
            .putInt(dyKey(mode), 0)
            .apply()
    }

    // ───────────────────────────────────────── ساختِ متن

    fun label(context: Context): String {
        val p = prefs(context)
        val persian = p.getBoolean("persian_digits", true)
        val date = IranianCalendar.fromEpochDay(IranianCalendar.todayEpochDay())
        val base = PersianText.statusLabel(date, persian, p.getBoolean(KEY_SHORT, false))
        return if (p.getBoolean(KEY_SHOW_YEAR, false)) {
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
        // فقط سایه، تا روی هر پس‌زمینه‌ای خوانا بماند؛ بی هیچ کادر یا زمینه
        setShadowLayer(7f, 0f, 2f, Color.argb(235, 0, 0, 0))
        includeFontPadding = false
        background = null
        text = label(context)
    }

    /** پارامترهای پنجرهٔ کشیدنی و شفاف */
    fun layoutParams(context: Context, windowType: Int): WindowManager.LayoutParams =
        WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            // NOT_TOUCHABLE نیست: متن باید با انگشت کشیده شود
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

    /** متن، اندازه، رنگ و جای تاریخ را از تنظیمات می‌خواند */
    fun configure(
        context: Context,
        view: TextView,
        params: WindowManager.LayoutParams,
        aboveBar: Boolean
    ) {
        val p = prefs(context)
        val size = p.getInt(KEY_SIZE, 18)
        val mode = mode(context)
        view.text = label(context)
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size.toFloat())
        view.setTextColor(
            runCatching { Color.parseColor(p.getString(KEY_COLOR, "#FFF3D9")) }
                .getOrDefault(Color.WHITE)
        )
        view.setBackgroundColor(Color.TRANSPARENT)
        place(context, view, params, spot(context, mode), offset(context, mode), aboveBar)
        view.visibility = View.VISIBLE
    }

    /** جای متن را از «جایگاه» و «جابه‌جاییِ دستی» می‌چیند */
    fun place(
        context: Context,
        view: TextView,
        params: WindowManager.LayoutParams,
        spot: String,
        offset: Pair<Int, Int>,
        aboveBar: Boolean
    ) {
        val metrics = context.resources.displayMetrics
        val density = metrics.density
        view.measure(
            View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val statusBar = statusBarHeight(context)
        val overBarY = ((statusBar - view.measuredHeight) / 2).coerceAtLeast(0)
        val underBarY = statusBar + (4f * density).toInt()
        val side = (8f * density).toInt()
        val barY = if (aboveBar) overBarY else underBarY

        when (spot) {
            POS_BAR_START -> anchor(params, Gravity.TOP or Gravity.START, side, barY)
            POS_BAR_CENTER -> anchor(params, Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, barY)
            POS_BAR_END -> anchor(params, Gravity.TOP or Gravity.END, side, barY)
            POS_BELOW_START -> anchor(params, Gravity.TOP or Gravity.START, side, underBarY)
            POS_BELOW_END -> anchor(params, Gravity.TOP or Gravity.END, side, underBarY)
            POS_CENTER -> anchor(params, Gravity.CENTER, 0, 0)
            else -> anchor(params, Gravity.TOP or Gravity.START, side, barY)
        }
        params.x += (offset.first * density).roundToInt()
        params.y += (offset.second * density).roundToInt()
    }

    private fun anchor(params: WindowManager.LayoutParams, gravity: Int, x: Int, y: Int) {
        params.gravity = gravity
        params.x = x
        params.y = y
    }

    /**
     * متن را کشیدنی می‌کند. با رهاکردنِ انگشت، جابه‌جایی برای **همان حالت**
     * (در حال استفاده یا صفحهٔ قفل) ذخیره می‌شود.
     */
    fun makeDraggable(
        context: Context,
        view: TextView,
        params: WindowManager.LayoutParams,
        manager: WindowManager
    ) {
        val density = context.resources.displayMetrics.density
        var mode = mode(context)
        var startOffset = offset(context, mode)
        var startX = 0
        var startY = 0
        var downX = 0f
        var downY = 0f
        var dragged = false

        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    // حالت و جابه‌جاییِ پایه را همین‌جا می‌خوانیم تا همیشه تازه باشد
                    mode = mode(context)
                    startOffset = offset(context, mode)
                    startX = params.x
                    startY = params.y
                    downX = event.rawX
                    downY = event.rawY
                    dragged = false
                    view.alpha = 0.75f
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY
                    if (kotlin.math.abs(dx) > 4 || kotlin.math.abs(dy) > 4) dragged = true
                    // گرانشِ راست یعنی افزایشِ x به چپ می‌برد؛ نشانه را برمی‌گردانیم
                    val signX = if (isEndAnchored(params.gravity)) -1 else 1
                    params.x = startX + (dx * signX).roundToInt()
                    params.y = startY + dy.roundToInt()
                    runCatching { manager.updateViewLayout(view, params) }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.alpha = 1f
                    if (dragged) {
                        val signX = if (isEndAnchored(params.gravity)) -1 else 1
                        val dx = (params.x - startX) / density * signX
                        val dy = (params.y - startY) / density
                        setOffset(
                            context,
                            mode,
                            startOffset.first + dx.roundToInt(),
                            startOffset.second + dy.roundToInt()
                        )
                        report(context, "جای تاریخ در حالتِ «${modeLabel(mode)}» ذخیره شد")
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun isEndAnchored(gravity: Int): Boolean {
        val horizontal = gravity and Gravity.HORIZONTAL_GRAVITY_MASK
        return horizontal == Gravity.END || horizontal == Gravity.RIGHT
    }

    fun modeLabel(mode: String): String =
        if (mode == MODE_LOCK) "صفحهٔ قفل / همیشه‌روشن" else "در حال استفاده"

    fun statusBarHeight(context: Context): Int {
        val id = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) {
            context.resources.getDimensionPixelSize(id)
        } else {
            (24f * context.resources.displayMetrics.density).toInt()
        }
    }
}
