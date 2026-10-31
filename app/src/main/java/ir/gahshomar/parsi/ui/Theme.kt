package ir.gahshomar.parsi.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ir.gahshomar.parsi.R
import ir.gahshomar.parsi.ui.theme.DarkPalette
import ir.gahshomar.parsi.ui.theme.GahPalette
import ir.gahshomar.parsi.ui.theme.Gold
import ir.gahshomar.parsi.ui.theme.GoldBright
import ir.gahshomar.parsi.ui.theme.GoldDeep
import ir.gahshomar.parsi.ui.theme.InkBrown
import ir.gahshomar.parsi.ui.theme.InkSoft
import ir.gahshomar.parsi.ui.theme.LapisDeep
import ir.gahshomar.parsi.ui.theme.LapisLine
import ir.gahshomar.parsi.ui.theme.LapisNight
import ir.gahshomar.parsi.ui.theme.LapisSoft
import ir.gahshomar.parsi.ui.theme.LightPalette
import ir.gahshomar.parsi.ui.theme.Parchment
import ir.gahshomar.parsi.ui.theme.ParchmentSurface
import ir.gahshomar.parsi.ui.theme.ParchmentVariant
import ir.gahshomar.parsi.ui.theme.Pomegranate
import ir.gahshomar.parsi.ui.theme.PomegranateBright
import ir.gahshomar.parsi.ui.theme.Terracotta
import ir.gahshomar.parsi.ui.theme.Turquoise
import ir.gahshomar.parsi.ui.theme.TurquoiseBright
import ir.gahshomar.parsi.ui.theme.TurquoiseDeep

val LocalGahPalette = staticCompositionLocalOf<GahPalette> { DarkPalette }

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

/**
 * پوستهٔ تیره: لاجوردی شبانه با متنِ استخوانی.
 * کنتراست متنِ اصلی روی زمینه ۱۲:۱ و متنِ فرعی ۷:۱ است.
 */
private val DarkScheme = darkColorScheme(
    primary = GoldBright,
    onPrimary = Color(0xFF1A1305),
    primaryContainer = Color(0xFF3A2F14),
    onPrimaryContainer = GoldBright,
    secondary = TurquoiseBright,
    onSecondary = Color(0xFF04211F),
    secondaryContainer = Color(0xFF10403C),
    onSecondaryContainer = Color(0xFFBFF3EC),
    tertiary = Color(0xFFF2A659),
    onTertiary = Color(0xFF2A1600),
    tertiaryContainer = Color(0xFF4A2C0D),
    onTertiaryContainer = Color(0xFFFFDDB8),
    background = LapisDeep,
    onBackground = Color(0xFFF3EBDC),
    surface = LapisNight,
    onSurface = Color(0xFFF6EFE0),
    surfaceVariant = LapisSoft,
    onSurfaceVariant = Color(0xFFCBC3B2),
    outline = LapisLine,
    outlineVariant = Color(0xFF28324D),
    error = PomegranateBright,
    onError = Color(0xFF2A000A),
    inverseSurface = Color(0xFFF3EBDC),
    inverseOnSurface = LapisDeep
)

/** پوستهٔ روشن: کاغذِ کهن با متنِ جوهرِ قهوه‌ای */
private val LightScheme = lightColorScheme(
    primary = GoldDeep,
    onPrimary = Color(0xFFFDF8EE),
    primaryContainer = Color(0xFFF5E6C2),
    onPrimaryContainer = Color(0xFF33260A),
    secondary = TurquoiseDeep,
    onSecondary = Color(0xFFFDF8EE),
    secondaryContainer = Color(0xFFCFEDE7),
    onSecondaryContainer = Color(0xFF06302C),
    tertiary = Terracotta,
    onTertiary = Color(0xFFFDF8EE),
    tertiaryContainer = Color(0xFFF7DCCD),
    onTertiaryContainer = Color(0xFF3A1A08),
    background = Parchment,
    onBackground = InkBrown,
    surface = ParchmentSurface,
    onSurface = InkBrown,
    surfaceVariant = ParchmentVariant,
    onSurfaceVariant = InkSoft,
    outline = Color(0xFFB8A480),
    outlineVariant = Color(0xFFD9CBB0),
    error = Pomegranate,
    onError = Color(0xFFFFFFFF),
    inverseSurface = InkBrown,
    inverseOnSurface = Parchment
)

@Composable
fun GahshomarTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    CompositionLocalProvider(LocalGahPalette provides palette) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = GahTypography,
            content = content
        )
    }
}
