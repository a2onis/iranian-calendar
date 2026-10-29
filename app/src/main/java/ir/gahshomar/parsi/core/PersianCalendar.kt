package ir.gahshomar.parsi.core

/**
 * موتور محاسبهٔ نوروز بر پایهٔ قاعدهٔ نجومی:
 *
 *   آغاز هر سال، روزی است که نیمروزِ آن در نصف‌النهار تهران (۵۲٫۵ درجهٔ شرقی)
 *   به لحظهٔ اعتدال بهاری نزدیک‌تر باشد؛ به بیان دیگر اگر لحظهٔ اعتدال پیش از
 *   نیمروز به وقت تهران رخ دهد، همان روز «یکم فروردین» است و اگر پس از نیمروز
 *   باشد، روز بعد یکم فروردین خواهد بود.
 *
 * این همان قاعدهٔ مصوّب مرکز تقویم مؤسسهٔ ژئوفیزیک دانشگاه تهران است.
 * برای سال‌های بیرون از بازهٔ ۱۸۰۰ تا ۲۱۵۰ میلادی که دقت نجومی کاهش می‌یابد،
 * از دورهٔ حساب‌شدهٔ ۲۸۲۰ ساله (بیرشک) استفاده می‌شود.
 */
object PersianCalendar {

    /** روزهای هفته؛ خانهٔ صفر = شنبه (آغاز هفتهٔ ایرانی) */
    val WEEKDAY_NAMES = arrayOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")
    val WEEKDAY_NAMES_SHORT = arrayOf("ش", "ی", "د", "س", "چ", "پ", "ج")
    val WEEKDAY_NAMES_FULL_FA = arrayOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "آدینه"
    )

    private val nowruzCache = HashMap<Int, Long>()

    // ---------------------------------------------------------------- نوروز

    /** شمارهٔ روز (epoch) یکم فروردین سال [persianYear] */
    fun nowruzEpochDay(persianYear: Int): Long {
        nowruzCache[persianYear]?.let { return it }
        val value = computeNowruz(persianYear)
        nowruzCache[persianYear] = value
        return value
    }

    private fun computeNowruz(persianYear: Int): Long {
        val gYear = persianYear + 621
        return if (gYear in 1800..2150) {
            val pair = Astronomy.localDayAndFraction(gYear, 0)
            val jdn = pair.first
            val frac = pair.second
            val nowruzJdn = if (frac <= 0.5) jdn else jdn + 1L
            nowruzJdn - CivilMath.JDN_1970_01_01
        } else {
            arithmeticNowruzEpochDay(persianYear)
        }
    }

    /** قاعدهٔ حسابی ۲۸۲۰ ساله برای سال‌های دور از امروز */
    fun arithmeticNowruzEpochDay(persianYear: Int): Long {
        val epBase = if (persianYear >= 0) persianYear - 474 else persianYear - 473
        val epYear = 474 + CivilMath.floorMod(epBase.toLong(), 2820L)
        val jdn = 1L + (epYear * 682 - 110) / 2816 +
            (epYear - 1) * 365 +
            CivilMath.floorDiv(epBase.toLong(), 2820L) * 1029983L +
            (PERSIAN_EPOCH_JDN - 1)
        return jdn - CivilMath.JDN_1970_01_01
    }

    /** لحظهٔ دقیق اعتدال بهاری = «لحظهٔ تحویل سال» به وقت تهران */
    fun nowruzMomentUtcMillis(persianYear: Int): Long =
        Astronomy.springEquinoxUtcMillis(persianYear + 621)

    // --------------------------------------------------------------- تبدیل‌ها

    fun isLeapYear(persianYear: Int): Boolean =
        nowruzEpochDay(persianYear + 1) - nowruzEpochDay(persianYear) == 366L

    fun yearLength(persianYear: Int): Int =
        (nowruzEpochDay(persianYear + 1) - nowruzEpochDay(persianYear)).toInt()

    fun todayEpochDay(): Long = System.currentTimeMillis() / 86400000L

    // ------------------------------------------------------------ روزِ هفته

    /** ۰ = شنبه … ۶ = جمعه */
    fun weekdayIndex(epochDay: Long): Int = CivilMath.floorMod(epochDay + 5L, 7L).toInt()

    fun weekdayName(epochDay: Long): String = WEEKDAY_NAMES[weekdayIndex(epochDay)]

    private const val PERSIAN_EPOCH_JDN = 1948320L
}
