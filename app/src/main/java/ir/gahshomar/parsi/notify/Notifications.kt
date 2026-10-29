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
import ir.gahshomar.parsi.data.Festivals
import java.util.Calendar

/** یادآور روزانهٔ مناسبت‌ها؛ کاملاً آفلاین */
object Notifications {

    const val CHANNEL_ID = "gahshomar_daily"
    const val NOTIFICATION_ID = 1404

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "یادآور جشن‌های ایرانی",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "هر روز، جشن‌ها و آیین‌های ایرانی آن روز را یادآوری می‌کند."
            manager?.createNotificationChannel(channel)
        }
    }

    fun schedule(context: Context, enabled: Boolean, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            1404,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (!enabled) {
            alarmManager.cancel(pending)
            return
        }
        ensureChannel(context)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pending
        )
    }

    /** نمایش یادآور امروز */
    fun notifyToday(context: Context) {
        ensureChannel(context)
        val today = IranianCalendar.todayEpochDay()
        val date = IranianCalendar.fromEpochDay(today)
        val festivals = Festivals.onEpochDay(today)
        val title = PersianText.formatDateWithWeekday(date)
        val body = if (festivals.isEmpty()) {
            "امروز جشن ویژه‌ای در گاه‌شمار ایرانی نیست؛ روز ${date.dayName} از ${date.monthName} است."
        } else {
            festivals.joinToString(" • ") { it.name }
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_calendar)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build()
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, notification)
    }
}
