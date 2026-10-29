package ir.gahshomar.parsi.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** بامداد هر روز، نوارگاه‌شمار تازه می‌شود و دوباره زمان‌بندی می‌گردد */
class DayStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("gahshomar_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("day_status", true)) return
        runCatching {
            DayStatus.show(context)
            DayStatus.scheduleMidnight(context)
        }
    }
}
