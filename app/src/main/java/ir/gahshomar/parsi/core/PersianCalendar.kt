package ir.gahshomar.parsi.core

/**
 * گاه‌شماری هجری خورشیدی (تقویم رسمی ایران) بر پایهٔ قاعدهٔ نجومی:
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

    val MONTH_NAMES = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    /** نام اوستایی/زرتشتی ماه‌ها */
    val MONTH_NAMES_AVESTAN = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "امرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "سپندارمذ"
    )

    /** روزهای هفته؛ خانهٔ صفر = شنبه (آغاز هفتهٔ ایرانی) */
    val WEEKDAY_NAMES = arrayOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")
    val WEEKDAY_NAMES_SHORT = arrayOf("ش", "ی", "د", "س", "چ", "پ", "ج")
    val WEEKDAY_NAMES_FULL_FA = arrayOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "آدینه"
    )

    const val ASTRONOMICAL_FROM_YEAR = 1179   // ۱۸۰۰ میلادی
    const val ASTRONOMICAL_TO_YEAR = 1600     // ۲۲۲۱ میلادی (با احتیاط: ۲۱۵۰)

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

    private const val PERSIAN_EPOCH_JDN = 1948320L

    /** لحظهٔ دقیق اعتدال بهاری = «لحظهٔ تحویل سال» به وقت تهران */
    fun nowruzMomentUtcMillis(persianYear: Int): Long =
        Astronomy.springEquinoxUtcMillis(persianYear + 621)

    // --------------------------------------------------------------- تبدیل‌ها

    fun isLeapYear(persianYear: Int): Boolean =
        nowruzEpochDay(persianYear + 1) - nowruzEpochDay(persianYear) == 366L

    fun yearLength(persianYear: Int): Int =
        (nowruzEpochDay(persianYear + 1) - nowruzEpochDay(persianYear)).toInt()

    fun monthLength(persianYear: Int, month: Int): Int = when {
        month in 1..6 -> 31
        month in 7..11 -> 30
        month == 12 -> if (isLeapYear(persianYear)) 30 else 29
        else -> 0
    }

    fun dayOfYear(year: Int, month: Int, day: Int): Int =
        if (month <= 7) (month - 1) * 31 + day else 186 + (month - 7) * 30 + day

    fun toEpochDay(date: PersianDate): Long {
        val yearStart = nowruzEpochDay(date.year)
        val offsetBeforeMonth = if (date.month <= 7) (date.month - 1) * 31 else 186 + (date.month - 7) * 30
        return yearStart + offsetBeforeMonth + date.day - 1
    }

    fun toEpochDay(year: Int, month: Int, day: Int): Long = toEpochDay(PersianDate(year, month, day))

    fun fromEpochDay(epochDay: Long): PersianDate {
        val gregorian = CivilMath.epochDayToGregorian(epochDay)
        var year = gregorian.year - 621
        while (epochDay < nowruzEpochDay(year)) year--
        while (epochDay >= nowruzEpochDay(year + 1)) year++
        val doy = (epochDay - nowruzEpochDay(year)).toInt() + 1
        val month: Int
        val day: Int
        if (doy <= 186) {
            month = (doy + 30) / 31
            day = doy - (month - 1) * 31
        } else {
            month = (doy - 157) / 30 + 6
            day = doy - (186 + (month - 7) * 30)
        }
        return PersianDate(year, month, day)
    }

    fun todayEpochDay(): Long = System.currentTimeMillis() / 86400000L

    fun today(): PersianDate = fromEpochDay(todayEpochDay())

    // ------------------------------------------------------------ روزِ هفته

    /** ۰ = شنبه … ۶ = جمعه */
    fun weekdayIndex(epochDay: Long): Int = CivilMath.floorMod(epochDay + 5L, 7L).toInt()

    fun weekdayName(epochDay: Long): String = WEEKDAY_NAMES[weekdayIndex(epochDay)]

    fun isFriday(epochDay: Long): Boolean = weekdayIndex(epochDay) == 6

    /** شمارهٔ هفتهٔ ایرانی در سال (هفتهٔ یکم = هفته‌ای که یکم فروردین در آن است) */
    fun weekOfYear(date: PersianDate): Int = (date.dayOfYear - 1 + weekdayIndex(nowruzEpochDay(date.year))) / 7 + 1

    /** شمارهٔ هفتهٔ ISO-8601 */
    fun isoWeek(epochDay: Long): Int {
        val thursday = epochDay - weekdayIndexIso(epochDay) + 3
        val g = CivilMath.epochDayToGregorian(thursday)
        val jan1 = CivilMath.gregorianToEpochDay(g.year, 1, 1)
        return ((thursday - jan1) / 7).toInt() + 1
    }

    fun isoWeekYear(epochDay: Long): Int {
        val thursday = epochDay - weekdayIndexIso(epochDay) + 3
        return CivilMath.epochDayToGregorian(thursday).year
    }

    private fun weekdayIndexIso(epochDay: Long): Int = CivilMath.floorMod(epochDay + 3L, 7L).toInt() // دوشنبه = ۰

    // ------------------------------------------------------------- فصل‌ها

    enum class Season(val title: String, val months: String) {
        SPRING("بهار", "فروردین تا خرداد"),
        SUMMER("تابستان", "تیر تا شهریور"),
        AUTUMN("پاییز", "مهر تا آذر"),
        WINTER("زمستان", "دی تا اسفند")
    }

    fun seasonOf(month: Int): Season = when (month) {
        in 1..3 -> Season.SPRING
        in 4..6 -> Season.SUMMER
        in 7..9 -> Season.AUTUMN
        else -> Season.WINTER
    }

    /** برج خورشیدی: هر ماه خورشیدی برابر یک برج است */
    val ZODIAC_NAMES = arrayOf(
        "حمل", "ثور", "جوزا", "سرطان", "اسد", "سنبله",
        "میزان", "عقرب", "قوس", "جدی", "دلو", "حوت"
    )

    val ZODIAC_SIGNS = arrayOf(
        "بره", "گاو", "دوپیکر", "خرچنگ", "شیر", "خوشه‌دانه",
        "ترازو", "کژدم", "کمان", "بزغاله", "دلو", "دوماهی"
    )

    fun zodiacOf(month: Int): String = ZODIAC_NAMES[(month - 1).coerceIn(0, 11)]

    // ------------------------------------------------------------ پیش‌بینی

    /** مناسبت‌های نجومی سال: اعتدالین و انقلابین */
    fun solarEvents(gregorianYear: Int): List<SolarMoment> = listOf(
        SolarMoment("اعتدال بهاری (نوروز نجومی)", 0, gregorianYear),
        SolarMoment("انقلاب تابستانی (تیرگان نجومی)", 1, gregorianYear),
        SolarMoment("اعتدال پاییزی (مهرگان نجومی)", 2, gregorianYear),
        SolarMoment("انقلاب زمستانی (شب یلدا)", 3, gregorianYear)
    )
}

data class SolarMoment(val title: String, val quarter: Int, val gregorianYear: Int) {
    val utcMillis: Long get() = Astronomy.momentUtcMillis(gregorianYear, quarter)

    /** روزِ آن رخداد به وقت تهران */
    val tehranEpochDay: Long
        get() = CivilMath.floorDiv(utcMillis + TEHRAN_OFFSET_MILLIS, DAY_MILLIS)

    val epochDay: Long get() = tehranEpochDay

    val persianDate: PersianDate get() = PersianCalendar.fromEpochDay(epochDay)

    private companion object {
        const val DAY_MILLIS = 86400000L
        const val TEHRAN_OFFSET_MILLIS = (3.5 * 3600 * 1000).toLong()
    }
}
