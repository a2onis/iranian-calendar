package ir.gahshomar.parsi.widget

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
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED -> refresh(context)
        }
    }

    companion object {
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
