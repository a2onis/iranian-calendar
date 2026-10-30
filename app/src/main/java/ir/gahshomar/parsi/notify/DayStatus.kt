package ir.gahshomar.parsi.notify

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import ir.gahshomar.parsi.MainActivity
import ir.gahshomar.parsi.R
import ir.gahshomar.parsi.core.IranianCalendar
import ir.gahshomar.parsi.core.PersianText
import ir.gahshomar.parsi.data.Festivals
import java.util.Calendar
import kotlin.math.ceil

/**
 * تاریخ در نوار وضعیت و بر صفحهٔ قفل.
 *
 * اندروید هیچ راهی برای نوشتن متن در نوار وضعیت ندارد؛ تنها چیزی که در آن نوار کشیده
 * می‌شود «نشانِ کوچکِ» یک اعلان است. پس تاریخ را با بوم (Canvas) بر یک تصویر می‌کشیم
 * و همان را نشانِ کوچک اعلان می‌کنیم: «۸ آبان / دی به آذر» در نوار وضعیت خوانده می‌شود.
 *
 * بر صفحهٔ قفل و در برگهٔ اعلان‌ها هم همان تاریخ با نام روز و جشن‌های امروز می‌آید.
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
                description = "روز و ماه ایرانی در نوار وضعیت و بر صفحهٔ قفل."
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
                setSound(null, null)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }

    /** نشان دادن یا برداشتن تاریخ */
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

    /** «۸ آبان / دی به آذر» — همان چیزی که در نوار وضعیت خوانده می‌شود */
    fun label(context: Context): String {
        val persian = context.getSharedPreferences("gahshomar_prefs", Context.MODE_PRIVATE)
            .getBoolean("persian_digits", true)
        return PersianText.statusLabel(
            IranianCalendar.fromEpochDay(IranianCalendar.todayEpochDay()),
            persian = persian
        )
    }

    fun show(context: Context) {
        ensureChannel(context)
        val today = IranianCalendar.todayEpochDay()
        val date = IranianCalendar.fromEpochDay(today)
        val persian = context.getSharedPreferences("gahshomar_prefs", Context.MODE_PRIVATE)
            .getBoolean("persian_digits", true)
        val label = PersianText.statusLabel(date, persian)
        val weekday = IranianCalendar.weekdayName(today)
        val festivals = Festivals.onEpochDay(today)

        val detail = buildString {
            append(date.ritualLabel)
            append(" • ")
            append(weekday)
            if (festivals.isNotEmpty()) {
                append('\n')
                festivals.forEach { append("• ${it.name} — ${it.kind.label}\n") }
            } else {
                append("\n")
                append("امروز جشن ویژه‌ای در گاه‌شمار ایرانی نیست.")
            }
        }

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(statusBarIcon(context, label))
            .setContentTitle(label)
            .setContentText("$weekday • ${date.ritualLabel}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(detail)
                    .setSummaryText("گاه‌شمار ایرانی")
            )
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setSilent(true)
            .setLocalOnly(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setColor(Color.WHITE)
            .build()

        context.getSystemService(NotificationManager::class.java)?.notify(NOTIFICATION_ID, notification)
    }

    fun hide(context: Context) {
        context.getSystemService(NotificationManager::class.java)?.cancel(NOTIFICATION_ID)
    }

    // ───────────────────────────── تاریخ، کشیده بر نشانِ نوار وضعیت

    /**
     * تصویری سپید بر زمینهٔ شفاف که متن تاریخ در آن نوشته شده است.
     * نوار وضعیت همین تصویر را به‌جای نشانِ اعلان می‌کشد.
     */
    private fun statusBarIcon(context: Context, text: String): IconCompat {
        val density = context.resources.displayMetrics.density
        val height = (24f * density).toInt().coerceAtLeast(24)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
            textSize = height * 0.74f
        }
        val metrics = paint.fontMetrics
        val textWidth = paint.measureText(text)
        val width = ceil(textWidth).toInt().coerceAtLeast(1)
        val bitmapHeight = ceil(metrics.descent - metrics.ascent).toInt().coerceIn(1, height)
        val bitmap = Bitmap.createBitmap(width + 2, bitmapHeight, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawText(text, 1f, -metrics.ascent, paint)
        return IconCompat.createWithBitmap(bitmap)
    }

    // ───────────────────────────── تازه‌سازی خودکار در بامداد

    /** ساعت ۰۰:۰۵ هر روز، تاریخ تازه می‌شود */
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
