package ir.gahshomar.parsi.notify

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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
 * تاریخِ همیشه‌در‌دسترس — **بی هیچ اعلانی**.
 *
 * اندروید ۸ به بالا پنجرهٔ «روی برنامه‌ها» (TYPE_APPLICATION_OVERLAY) را هنگام قفل
 * پنهان می‌کند، اما پنجرهٔ سرویس دسترس‌پذیری (TYPE_ACCESSIBILITY_OVERLAY) پنهان
 * نمی‌شود و از نوار وضعیت هم بالاتر می‌نشیند. پس همین پنجره را برای نشان‌دادن تاریخ
 * به کار می‌بریم: متنی خوانا، با اندازه و جای دلخواه، بر نوار وضعیت و بر صفحهٔ قفل،
 * و برگهٔ اعلان‌ها کاملاً پاک می‌ماند.
 *
 * این سرویس هیچ محتوایی از صفحه نمی‌خواند؛ تنها یک پنجرهٔ متنی می‌کشد.
 */
class LockDateOverlay : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var view: TextView? = null
    private var params: WindowManager.LayoutParams? = null
    private var attached = false

    private val ticker = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, TICK_MILLIS)
        }
    }

    private val prefsListener =
        android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> refresh() }

    /** با روشن‌شدن صفحه، بازشدن قفل، تغییر تاریخ و تغییر ساعت بی‌درنگ تازه می‌شود */
    private val clockReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF, Intent.ACTION_SCREEN_ON,
                Intent.ACTION_USER_PRESENT, Intent.ACTION_DATE_CHANGED,
                Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED,
                Intent.ACTION_BOOT_COMPLETED -> refresh()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        attach()
        handler.post(ticker)
        runCatching {
            prefs().registerOnSharedPreferenceChangeListener(prefsListener)
        }
        runCatching {
            val filter = android.content.IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
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
        handler.removeCallbacks(ticker)
        runCatching { prefs().unregisterOnSharedPreferenceChangeListener(prefsListener) }
        runCatching { unregisterReceiver(clockReceiver) }
        detach()
        super.onDestroy()
    }

    // ───────────────────────────────────────── پنجره

    private fun prefs() = getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun attach() {
        if (attached) return
        val text = TextView(this).apply {
            setTextColor(Color.WHITE)
            typeface = runCatching {
                ResourcesCompat.getFont(this@LockDateOverlay, R.font.vazirmatn_semibold)
            }.getOrNull() ?: Typeface.create("sans-serif-medium", Typeface.BOLD)
            setShadowLayer(6f, 0f, 2f, Color.argb(200, 0, 0, 0))
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
        )
        view = text
        params = layoutParams
        runCatching { windowManager.addView(text, layoutParams) }.onSuccess { attached = true }
        refresh()
    }

    private fun detach() {
        val current = view ?: return
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        runCatching { windowManager?.removeView(current) }
        view = null
        attached = false
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
        val params = params ?: return

        val persian = prefs.getBoolean("persian_digits", true)
        val date = IranianCalendar.fromEpochDay(IranianCalendar.todayEpochDay())
        val short = prefs.getBoolean(KEY_SHORT, false)
        val label = PersianText.statusLabel(date, persian, short)
        val year = PersianText.pahlaviYear(date, persian)
        text.text = if (prefs.getBoolean(KEY_SHOW_YEAR, false)) "$label • $year" else label
        text.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            prefs.getInt(KEY_SIZE, 22).toFloat()
        )
        text.setTextColor(
            runCatching { Color.parseColor(prefs.getString(KEY_COLOR, "#FFF3D9")) }
                .getOrDefault(Color.WHITE)
        )
        // پس‌زمینهٔ نیم‌شفاف تا روی هر تصویری خوانا بماند
        text.background = GradientDrawable().apply {
            cornerRadius = 28f
            setColor(Color.argb(120, 0, 0, 0))
            setStroke(2, Color.argb(90, 255, 255, 255))
        }
        text.setPadding(22, 6, 22, 6)

        val metrics = resources.displayMetrics
        val statusBar = statusBarHeight()
        val yOffset = statusBar + (6f * metrics.density).toInt()
        when (prefs.getString(KEY_POSITION, POS_BAR_END)) {
            POS_BAR_START -> {
                params.gravity = Gravity.TOP or Gravity.START
                params.x = (10f * metrics.density).toInt()
                params.y = -(3f * metrics.density).toInt()
            }
            POS_BAR_CENTER -> {
                params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                params.x = 0
                params.y = -(3f * metrics.density).toInt()
            }
            POS_BAR_END -> {
                params.gravity = Gravity.TOP or Gravity.END
                params.x = (10f * metrics.density).toInt()
                params.y = -(3f * metrics.density).toInt()
            }
            POS_BELOW_END -> {
                params.gravity = Gravity.TOP or Gravity.END
                params.x = (12f * metrics.density).toInt()
                params.y = yOffset
            }
            POS_BELOW_START -> {
                params.gravity = Gravity.TOP or Gravity.START
                params.x = (12f * metrics.density).toInt()
                params.y = yOffset
            }
            POS_CENTER -> {
                params.gravity = Gravity.CENTER
                params.x = 0
                params.y = 0
            }
            POS_BOTTOM -> {
                params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                params.x = 0
                params.y = (60f * metrics.density).toInt()
            }
            else -> {
                params.gravity = Gravity.TOP or Gravity.END
                params.x = (10f * metrics.density).toInt()
                params.y = -(3f * metrics.density).toInt()
            }
        }
        // در حالت «فقط هنگام قفل» بیرون از قفل پنهان می‌شود
        val lockOnly = prefs.getBoolean(KEY_LOCK_ONLY, false)
        val keyguard = runCatching {
            (getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager).isKeyguardLocked
        }.getOrDefault(false)
        text.visibility = if (lockOnly && !keyguard) View.GONE else View.VISIBLE

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        runCatching { windowManager?.updateViewLayout(text, params) }
    }

    private fun statusBarHeight(): Int {
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else (24f * resources.displayMetrics.density).toInt()
    }

    companion object {
        const val PREFS = "gahshomar_prefs"
        const val KEY_ENABLED = "overlay_enabled"
        const val KEY_POSITION = "overlay_position"
        const val KEY_SIZE = "overlay_size"
        const val KEY_LOCK_ONLY = "overlay_lock_only"
        const val KEY_SHOW_YEAR = "overlay_show_year"
        const val KEY_SHORT = "overlay_short"
        const val KEY_COLOR = "overlay_color"

        const val POS_BAR_START = "barStart"
        const val POS_BAR_CENTER = "barCenter"
        const val POS_BAR_END = "barEnd"
        const val POS_BELOW_START = "belowStart"
        const val POS_BELOW_END = "belowEnd"
        const val POS_CENTER = "center"
        const val POS_BOTTOM = "bottom"

        private const val TICK_MILLIS = 30_000L

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

        /** کارت تنظیمات دسترس‌پذیری برای روشن‌کردن سرویس */
        fun settingsIntent(): Intent =
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
