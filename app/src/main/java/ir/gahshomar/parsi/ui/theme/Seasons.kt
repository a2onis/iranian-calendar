package ir.gahshomar.parsi.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import ir.gahshomar.parsi.core.IranianCalendar

/**
 * چهار فصل، چهار رنگ‌مایه.
 *
 * پوستهٔ «فصلی» با ماهِ ایرانی می‌گردد:
 *  • بهار (فروردین تا خرداد): سبزِ سبزه و صورتیِ شکوفه، روشن
 *  • تابستان (تیر تا شهریور): فیروزهٔ آب و زرِ خورشید، روشن
 *  • پاییز (مهر تا آذر): نارنجیِ برگ و ارغوانِ انار، تیره
 *  • زمستان (دی تا سپندارمذ): آبیِ یخ و نقرهٔ برف، تیره
 */
enum class Season(
    val label: String,
    val prefersDark: Boolean,
    val accent: Color,
    val accentBright: Color,
    val accentDeep: Color,
    val second: Color,
    val secondBright: Color,
    val secondDeep: Color,
    val third: Color,
    /** رنگِ زمینهٔ پوستهٔ تیره */
    val nightBackground: Color,
    val nightSurface: Color,
    val nightVariant: Color,
    val nightLine: Color,
    /** رنگِ زمینهٔ پوستهٔ روشن */
    val dayBackground: Color,
    val daySurface: Color,
    val dayVariant: Color,
    val dayLine: Color
) {
    SPRING(
        label = "بهار",
        prefersDark = false,
        accent = Color(0xFF2E7D4F),        // سبزِ سبزه
        accentBright = Color(0xFF7FB77E),
        accentDeep = Color(0xFF14452A),
        second = Color(0xFFC9436B),        // صورتیِ شکوفه
        secondBright = Color(0xFFFF9FB6),
        secondDeep = Color(0xFF7A1D38),
        third = Color(0xFFD9762F),         // نارنجیِ لاله
        nightBackground = Color(0xFF08160F),
        nightSurface = Color(0xFF0F2418),
        nightVariant = Color(0xFF1A3524),
        nightLine = Color(0xFF2A4A34),
        dayBackground = Color(0xFFF3F7EA),
        daySurface = Color(0xFFFCFEF7),
        dayVariant = Color(0xFFE4EDD6),
        dayLine = Color(0xFFB9CBA4)
    ),
    SUMMER(
        label = "تابستان",
        prefersDark = false,
        accent = Color(0xFF0E8F8F),        // فیروزهٔ آب
        accentBright = Color(0xFF5FD6CE),
        accentDeep = Color(0xFF07504F),
        second = Color(0xFFB07B10),        // زرِ خورشید
        secondBright = Color(0xFFF2C766),
        secondDeep = Color(0xFF6B4A05),
        third = Color(0xFFD2691E),
        nightBackground = Color(0xFF06141A),
        nightSurface = Color(0xFF0B2230),
        nightVariant = Color(0xFF123143),
        nightLine = Color(0xFF204A5F),
        dayBackground = Color(0xFFF1F7F5),
        daySurface = Color(0xFFFBFEFC),
        dayVariant = Color(0xFFDCEDE8),
        dayLine = Color(0xFFA9CCC5)
    ),
    AUTUMN(
        label = "پاییز",
        prefersDark = true,
        accent = Color(0xFFC9761B),        // نارنجیِ برگ
        accentBright = Color(0xFFF2A95C),
        accentDeep = Color(0xFF7A420A),
        second = Color(0xFF9B2B4A),        // ارغوانِ انار
        secondBright = Color(0xFFE47B93),
        secondDeep = Color(0xFF5C1226),
        third = Color(0xFF8A7A2E),
        nightBackground = Color(0xFF150E08),
        nightSurface = Color(0xFF231810),
        nightVariant = Color(0xFF33241A),
        nightLine = Color(0xFF4A3726),
        dayBackground = Color(0xFFF8F0E4),
        daySurface = Color(0xFFFFFBF3),
        dayVariant = Color(0xFFEFE0CB),
        dayLine = Color(0xFFC9B18F)
    ),
    WINTER(
        label = "زمستان",
        prefersDark = true,
        accent = Color(0xFF3F7FA8),        // آبیِ یخ
        accentBright = Color(0xFF8EC5E6),
        accentDeep = Color(0xFF1B4463),
        second = Color(0xFF8E8FA6),        // نقرهٔ برف
        secondBright = Color(0xFFC9CEE0),
        secondDeep = Color(0xFF4A4C63),
        third = Color(0xFF7A5C9E),
        nightBackground = Color(0xFF080F1A),
        nightSurface = Color(0xFF0F1B2A),
        nightVariant = Color(0xFF17283C),
        nightLine = Color(0xFF263D55),
        dayBackground = Color(0xFFEFF4F9),
        daySurface = Color(0xFFFBFDFF),
        dayVariant = Color(0xFFDDE8F2),
        dayLine = Color(0xFFAEC4D6)
    );

    val darkScheme: ColorScheme
        get() = darkColorScheme(
            primary = accentBright,
            onPrimary = nightBackground,
            primaryContainer = nightVariant,
            onPrimaryContainer = accentBright,
            secondary = secondBright,
            onSecondary = nightBackground,
            secondaryContainer = nightVariant,
            onSecondaryContainer = Color(0xFFEFF6FF),
            tertiary = third,
            onTertiary = nightBackground,
            tertiaryContainer = nightVariant,
            onTertiaryContainer = Color(0xFFFFE9D2),
            background = nightBackground,
            onBackground = Color(0xFFF4EEE2),
            surface = nightSurface,
            onSurface = Color(0xFFF7F1E6),
            surfaceVariant = nightVariant,
            onSurfaceVariant = Color(0xFFCFC6B6),
            outline = nightLine,
            outlineVariant = nightLine.copy(alpha = 0.6f),
            error = Color(0xFFFF8AA0),
            onError = Color(0xFF2A000A),
            inverseSurface = Color(0xFFF4EEE2),
            inverseOnSurface = nightBackground
        )

    val lightScheme: ColorScheme
        get() = lightColorScheme(
            primary = accentDeep,
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = dayVariant,
            onPrimaryContainer = accentDeep,
            secondary = secondDeep,
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = dayVariant,
            onSecondaryContainer = secondDeep,
            tertiary = third,
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = dayVariant,
            onTertiaryContainer = Color(0xFF3A1A08),
            background = dayBackground,
            onBackground = Color(0xFF241F17),
            surface = daySurface,
            onSurface = Color(0xFF241F17),
            surfaceVariant = dayVariant,
            onSurfaceVariant = Color(0xFF5C5346),
            outline = dayLine,
            outlineVariant = dayLine.copy(alpha = 0.6f),
            error = Color(0xFFB02A44),
            onError = Color(0xFFFFFFFF),
            inverseSurface = Color(0xFF241F17),
            inverseOnSurface = dayBackground
        )

    val darkPalette: GahPalette
        get() = GahPalette(
            gold = accentBright,
            goldSoft = nightVariant,
            turquoise = secondBright,
            pomegranate = Color(0xFFFF8AA0),
            saffron = third,
            motif = accentBright.copy(alpha = 0.24f),
            headerTop = nightBackground,
            headerBottom = nightVariant,
            onHeader = Color(0xFFF7EFDF),
            cardBorder = accentBright.copy(alpha = 0.28f),
            holiday = Color(0xFFFF8AA0),
            todayRing = secondBright,
            onGold = Color(0xFF14100A)
        )

    val lightPalette: GahPalette
        get() = GahPalette(
            gold = accentDeep,
            goldSoft = dayVariant,
            turquoise = secondDeep,
            pomegranate = Color(0xFF9B2B4A),
            saffron = third,
            motif = accentDeep.copy(alpha = 0.18f),
            headerTop = accentDeep,
            headerBottom = accentDeep.copy(alpha = 0.85f),
            onHeader = Color(0xFFFBF6EA),
            cardBorder = accentDeep.copy(alpha = 0.35f),
            holiday = Color(0xFF9B2B4A),
            todayRing = secondDeep,
            onGold = Color(0xFF14100A)
        )

    companion object {
        /** فصلِ یک ماهِ ایرانی (۱ تا ۱۲) */
        fun ofMonth(month: Int): Season = when (month.coerceIn(1, 12)) {
            in 1..3 -> SPRING
            in 4..6 -> SUMMER
            in 7..9 -> AUTUMN
            else -> WINTER
        }

        /** فصلِ یک روز */
        fun of(epochDay: Long): Season =
            ofMonth(IranianCalendar.fromEpochDay(epochDay).month)
    }
}
