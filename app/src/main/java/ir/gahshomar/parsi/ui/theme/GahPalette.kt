package ir.gahshomar.parsi.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * رنگ‌مایهٔ «شب ایرانی»: لاجوردِ شب، فیروزهٔ آتشگاه، طلای زر و گلِ انار.
 * همهٔ رنگ‌ها با کنتراست بالا برگزیده شده‌اند تا متن در هر پوسته خوانا باشد.
 */

// ── پایه‌ها
val LapisDeep = Color(0xFF0B1226)
val LapisNight = Color(0xFF111A33)
val LapisSoft = Color(0xFF1B2745)
val LapisLine = Color(0xFF2C3A5C)

val Parchment = Color(0xFFF6EEDC)
val ParchmentSurface = Color(0xFFFFFAF0)
val ParchmentVariant = Color(0xFFEDE2CB)
val InkBrown = Color(0xFF2A231B)
val InkSoft = Color(0xFF6A5C48)

val Turquoise = Color(0xFF17A99B)
val TurquoiseBright = Color(0xFF57D6C7)
val TurquoiseDeep = Color(0xFF0B5F58)

val Gold = Color(0xFFC89834)
val GoldBright = Color(0xFFF0CE7E)
val GoldDeep = Color(0xFF8A6415)

val Terracotta = Color(0xFFD2603A)
val Pomegranate = Color(0xFFB02A44)
val PomegranateBright = Color(0xFFFF8AA0)

/** رنگ‌های اختصاصی نقش‌مایه‌های ایرانی */
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
    val todayRing: Color,
    /** رنگِ متن روی زمینهٔ زر (برای خانهٔ برگزیده‌شده) */
    val onGold: Color
)

val DarkPalette = GahPalette(
    gold = GoldBright,
    goldSoft = Color(0xFF3B3324),
    turquoise = TurquoiseBright,
    pomegranate = PomegranateBright,
    saffron = Color(0xFFF2A659),
    motif = Color(0x3DF0CE7E),
    headerTop = LapisDeep,
    headerBottom = LapisSoft,
    onHeader = Color(0xFFF7EFDF),
    cardBorder = Color(0x40F0CE7E),
    holiday = PomegranateBright,
    todayRing = TurquoiseBright,
    onGold = Color(0xFF1A1305)
)

val LightPalette = GahPalette(
    gold = GoldDeep,
    goldSoft = Color(0xFFF5E6C2),
    turquoise = TurquoiseDeep,
    pomegranate = Pomegranate,
    saffron = Color(0xFFB4681A),
    motif = Color(0x2E8A6415),
    headerTop = LapisNight,
    headerBottom = LapisSoft,
    onHeader = Color(0xFFF7EFDF),
    cardBorder = Color(0x559A7A3A),
    holiday = Pomegranate,
    todayRing = TurquoiseDeep,
    onGold = Color(0xFF1A1305)
)
