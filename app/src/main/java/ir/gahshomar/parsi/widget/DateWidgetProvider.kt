package ir.gahshomar.parsi.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import ir.gahshomar.parsi.MainActivity
import ir.gahshomar.parsi.R
import ir.gahshomar.parsi.core.IranianCalendar
import ir.gahshomar.parsi.core.PersianText
import ir.gahshomar.parsi.notify.OverlayStyle

/**
 * ویجتِ صفحهٔ اصلی: تاریخِ گاه‌شمار با حروفِ درشت.
 *
 * بی هیچ اجازه‌ای کار می‌کند. اگر نه سرویس دسترس‌پذیری در دسترس بود و نه اجازهٔ
 * «نمایش روی برنامه‌های دیگر»، این راه همیشه هست. با هر تازه‌سازی و هر نیم‌ساعت
 * تاریخ از نو خوانده می‌شود، پس هرگز ایستا نمی‌مانَد.
 */
class DateWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val views = buildWidgetViews(context)
        appWidgetIds.forEach { id ->
            runCatching { appWidgetManager.updateAppWidget(id, views) }
        }
        scheduleMidnight(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            ACTION_TICK, Intent.ACTION_BOOT_COMPLETED -> {
                refresh(context)
                scheduleMidnight(context)
            }
        }
    }

    companion object {
        private const val ACTION_TICK = "ir.gahshomar.parsi.WIDGET_TICK"
        private const val REQUEST = 2586

        /** بامداد هر روز، ویجت خودبه‌خود تاریخِ تازه می‌گیرد */
        fun scheduleMidnight(context: Context) {
            runCatching {
                val manager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                    ?: return
                val intent = Intent(context, DateWidgetProvider::class.java)
                    .setAction(ACTION_TICK)
                val pending = PendingIntent.getBroadcast(
                    context,
                    REQUEST,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val calendar = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 5)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(java.util.Calendar.DAY_OF_YEAR, 1)
                    }
                }
                manager.setInexactRepeating(
                    AlarmManager.RTC,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pending
                )
            }
        }

        /** تاریخِ ویجت را بی‌درنگ تازه می‌کند */
        fun refresh(context: Context) {
            runCatching {
                val manager = AppWidgetManager.getInstance(context) ?: return
                val ids = manager.getAppWidgetIds(
                    ComponentName(context, DateWidgetProvider::class.java)
                )
                if (ids == null || ids.isEmpty()) return
                val views = buildWidgetViews(context)
                ids.forEach { manager.updateAppWidget(it, views) }
            }
        }

        private fun buildWidgetViews(context: Context): RemoteViews {
            val prefs = OverlayStyle.prefs(context)
            val persian = prefs.getBoolean("persian_digits", true)
            val date = IranianCalendar.today()
            val views = RemoteViews(context.packageName, R.layout.widget_date)
            views.setTextViewText(R.id.widget_day, PersianText.dayMonthLabel(date, persian))
            views.setTextViewText(R.id.widget_year, PersianText.pahlaviYear(date, persian))
            val pending = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_day, pending)
            views.setOnClickPendingIntent(R.id.widget_year, pending)
            return views
        }
    }
}
