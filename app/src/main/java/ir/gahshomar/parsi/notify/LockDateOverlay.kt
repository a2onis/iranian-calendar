package ir.gahshomar.parsi.notify

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView

/**
 * تاریخ، بزرگ و خوانا، بر نوار وضعیت و صفحهٔ قفل — **بی هیچ اعلانی**.
 *
 * پنجرهٔ «روی برنامه‌ها» هنگام قفل پنهان می‌شود، اما پنجرهٔ سرویس دسترس‌پذیری
 * (TYPE_ACCESSIBILITY_OVERLAY) پنهان نمی‌شود و از نوار وضعیت هم بالاتر می‌نشیند.
 * این سرویس هیچ محتوایی از صفحه نمی‌خواند و تنها یک متن می‌کشد.
 *
 * نکتهٔ مهم (اندروید ۱۳+): برای برنامه‌های نصب‌شده از بیرونِ فروشگاه، اندروید
 * کلیدِ روشن‌کردنِ سرویس دسترس‌پذیری را «تنظیمات محدود» می‌کند تا کاربر خودش
 * اجازه دهد: تنظیمات ← برنامه‌ها ← گاه‌شمار ایرانی ← سه‌نقطه ← «اجازهٔ تنظیمات محدود».
 * اگر این راه در دسترس نبود، [OverlayService] همان تاریخ را با اجازهٔ «نمایش روی
 * برنامه‌های دیگر» می‌کشد و برنامه بی‌نیاز از دسترس‌پذیری کار می‌کند.
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

    private val clockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON, Intent.ACTION_SCREEN_OFF,
                Intent.ACTION_USER_PRESENT, Intent.ACTION_DATE_CHANGED,
                Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> refresh()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        OverlayStyle.report(this, "سرویس دسترس‌پذیری وصل شد")
        refresh()
        handler.post(ticker)
        // پنجرهٔ دسترس‌پذیری بر صفحهٔ قفل هم می‌نشیند؛ نسخهٔ «روی برنامه‌ها» لازم نیست
        runCatching { OverlayService.stop(this) }
        runCatching { OverlayStyle.prefs(this).registerOnSharedPreferenceChangeListener(prefsListener) }
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
        runCatching { OverlayStyle.prefs(this).unregisterOnSharedPreferenceChangeListener(prefsListener) }
        runCatching { unregisterReceiver(clockReceiver) }
        detach()
        // اگر سرویس خاموش شد، راهِ دوم تاریخ را برمی‌گیرد
        runCatching { OverlayService.start(this) }
        super.onDestroy()
    }

    // ───────────────────────────────────────── پنجره

    private fun attach() {
        if (view != null) return
        val text = OverlayStyle.buildView(this)
        val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }
        val added = runCatching { manager.addView(text, layoutParams) }
        if (added.isFailure) {
            // پنجره باز نشد؛ نباید چیزی را «باز» بشماریم تا تلاشِ دوباره انجام شود
            view = null
            params = null
            attachedNow = false
            OverlayStyle.report(this, "افزودنِ پنجره ناموفق: ${added.exceptionOrNull()?.message}")
            return
        }
        view = text
        params = layoutParams
        attachedNow = true
        OverlayStyle.report(this, "پنجره بر نوار وضعیت و صفحهٔ قفل ایستاد")
        refresh()
    }

    private fun detach() {
        val current = view ?: return
        val manager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        runCatching { manager?.removeView(current) }
        view = null
        params = null
        attachedNow = false
    }

    private fun refresh() {
        val prefs = OverlayStyle.prefs(this)
        if (!prefs.getBoolean(OverlayStyle.KEY_ENABLED, true)) {
            detach()
            return
        }
        if (view == null) {
            attach()
            return
        }
        val text = view ?: return
        val layoutParams = params ?: return

        OverlayStyle.configure(this, text, layoutParams, aboveBar = true)

        text.visibility = View.VISIBLE

        val manager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        runCatching { manager?.updateViewLayout(text, layoutParams) }
    }

    companion object {
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
