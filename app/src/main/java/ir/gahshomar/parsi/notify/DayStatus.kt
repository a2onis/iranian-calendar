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

    const val CHANNEL_ID = "gahshomar_day_v4"
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
            listOf(
                "gahshomar_day", "gahshomar_day_v2", "gahshomar_day_v3", "gahshomar_reminder"
            ).forEach { old ->
                if (old != CHANNEL_ID) manager.deleteNotificationChannel(old)
            }
        }
        // اهمیتِ DEFAULT (نه LOW) تا اگر کاربر «اعلان‌های بی‌صدا را روی صفحهٔ قفل پنهان کن»
        // را روشن کرده باشد، تاریخ از صفحهٔ قفل ناپدید نشود. صدا و لرزش ندارد.
        val channel = NotificationChannel(
            CHANNEL_ID,
            "تاریخ در نوار وضعیت",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            setSound(null, null)
            vibrationPattern = null
            setShowBadge(false)
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
        // «دی به آذر ۹ آبان» — نام روز، شمارهٔ روز، نام ماه
        val label = PersianText.dayMonthLabel(date, persian)
        val barLabel = PersianText.statusLabel(
            date,
            persian,
            short = prefs.getString("status_text", "full") == "short"
        )
        val position = prefs.getString("status_position", POSITION_RIGHT) ?: POSITION_RIGHT
        val weekday = IranianCalendar.weekdayName(today)
        val festivals = Festivals.onEpochDay(today)
        val year = PersianText.pahlaviYear(date, persian)

        // سطرهای صفحهٔ قفل: ماه و سال پهلوی، روزِ نام‌دار، سپس جشن‌های امروز
        val headline = "$label • $weekday"
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
            .setDefaults(0)
            .setSound(null)
            .setVibrate(null)
            .setLocalOnly(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // نسخهٔ آشکار برای صفحهٔ قفل (همان تاریخ، بی هیچ پنهان‌کاری)
        notification.publicVersion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(statusBarIcon(context, barLabel, position))
            .setContentTitle(label)
            .setContentText(headline)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detail))
            .setShowWhen(false)
            .setDefaults(0)
            .setSound(null)
            .setVibrate(null)
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
        val metrics = context.resources.displayMetrics
        val density = metrics.density
        val barHeight = (24f * density).toInt().coerceAtLeast(24)
        val screenWidth = metrics.widthPixels.coerceAtLeast(barHeight * 6)

        val typeface = runCatching {
            ResourcesCompat.getFont(context, R.font.vazirmatn_medium)
        }.getOrNull() ?: Typeface.create("sans-serif-medium", Typeface.NORMAL)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.typeface = typeface
            textAlign = Paint.Align.LEFT
            // دو برابرِ پیشین: هم‌اندازهٔ ساعت یا بزرگ‌تر
            textSize = barHeight * 1.9f
        }

        fun measure(): Rect {
            val rect = Rect()
            paint.getTextBounds(text, 0, text.length, rect)
            return rect
        }

        // اگر بلندیِ متن از دو برابرِ نوار بیشتر شد، کمی کوچک می‌شود
        var ink = measure()
        val heightLimit = barHeight * 2.1f
        if (ink.height() > heightLimit) {
            paint.textSize *= heightLimit / ink.height()
            ink = measure()
        }
        // اگر از سه برابرِ عرض صفحه پهن‌تر شد، تا آن اندازه کوچک می‌شود
        val widthLimit = screenWidth * 3f
        if (ink.width() > widthLimit) {
            paint.textSize *= widthLimit / ink.width()
            ink = measure()
        }

        val pad = (density * 2f).toInt().coerceAtLeast(1)
        val textWidth = ink.width()
        // عرضِ تصویر: متن + حاشیه. برای «میان» و «چپ» سطحِ گسترده‌تری می‌سازیم
        // تا متن واقعاً در میانه یا آغازِ نوار بنشیند، نه اینکه فقط حاشیه بیفزاید.
        val width = when (position) {
            POSITION_LEFT -> textWidth + pad * 2
            POSITION_CENTER -> textWidth * 3 + pad * 2
            else -> textWidth + pad * 2
        }
        val height = (ink.height() + pad * 2).coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val baseline = (pad - ink.top).toFloat()
        when (position) {
            POSITION_LEFT -> canvas.drawText(text, pad.toFloat(), baseline, paint)
            POSITION_CENTER -> {
                // نوار وضعیت تصویر را در جایگاه خود می‌چسباند؛ متن در میانهٔ آن می‌نشیند
                val x = (bitmap.width - textWidth) / 2f
                canvas.drawText(text, x, baseline, paint)
            }
            else -> canvas.drawText(
                text,
                (bitmap.width - textWidth - pad).toFloat(),
                baseline,
                paint
            )
        }
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
