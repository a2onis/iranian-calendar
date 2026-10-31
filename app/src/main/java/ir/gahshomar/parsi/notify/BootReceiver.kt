package ir.gahshomar.parsi.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** پس از روشن شدن دستگاه، تاریخِ نوار وضعیت بازمی‌گردد و دوباره زمان‌بندی می‌شود */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val prefs = context.getSharedPreferences("gahshomar_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("day_status", true)) {
            runCatching { DayStatus.refresh(context, true) }
            DayStatusService.apply(context)
        }
    }
}
