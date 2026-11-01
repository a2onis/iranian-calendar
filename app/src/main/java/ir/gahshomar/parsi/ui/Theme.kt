package ir.gahshomar.parsi.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ir.gahshomar.parsi.R
import ir.gahshomar.parsi.ui.theme.GahPalette
import ir.gahshomar.parsi.ui.theme.Season

val LocalGahPalette = staticCompositionLocalOf<GahPalette> { Season.AUTUMN.darkPalette }

// ───────────────────────────────────────────── قلم‌ها
val Vazir = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

/** قلم نمایشی برای عنوان‌ها — همان وزیرمتن، تنها سنگین‌تر */
val DisplayFont = Vazir

private val GahTypography = Typography(
    displayLarge = TextStyle(fontFamily = DisplayFont, fontSize = 46.sp, lineHeight = 58.sp),
    displayMedium = TextStyle(fontFamily = DisplayFont, fontSize = 36.sp, lineHeight = 46.sp),
    displaySmall = TextStyle(fontFamily = DisplayFont, fontSize = 29.sp, lineHeight = 40.sp),
    headlineLarge = TextStyle(fontFamily = DisplayFont, fontSize = 26.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = DisplayFont, fontSize = 22.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = DisplayFont, fontSize = 19.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 29.sp),
    titleMedium = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 25.sp),
    titleSmall = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Medium, fontSize = 13.5.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = Vazir, fontSize = 15.5.sp, lineHeight = 29.sp),
    bodyMedium = TextStyle(fontFamily = Vazir, fontSize = 13.5.sp, lineHeight = 25.sp),
    bodySmall = TextStyle(fontFamily = Vazir, fontSize = 12.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Medium, fontSize = 13.5.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = Vazir, fontSize = 10.5.sp, lineHeight = 16.sp)
)

@Composable
fun GahshomarTheme(
    season: Season = Season.AUTUMN,
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) season.darkPalette else season.lightPalette
    CompositionLocalProvider(LocalGahPalette provides palette) {
        MaterialTheme(
            colorScheme = if (darkTheme) season.darkScheme else season.lightScheme,
            typography = GahTypography,
            content = content
        )
    }
}
