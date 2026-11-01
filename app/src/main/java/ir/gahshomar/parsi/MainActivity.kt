package ir.gahshomar.parsi

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ir.gahshomar.parsi.notify.LegacyCleanup
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
        // بازماندهٔ اعلان‌های نسخه‌های پیشین یک‌بار و برای همیشه پاک می‌شود؛
        // از این نسخه هیچ اعلانی ساخته نمی‌شود.
        LegacyCleanup.clear(this)
        state = AppState(applicationContext)
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                GahshomarApp(state)
            }
        }
        window.decorView.post {
            state.refreshOverlayReady()
            state.refreshToday()
        }
    }

    override fun onResume() {
        super.onResume()
        // بازخوانی «امروز» و وضعیت پنجرهٔ تاریخ (مثلاً پس از بازگشت از تنظیمات اندروید)
        if (::state.isInitialized) {
            state.refreshOverlayReady()
            state.refreshToday()
        }
    }
}
