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
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.IconCompat
import ir.gahshomar.parsi.MainActivity
import ir.gahshomar.parsi.R
import ir.gahshomar.parsi.core.IranianCalendar
import ir.gahshomar.parsi.core.PersianText
import ir.gahshomar.parsi.data.Festivals
import java.util.Calendar

/**
 * تاریخ در نوار وضعیت و بر صفحهٔ قفل.
 *
 * اندروید هیچ APIای برای نوشتن متن در نوار وضعیت ندارد؛ تنها چیزی که در آن نوار کشیده
 * می‌شود «نشانِ کوچکِ» یک اعلان است. پس تاریخ را با بوم (Canvas) و با قلم وزیرمتن بر
 * یک تصویر می‌کشیم و همان را نشان اعلان می‌کنیم. جای متن در آن نشان (چپ، میان، راست)
 * و کوتاه یا کامل‌بودنش از تنظیمات می‌آید.
 */
object DayStatus {

    const val CHANNEL_ID = "gahshomar_day_v3"
    const val NOTIFICATION_ID = 1405
    private const val ALARM_REQUEST = 1405

    /** جای متن در نشانِ نوار وضعیت */
    const val POSITION_LEFT = "left"
    const val POSITION_CENTER = "center"
    const val POSITION_RIGHT = "right"

    private fun prefs(context: Context) =
        context.getSharedPreferences("gahshomar_prefs", Context.MODE_PRIVATE)

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        // کانال پیشین (نسخه‌های ۲٫۱ و ۲٫۲) برداشته می‌شود تا نشانی تکراری نماند
        runCatching {
            listOf("gahshomar_day", "gahshomar_day_v2", "gahshomar_reminder").forEach { old ->
                if (old != CHANNEL_ID) manager.deleteNotificationChannel(old)
            }
        }
        val channel = NotificationChannel(
            CHANNEL_ID,
            "تاریخ در نوار وضعیت",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "روز و ماه ایرانی، در نوار وضعیت و بر صفحهٔ قفل."
            setShowBadge(false)
            enableVibration(false)
            enableLights(false)
            setSound(null, null)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
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

    /** «۸ آبان / دی به آذر» یا «۸ آبان» — همان چیزی که در نوار وضعیت خوانده می‌شود */
    fun label(context: Context): String {
        val prefs = prefs(context)
        return PersianText.statusLabel(
            IranianCalendar.fromEpochDay(IranianCalendar.todayEpochDay()),
            persian = prefs.getBoolean("persian_digits", true),
            short = prefs.getString("status_text", "full") == "short"
        )
    }

    fun show(context: Context) {
        ensureChannel(context)
        val prefs = prefs(context)
        val today = IranianCalendar.todayEpochDay()
        val date = IranianCalendar.fromEpochDay(today)
        val persian = prefs.getBoolean("persian_digits", true)
        val label = PersianText.statusLabel(date, persian, short = false)
        val shortLabel = PersianText.statusLabel(date, persian, short = true)
        val barLabel = if (prefs.getString("status_text", "full") == "short") shortLabel else label
        val position = prefs.getString("status_position", POSITION_RIGHT) ?: POSITION_RIGHT
        val weekday = IranianCalendar.weekdayName(today)
        val festivals = Festivals.onEpochDay(today)
        val year = PersianText.pahlaviYear(date, persian)

        // سطرهای صفحهٔ قفل: ماه و سال پهلوی، روزِ نام‌دار، سپس جشن‌های امروز
        val headline = "$shortLabel • $weekday"
        val detail = buildString {
            append("سال $year پهلوی — ${date.ritualLabel}")
            append('\n')
            if (festivals.isEmpty()) {
                append("امروز جشن ویژه‌ای در گاه‌شمار ایرانی نیست.")
            } else {
                appFestivals(festivals, this)
            }
        }

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(statusBarIcon(context, barLabel, position))
            .setContentTitle(label)
            .setContentText(headline)
            .setSubText("سال $year")
            .setStyle(NotificationCompat.BigTextStyle().bigText(detail).setSummaryText(headline))
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setWhen(0)
            .setSilent(true)
            .setLocalOnly(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // نسخهٔ آشکار برای صفحهٔ قفل (همان تاریخ، بی هیچ پنهان‌کاری)
        notification.publicVersion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(statusBarIcon(context, barLabel, position))
            .setContentTitle(label)
            .setContentText(headline)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detail))
            .setShowWhen(false)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()

        context.getSystemService(NotificationManager::class.java)?.notify(NOTIFICATION_ID, notification)
    }

    private fun appFestivals(festivals: List<ir.gahshomar.parsi.data.Festival>, out: StringBuilder) {
        festivals.forEachIndexed { index, festival ->
            if (index > 0) out.append('\n')
            out.append("• ${festival.name} — ${festival.kind.label}")
        }
    }

    fun hide(context: Context) {
        context.getSystemService(NotificationManager::class.java)?.cancel(NOTIFICATION_ID)
    }

    // ───────────────────────────── تاریخ، کشیده بر نشانِ نوار وضعیت

    /**
     * تصویری سپید بر زمینهٔ شفاف که متن تاریخ در آن نوشته شده است.
     * متن تا می‌تواند بزرگ کشیده می‌شود (به بلندیِ نوار وضعیت) و جای آن
     * با [position] جابه‌جا می‌شود: چپ، میان یا راست.
     */
    fun statusBarIcon(context: Context, text: String, position: String): IconCompat {
        val density = context.resources.displayMetrics.density
        val barHeight = (24f * density).toInt().coerceAtLeast(24)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = runCatching {
                ResourcesCompat.getFont(context, R.font.vazirmatn_medium)
            }.getOrNull() ?: Typeface.create("sans-serif-medium", Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
            // به بلندیِ نوار وضعیت، هم‌اندازهٔ ساعتِ نوار وضعیت
            textSize = barHeight * 0.96f
        }
        // متن بلند در نشان جای کمتری می‌گیرد؛ اندازه را کمی پایین می‌آوریم
        if (text.length > 12) paint.textSize = barHeight * 0.78f
        val ink = Rect()
        paint.getTextBounds(text, 0, text.length, ink)
        if (ink.width() <= 0 || ink.height() <= 0) {
            paint.getTextBounds("۰", 0, 1, ink)
        }
        val pad = (density * 1.2f).toInt().coerceAtLeast(1)
        val shift = when (position) {
            POSITION_LEFT -> (ink.width() * 0.55f).toInt()
            POSITION_CENTER -> (ink.width() * 0.27f).toInt()
            else -> 0
        }
        val width = ink.width() + pad * 2 + shift * 2
        val height = ink.height() + pad * 2
        val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(
            text,
            (pad + shift - ink.left).toFloat(),
            (pad - ink.top).toFloat(),
            paint
        )
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
