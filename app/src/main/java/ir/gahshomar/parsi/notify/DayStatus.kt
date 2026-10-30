package ir.gahshomar.parsi.notify

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import ir.gahshomar.parsi.MainActivity
import ir.gahshomar.parsi.R
import ir.gahshomar.parsi.core.IranianCalendar
import ir.gahshomar.parsi.core.PersianText
import java.util.Calendar

/**
 * تاریخ در نوار وضعیت.
 *
 * تنها یک سطر کوتاه — «۸ آبان / دی به آذر» — چون فقط نوار وضعیت را می‌خواهیم:
 * نه طرح سفارشی، نه متن بلند، نه برگهٔ گسترده. هر روز بامداد خودبه‌خود تازه می‌شود.
 * این نشان تنها وقتی برنامه بسته است دیده می‌شود؛ درون برنامه، خودِ صفحه تاریخ را دارد.
 */
object DayStatus {

    const val CHANNEL_ID = "gahshomar_day"
    const val NOTIFICATION_ID = 1405
    private const val ALARM_REQUEST = 1405

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تاریخ در نوار وضعیت",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "روز و ماه ایرانی، کوتاه و بی‌صدا، در نوار وضعیت."
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
                setSound(null, null)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }

    /** نشان دادن یا برداشتن نشانِ نوار وضعیت */
    fun refresh(context: Context, enabled: Boolean) {
        ensureChannel(context)
        if (enabled) {
            show(context)
            scheduleMidnight(context)
        } else {
            hide(context)
            cancelMidnight(context)
        }
    }

    /** یک سطر: «۸ آبان / دی به آذر» */
    fun label(context: Context): String =
        PersianText.statusLabel(
            IranianCalendar.fromEpochDay(IranianCalendar.todayEpochDay()),
            persian = context.getSharedPreferences("gahshomar_prefs", Context.MODE_PRIVATE)
                .getBoolean("persian_digits", true)
        )

    fun show(context: Context) {
        ensureChannel(context)
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_calendar)
            .setContentTitle(label(context))
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setSilent(true)
            .setLocalOnly(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        context.getSystemService(NotificationManager::class.java)?.notify(NOTIFICATION_ID, notification)
    }

    fun hide(context: Context) {
        context.getSystemService(NotificationManager::class.java)?.cancel(NOTIFICATION_ID)
    }

    // ───────────────────────────── تازه‌سازی خودکار در بامداد

    /** ساعت ۰۰:۰۵ هر روز، تاریخ نوار وضعیت تازه می‌شود */
    fun scheduleMidnight(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pending = midnightIntent(context)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 5)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        runCatching {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pending
            )
        }
    }

    fun cancelMidnight(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.cancel(midnightIntent(context))
    }

    private fun midnightIntent(context: Context): PendingIntent {
        val intent = Intent(context, DayStatusReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
