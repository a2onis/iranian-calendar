package ir.gahshomar.parsi

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ir.gahshomar.parsi.notify.Notifications
import ir.gahshomar.parsi.ui.AppState
import ir.gahshomar.parsi.ui.GahshomarApp
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var state: AppState

    /**
     * برنامه به‌طور کامل فارسی و راست‌به‌چپ است؛ مستقل از زبان دستگاه.
     */
    override fun attachBaseContext(newBase: Context) {
        val locale = Locale("fa", "IR")
        Locale.setDefault(locale)
        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Notifications.ensureChannel(this)
        state = AppState(applicationContext)
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                GahshomarApp(state)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // بازخوانی «امروز» پس از بازگشت به برنامه (مثلاً پس از نیمه‌شب)
        if (::state.isInitialized) state.refreshToday()
    }
}
