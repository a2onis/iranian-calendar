package ir.gahshomar.parsi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.navigationBarsPadding

/**
 * برنامه یک صفحه دارد: گاه‌شمار ایرانی.
 * نه نوار پایین، نه تب؛ فقط تقویم و جشن‌های روزِ برگزیده.
 */
@Composable
fun GahshomarApp(state: AppState) {
    GahshomarTheme(darkTheme = state.themeIsDark(isSystemInDarkTheme())) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
        ) {
            IranianCalendarScreen(state = state)
        }
    }
}
