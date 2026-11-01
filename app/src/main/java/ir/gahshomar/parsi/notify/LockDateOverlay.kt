package ir.gahshomar.parsi.notify

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import ir.gahshomar.parsi.R
import ir.gahshomar.parsi.core.IranianCalendar
import ir.gahshomar.parsi.core.PersianText

/**
 * تاریخ، بزرگ و خوانا، روی نوار وضعیت و بر صفحهٔ قفل — **بی هیچ اعلانی**.
 *
 * چرا اعلان به کار نمی‌آید: اندروید برای نوشتنِ متن در نوار وضعیت تنها «نشانِ کوچکِ»
 * اعلان را می‌پذیرد و آن نشان جای متنِ درشت ندارد؛ بر صفحهٔ همیشه‌روشن (AOD) هم
 * سامانه فقط همان نشانِ ریز را می‌کشد — همان چیزی که ریز و ناخوانا دیده می‌شد.
 *
 * چرا این پنجره به کار می‌آید: پنجرهٔ «روی برنامه‌ها» (TYPE_APPLICATION_OVERLAY)
 * هنگام قفل پنهان می‌شود، اما پنجرهٔ سرویس دسترس‌پذیری
 * (TYPE_ACCESSIBILITY_OVERLAY) پنهان نمی‌شود و از نوار وضعیت هم بالاتر می‌نشیند.
 * پس متن را با هر اندازه‌ای که بخواهید می‌کشیم: از ۲۰ تا ۷۲ sp.
 *
 * این سرویس هیچ محتوایی از صفحه نمی‌خواند، هیچ اطلاعاتی جایی نمی‌فرستد و تنها
 * یک متن می‌کشد.
 */
class LockDateOverlay : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var view: TextView? = null
    private var params: WindowManager.LayoutParams? = null

    private val ticker = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, TICK_MILLIS)
        }
    }

    private val prefsListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> refresh() }

    /** با روشن‌شدن صفحه، بازشدن قفل و تغییر تاریخ/ساعت بی‌درنگ تازه می‌شود */
    private val clockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON, Intent.ACTION_SCREEN_OFF,
                Intent.ACTION_USER_PRESENT, Intent.ACTION_DATE_CHANGED,
                Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED,
                Intent.ACTION_BOOT_COMPLETED -> refresh()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        attachedNow = true
        refresh()
        handler.post(ticker)
        runCatching { prefs().registerOnSharedPreferenceChangeListener(prefsListener) }
        runCatching {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
                addAction(Intent.ACTION_DATE_CHANGED)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            }
            registerReceiver(clockReceiver, filter)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        attachedNow = false
        handler.removeCallbacks(ticker)
        runCatching { prefs().unregisterOnSharedPreferenceChangeListener(prefsListener) }
        runCatching { unregisterReceiver(clockReceiver) }
        detach()
        super.onDestroy()
    }

    // ───────────────────────────────────────── پنجره

    private fun prefs() = getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun attach() {
        if (view != null) return
        val text = TextView(this).apply {
            setTextColor(Color.WHITE)
            typeface = runCatching {
                ResourcesCompat.getFont(this@LockDateOverlay, R.font.vazirmatn_bold)
            }.getOrNull() ?: Typeface.create("sans-serif-black", Typeface.BOLD)
            setShadowLayer(8f, 0f, 3f, Color.argb(230, 0, 0, 0))
            includeFontPadding = false
        }
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            title = "گاه‌شمار"
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }
        view = text
        params = layoutParams
        runCatching { windowManager.addView(text, layoutParams) }
        refresh()
    }

    private fun detach() {
        val current = view ?: return
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        runCatching { windowManager?.removeView(current) }
        view = null
        params = null
    }

    /** متن، اندازه، رنگ و جای تاریخ را از تنظیمات می‌خواند و پنجره را می‌چیند */
    private fun refresh() {
        val prefs = prefs()
        if (!prefs.getBoolean(KEY_ENABLED, true)) {
            detach()
            return
        }
        if (view == null) {
            attach()
            return
        }
        val text = view ?: return
        val layoutParams = params ?: return

        val persian = prefs.getBoolean("persian_digits", true)
        val date = IranianCalendar.fromEpochDay(IranianCalendar.todayEpochDay())
        val short = prefs.getBoolean(KEY_SHORT, false)
        val label = PersianText.statusLabel(date, persian, short)
        val year = PersianText.pahlaviYear(date, persian)
        text.text = if (prefs.getBoolean(KEY_SHOW_YEAR, false)) "$label • $year" else label

        val size = prefs.getInt(KEY_SIZE, 36)
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, size.toFloat())
        text.setTextColor(
            runCatching { Color.parseColor(prefs.getString(KEY_COLOR, "#FFF3D9")) }
                .getOrDefault(Color.WHITE)
        )
        text.background = if (prefs.getBoolean(KEY_PLATE, true)) {
            GradientDrawable().apply {
                cornerRadius = (size * 0.55f) * resources.displayMetrics.scaledDensity
                setColor(Color.argb(120, 0, 0, 0))
                setStroke(2, Color.argb(90, 255, 255, 255))
            }
        } else {
            null
        }
        val padX = (size * 0.34f * resources.displayMetrics.scaledDensity).toInt()
        val padY = (size * 0.06f * resources.displayMetrics.scaledDensity).toInt()
        text.setPadding(padX, padY, padX, padY)

        val metrics = resources.displayMetrics
        val statusBar = statusBarHeight()
        val belowBar = statusBar + (4f * metrics.density).toInt()

        // بلندیِ واقعی متن را می‌سنجیم تا متن دقیقاً بر میانهٔ نوار وضعیت بنشیند،
        // هر اندازه‌ای که داشته باشد؛ و هرگز از لبهٔ بالای صفحه بیرون نزند.
        text.measure(
            View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val onBarY = ((statusBar - text.measuredHeight) / 2).coerceAtLeast(0)
        val sideX = (8f * metrics.density).toInt()
        when (prefs.getString(KEY_POSITION, POS_BAR_END)) {
            POS_BAR_START -> setSpot(layoutParams, Gravity.TOP or Gravity.START, sideX, onBarY)
            POS_BAR_CENTER -> setSpot(layoutParams, Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, onBarY)
            POS_BAR_END -> setSpot(layoutParams, Gravity.TOP or Gravity.END, sideX, onBarY)
            POS_BELOW_START -> setSpot(layoutParams, Gravity.TOP or Gravity.START, sideX, belowBar)
            POS_BELOW_END -> setSpot(layoutParams, Gravity.TOP or Gravity.END, sideX, belowBar)
            POS_CENTER -> setSpot(layoutParams, Gravity.CENTER, 0, 0)
            POS_BOTTOM -> setSpot(
                layoutParams,
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
                0,
                (48f * metrics.density).toInt()
            )
            else -> setSpot(layoutParams, Gravity.TOP or Gravity.END, sideX, onBarY)
        }

        val lockOnly = prefs.getBoolean(KEY_LOCK_ONLY, false)
        val keyguard = runCatching {
            (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isKeyguardLocked
        }.getOrDefault(false)
        text.visibility = if (lockOnly && !keyguard) View.GONE else View.VISIBLE

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        runCatching { windowManager?.updateViewLayout(text, layoutParams) }
    }

    private fun setSpot(
        params: WindowManager.LayoutParams,
        gravity: Int,
        x: Int,
        y: Int
    ) {
        params.gravity = gravity
        params.x = x
        params.y = y
    }

    private fun statusBarHeight(): Int {
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) {
            resources.getDimensionPixelSize(id)
        } else {
            (24f * resources.displayMetrics.density).toInt()
        }
    }

    companion object {
        const val PREFS = "gahshomar_prefs"
        const val KEY_ENABLED = "overlay_enabled"
        const val KEY_POSITION = "overlay_position"
        const val KEY_SIZE = "overlay_size"
        const val KEY_LOCK_ONLY = "overlay_lock_only"
        const val KEY_SHOW_YEAR = "overlay_show_year"
        const val KEY_SHORT = "overlay_short"
        const val KEY_PLATE = "overlay_plate"
        const val KEY_COLOR = "overlay_color"

        const val POS_BAR_START = "barStart"
        const val POS_BAR_CENTER = "barCenter"
        const val POS_BAR_END = "barEnd"
        const val POS_BELOW_START = "belowStart"
        const val POS_BELOW_END = "belowEnd"
        const val POS_CENTER = "center"
        const val POS_BOTTOM = "bottom"

        private const val TICK_MILLIS = 20_000L

        /** آیا پنجره همین حالا روی صفحه ایستاده است؟ */
        @Volatile
        var attachedNow: Boolean = false
            internal set

        /** آیا سرویس دسترس‌پذیری روشن است؟ */
        fun isEnabled(context: Context): Boolean {
            val expected = ComponentName(context, LockDateOverlay::class.java).flattenToString()
            val enabled = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val splitter = TextUtils.SimpleStringSplitter(':')
            splitter.setString(enabled)
            for (entry in splitter) {
                if (entry.equals(expected, ignoreCase = true)) return true
            }
            return false
        }
    }
}
