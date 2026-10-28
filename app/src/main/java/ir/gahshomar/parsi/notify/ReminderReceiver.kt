package ir.gahshomar.parsi.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** گیرندهٔ یادآور روزانه */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        runCatching { Notifications.notifyToday(context) }
    }
}
