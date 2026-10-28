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

// ───────────────────────────────────────────── رنگ‌های ایرانی
val LapisDeep = Color(0xFF152A54)
val Lapis = Color(0xFF1F3A6E)
val LapisLight = Color(0xFF2F4F8F)
val Turquoise = Color(0xFF0E8C82)
val TurquoiseLight = Color(0xFF43C6B7)
val Gold = Color(0xFFC8952E)
val GoldLight = Color(0xFFE7BE6A)
val Pomegranate = Color(0xFFA5223C)
val Saffron = Color(0xFFE08A2B)
val CreamParchment = Color(0xFFFAF3E7)
val CreamSurface = Color(0xFFFFFBF4)
val CreamVariant = Color(0xFFF1E5D3)
val InkBrown = Color(0xFF2A2521)
val NightBlue = Color(0xFF0D1424)
val NightSurface = Color(0xFF16203A)
val NightVariant = Color(0xFF1F2C4A)
val NightInk = Color(0xFFF0E7D7)

/** رنگ‌های ویژهٔ نقش‌مایه‌های ایرانی */
data class GahPalette(
    val gold: Color,
    val goldSoft: Color,
    val turquoise: Color,
    val pomegranate: Color,
    val saffron: Color,
    val motif: Color,
    val headerTop: Color,
    val headerBottom: Color,
    val onHeader: Color,
    val cardBorder: Color,
    val holiday: Color,
    val todayRing: Color
)

private val LightPalette = GahPalette(
    gold = Gold,
    goldSoft = Color(0xFFF0DFB8),
    turquoise = Turquoise,
    pomegranate = Pomegranate,
    saffron = Saffron,
    motif = Color(0x2E1F3A6E),
    headerTop = LapisDeep,
    headerBottom = LapisLight,
    onHeader = Color(0xFFF7EFE0),
    cardBorder = Color(0x33A98A5B),
    holiday = Pomegranate,
    todayRing = Turquoise
)

private val DarkPalette = GahPalette(
    gold = GoldLight,
    goldSoft = Color(0xFF3A3524),
    turquoise = TurquoiseLight,
    pomegranate = Color(0xFFE06B83),
    saffron = Color(0xFFF0A752),
    motif = Color(0x40D9C9A3),
    headerTop = Color(0xFF0A1120),
    headerBottom = Color(0xFF1B2A4C),
    onHeader = Color(0xFFF3EAD8),
    cardBorder = Color(0x33E7BE6A),
    holiday = Color(0xFFE06B83),
    todayRing = TurquoiseLight
)

val LocalGahPalette = staticCompositionLocalOf { LightPalette }

// ───────────────────────────────────────────── قلم‌ها
val Vazir = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

/** قلم نمایشی برای عنوان‌ها (لاله‌زار) */
val DisplayFont = FontFamily(Font(R.font.lalezar_regular, FontWeight.Normal))

private val GahTypography = Typography(
    displayLarge = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Normal, fontSize = 44.sp, lineHeight = 56.sp),
    displayMedium = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Normal, fontSize = 34.sp, lineHeight = 44.sp),
    displaySmall = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 38.sp),
    headlineLarge = TextStyle(fontFamily = DisplayFont, fontSize = 26.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = DisplayFont, fontSize = 22.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = DisplayFont, fontSize = 19.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 30.sp),
    titleMedium = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 26.sp),
    titleSmall = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = Vazir, fontSize = 16.sp, lineHeight = 30.sp),
    bodyMedium = TextStyle(fontFamily = Vazir, fontSize = 14.sp, lineHeight = 26.sp),
    bodySmall = TextStyle(fontFamily = Vazir, fontSize = 12.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = Vazir, fontSize = 10.sp, lineHeight = 15.sp)
)

private val LightScheme = lightColorScheme(
    primary = Lapis,
    onPrimary = Color(0xFFFFF8EA),
    primaryContainer = Color(0xFFDEE6F6),
    onPrimaryContainer = LapisDeep,
    secondary = Turquoise,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD3EFEA),
    onSecondaryContainer = Color(0xFF00382F),
    tertiary = Gold,
    onTertiary = Color(0xFF221A00),
    tertiaryContainer = Color(0xFFF7E8C4),
    onTertiaryContainer = Color(0xFF2A1F00),
    background = CreamParchment,
    onBackground = InkBrown,
    surface = CreamSurface,
    onSurface = InkBrown,
    surfaceVariant = CreamVariant,
    onSurfaceVariant = Color(0xFF4F4638),
    outline = Color(0xFFC8B79C),
    error = Pomegranate,
    onError = Color(0xFFFFFFFF)
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFA8C4F0),
    onPrimary = Color(0xFF0B1B33),
    primaryContainer = Color(0xFF23375C),
    onPrimaryContainer = Color(0xFFDCE6F8),
    secondary = TurquoiseLight,
    onSecondary = Color(0xFF00312B),
    secondaryContainer = Color(0xFF12514A),
    onSecondaryContainer = Color(0xFFB7EFE7),
    tertiary = GoldLight,
    onTertiary = Color(0xFF2A1F00),
    tertiaryContainer = Color(0xFF4A3A12),
    onTertiaryContainer = Color(0xFFF7E8C4),
    background = NightBlue,
    onBackground = NightInk,
    surface = NightSurface,
    onSurface = NightInk,
    surfaceVariant = NightVariant,
    onSurfaceVariant = Color(0xFFC9C0B0),
    outline = Color(0xFF5A6480),
    error = Color(0xFFE06B83),
    onError = Color(0xFF2A000A)
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
