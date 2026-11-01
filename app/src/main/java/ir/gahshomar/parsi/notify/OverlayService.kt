package ir.gahshomar.parsi.notify

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.TextView

/**
 * راهِ دومِ نمایشِ تاریخ — بی هیچ سرویس دسترس‌پذیری.
 *
 * با اجازهٔ «نمایش روی برنامه‌های دیگر» (SYSTEM_ALERT_WINDOW) یک پنجرهٔ
 * TYPE_APPLICATION_OVERLAY می‌کشد و تاریخ را با حروفِ درشت زیر نوار وضعیت
 * (یا هر جای دیگری که برگزیده شده) نشان می‌دهد. این اجازه در همهٔ نسخه‌های
 * اندروید در دسترس است و «تنظیمات محدود» برایش پیش نمی‌آید.
 *
 * این پنجره هنگام قفل دیده نمی‌شود؛ اگر کاربر سرویس دسترس‌پذیری را روشن کند،
 * [LockDateOverlay] کار را به‌دست می‌گیرد و این خدمت خودبه‌خود می‌ایستد تا دو
 * متن روی هم نیفتد. برنامه هیچ اعلانی نمی‌سازد.
 */
class OverlayService : Service() {

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
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT,
                Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED -> refresh()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        refresh()
        handler.post(ticker)
        runCatching { OverlayStyle.prefs(this).registerOnSharedPreferenceChangeListener(prefsListener) }
        runCatching {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
                addAction(Intent.ACTION_DATE_CHANGED)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            }
            registerReceiver(clockReceiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        refresh()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        handler.removeCallbacks(ticker)
        runCatching { OverlayStyle.prefs(this).unregisterOnSharedPreferenceChangeListener(prefsListener) }
        runCatching { unregisterReceiver(clockReceiver) }
        detach()
        super.onDestroy()
    }

    // ───────────────────────────────────────── پنجره

    private fun refresh() {
        val prefs = OverlayStyle.prefs(this)
        val wanted = prefs.getBoolean(OverlayStyle.KEY_ENABLED, true) &&
            !LockDateOverlay.isEnabled(this) &&
            Settings.canDrawOverlays(this)
        if (!wanted) {
            detach()
            return
        }
        if (view == null) {
            attach()
            return
        }
        val text = view ?: return
        val layoutParams = params ?: return
        OverlayStyle.configure(this, text, layoutParams, aboveBar = false)

        text.visibility = View.VISIBLE

        val manager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        runCatching { manager?.updateViewLayout(text, layoutParams) }
    }

    private fun attach() {
        if (view != null) return
        val prefs = OverlayStyle.prefs(this)
        if (!prefs.getBoolean(OverlayStyle.KEY_ENABLED, true)) return
        if (!Settings.canDrawOverlays(this)) return
        val text = OverlayStyle.buildView(this)
        val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            title = "گاه‌شمار"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        view = text
        params = layoutParams
        val added = runCatching { manager.addView(text, layoutParams) }
        if (added.isFailure) {
            view = null
            params = null
            attachedNow = false
            OverlayStyle.report(this, "افزودنِ پنجرهٔ روی برنامه‌ها ناموفق: ${added.exceptionOrNull()?.message}")
            return
        }
        attachedNow = true
        OverlayStyle.report(this, "پنجره روی برنامه‌ها ایستاد")
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

    companion object {
        private const val TICK_MILLIS = 20_000L

        /** آیا پنجرهٔ «روی برنامه‌ها» همین حالا ایستاده است؟ */
        @Volatile
        var attachedNow: Boolean = false
            internal set

        fun canDraw(context: Context): Boolean = Settings.canDrawOverlays(context)

        fun start(context: Context) {
            runCatching { context.startService(Intent(context, OverlayService::class.java)) }
        }

        fun stop(context: Context) {
            runCatching { context.stopService(Intent(context, OverlayService::class.java)) }
        }

        /** پس از هر تغییر تنظیم یا بازگشت به برنامه، دوباره سنجیده می‌شود */
        fun sync(context: Context) {
            if (Settings.canDrawOverlays(context) && !LockDateOverlay.isEnabled(context)) {
                start(context)
            } else {
                stop(context)
            }
        }
    }
}
