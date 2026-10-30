package ir.gahshomar.parsi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ir.gahshomar.parsi.notify.DayStatus
import ir.gahshomar.parsi.ui.AppState
import ir.gahshomar.parsi.ui.GahshomarApp
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var state: AppState

    /** در اندروید ۱۳ و بالاتر، اجازهٔ نمایش در نوار وضعیت را یک‌بار می‌پرسیم */
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted && ::state.isInitialized) {
                runCatching { DayStatus.refresh(this, state.dayStatusEnabled) }
            }
        }

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
        DayStatus.ensureChannel(this)
        state = AppState(applicationContext)
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                GahshomarApp(state)
            }
        }
        // اجازهٔ اطلاع‌رسانی پس از نخستین نقش‌بندی پرسیده می‌شود تا آغاز برنامه کند نشود
        window.decorView.post { askNotificationPermission() }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted && state.dayStatusEnabled) {
            runCatching { notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) }
        }
    }

    override fun onStart() {
        super.onStart()
        // درون برنامه، تاریخ در خود صفحه هست؛ نشانِ نوار وضعیت برداشته می‌شود
        if (::state.isInitialized) state.hideDayStatusWhileOpen()
    }

    override fun onResume() {
        super.onResume()
        // بازخوانی «امروز» پس از بازگشت به برنامه (مثلاً پس از نیمه‌شب)
        if (::state.isInitialized) state.refreshToday()
    }

    override fun onStop() {
        super.onStop()
        // با بیرون رفتن از برنامه، تاریخ در نوار وضعیت می‌ماند
        if (::state.isInitialized && !isChangingConfigurations) state.showDayStatusWhenClosed()
    }
}
