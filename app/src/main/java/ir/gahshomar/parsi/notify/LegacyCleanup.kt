package ir.gahshomar.parsi.notify

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * پاک‌سازیِ یک‌بارهٔ بازمانده‌های نسخه‌های پیشین.
 *
 * در نسخه‌های ۲٫۱ تا ۲٫۶ تاریخ با یک اعلان در نوار وضعیت نشان داده می‌شد؛ همان اعلان
 * بود که بر صفحهٔ همیشه‌روشن هم می‌نشست و چون نشانِ کوچکِ اعلان جایی برای متن بزرگ
 * ندارد، ریز و ناخوانا دیده می‌شد. از این نسخه (۲٫۷) هیچ اعلانی ساخته نمی‌شود؛
 * این کلاس تنها بازمانده‌ها را می‌زداید: اعلانِ باز، کانال‌ها و ساعتِ بامدادی.
 */
object LegacyCleanup {

    private const val NOTIFICATION_ID = 1405
    private const val ALARM_REQUEST = 1405

    fun clear(context: Context) {
        runCatching {
            context.getSystemService(NotificationManager::class.java)?.cancel(NOTIFICATION_ID)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching {
                val manager = context.getSystemService(NotificationManager::class.java)
                listOf(
                    "gahshomar_day",
                    "gahshomar_day_v2",
                    "gahshomar_day_v3",
                    "gahshomar_day_v4",
                    "gahshomar_reminder"
                ).forEach { manager?.deleteNotificationChannel(it) }
            }
        }
        // ساعتِ بامدادیِ نسخه‌های پیشین؛ گیرنده‌اش دیگر نیست، ولی زنگ همچنان در سامانه می‌مانَد
        runCatching {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val intent = Intent().setClassName(
                context.packageName,
                "ir.gahshomar.parsi.notify.DayStatusReceiver"
            )
            val pending = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager?.cancel(pending)
            pending.cancel()
        }
        // خدمتِ قدیمی، اگر هنوز زنده باشد
        runCatching {
            context.stopService(Intent().setClassName(context.packageName, "ir.gahshomar.parsi.notify.DayStatusService"))
        }
    }
}
