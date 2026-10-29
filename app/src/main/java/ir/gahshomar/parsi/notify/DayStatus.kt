package ir.gahshomar.parsi.notify

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import ir.gahshomar.parsi.MainActivity
import ir.gahshomar.parsi.R
import ir.gahshomar.parsi.core.IranianCalendar
import ir.gahshomar.parsi.core.PersianText
import ir.gahshomar.parsi.data.Festivals
import java.util.Calendar

/**
 * گاه‌شمارِ همیشه‌در‌دسترس:
 * نواری ماندگار در نوار وضعیت و بر صفحهٔ قفل، با روز و ماه ایرانی و
 * جشن‌های همان روز. هر روز بامداد، خودبه‌خود تازه می‌شود.
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
                "گاه‌شمار در نوار وضعیت",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "روز و ماه ایرانی و جشن‌های امروز، همیشه در نوار وضعیت و بر صفحهٔ قفل."
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
                setSound(null, null)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }

    /** نشان دادن یا برداشتن نوارگاه‌شمار */
    fun refresh(context: Context, enabled: Boolean) {
        ensureChannel(context)
        if (!enabled) {
            cancel(context)
            cancelMidnight(context)
            return
        }
        show(context)
        scheduleMidnight(context)
    }

    /** ساخت و نمایش نوار امروز */
    fun show(context: Context) {
        ensureChannel(context)
        val today = IranianCalendar.todayEpochDay()
        val date = IranianCalendar.fromEpochDay(today)
        val weekday = IranianCalendar.weekdayName(today)
        val digitsOf = { value: String -> PersianText.convertDigits(value) }
        val festivals = Festivals.onEpochDay(today)

        val dayLabel = if (date.isPanjeh) "پ" + digitsOf(date.panjehIndex.toString())
        else digitsOf(date.day.toString())
        val monthLabel = date.monthName
        val yearLabel = digitsOf(date.year.toString())

        val contentTitle = "$dayLabel $monthLabel — روز ${date.dayName}"
        val contentText = if (festivals.isEmpty()) {
            "روز ${date.dayName} از $monthLabel — ${date.dayMeaning}"
        } else {
            festivals.joinToString(" • ") { it.name }
        }

        val views = RemoteViews(context.packageName, R.layout.notification_day).apply {
            setTextViewText(R.id.notif_day, dayLabel)
            setTextViewText(R.id.notif_month, monthLabel)
            setTextViewText(R.id.notif_year, yearLabel)
            setTextViewText(R.id.notif_weekday, weekday)
            setTextViewText(R.id.notif_ritual, date.ritualLabel)
            setTextViewText(R.id.notif_meaning, date.dayMeaning)

            val upcoming = Festivals.upcoming(today + 1, 1).firstOrNull()
            val upcomingLine = upcoming?.let {
                val days = (it.epochDay - today).toInt()
                val whenLabel = if (days == 1) "فردا" else "${digitsOf(days.toString())} روز دیگر"
                "پیشِ رو: ${it.festival.name} — $whenLabel"
            }
            val occasions = buildString {
                if (festivals.isEmpty()) {
                    append("امروز جشن ویژه‌ای در گاه‌شمار ایرانی نیست.")
                } else {
                    festivals.forEach { append("${it.name} — ${it.kind.label}\n") }
                }
                if (upcomingLine != null) append(upcomingLine)
            }
            setTextViewText(R.id.notif_events, occasions)
            setTextViewText(
                R.id.notif_footer,
                if (festivals.isEmpty()) "گاه‌شمار ایرانی"
                else "${digitsOf(festivals.size.toString())} جشن و آیین امروز • گاه‌شمار ایرانی"
            )
        }

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_calendar)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSubText("$weekday • $yearLabel")
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(views)
            .setCustomBigContentView(views)
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

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, notification)
    }

    fun cancel(context: Context) {
        context.getSystemService(NotificationManager::class.java)?.cancel(NOTIFICATION_ID)
    }

    // ───────────────────────────── تازه‌سازی خودکار در بامداد

    /** ساعت ۰۰:۰۵ هر روز، نوارگاه‌شمار تازه می‌شود */
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
