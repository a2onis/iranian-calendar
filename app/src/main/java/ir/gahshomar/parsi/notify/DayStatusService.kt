package ir.gahshomar.parsi.notify

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder

/**
 * در حالت «فقط بر صفحهٔ قفل»، تاریخ تنها وقتی دیده می‌شود که دستگاه قفل است؛
 * با روشن‌شدن صفحه بی‌درنگ برداشته می‌شود تا برگهٔ اعلان‌ها پاک بماند.
 */
class DayStatusService : Service() {

    private var receiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        val screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (!onlyLocked(context)) return
                runCatching {
                    when (intent.action) {
                        Intent.ACTION_SCREEN_OFF -> DayStatus.show(context)
                        Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> DayStatus.hide(context)
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)
        receiver = screenReceiver
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (onlyLocked(this) && !isKeyguardLocked()) DayStatus.hide(this)
        return START_STICKY
    }

    override fun onDestroy() {
        receiver?.let { runCatching { unregisterReceiver(it) } }
        receiver = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun isKeyguardLocked(): Boolean =
        runCatching {
            (getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager).isKeyguardLocked
        }.getOrDefault(false)

    companion object {
        fun onlyLocked(context: Context): Boolean =
            context.getSharedPreferences("gahshomar_prefs", Context.MODE_PRIVATE)
                .getString("status_mode", "always") == "locked"

        /** در حالت «فقط بر صفحهٔ قفل» سرویس را روشن می‌کند، وگرنه خاموش */
        fun apply(context: Context) {
            val enabled = context.getSharedPreferences("gahshomar_prefs", Context.MODE_PRIVATE)
                .getBoolean("day_status", true)
            val intent = Intent(context, DayStatusService::class.java)
            runCatching {
                if (enabled && onlyLocked(context)) context.startService(intent)
                else context.stopService(intent)
            }
        }
    }
}
