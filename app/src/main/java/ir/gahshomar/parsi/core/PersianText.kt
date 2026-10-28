package ir.gahshomar.parsi.core

import kotlin.math.abs

/** ابزارهای متن و عدد فارسی */
object PersianText {

    private const val PERSIAN_DIGITS = "۰۱۲۳۴۵۶۷۸۹"

    val GREGORIAN_MONTHS = arrayOf(
        "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
        "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
    )

    fun digits(value: Int, persian: Boolean = true): String =
        convertDigits(value.toString(), persian)

    fun digits(value: Long, persian: Boolean = true): String =
        convertDigits(value.toString(), persian)

    fun convertDigits(text: String, persian: Boolean = true): String {
        val sb = StringBuilder(text.length)
        for (ch in text) {
            sb.append(
                when {
                    persian && ch in '0'..'9' -> PERSIAN_DIGITS[ch - '0']
                    !persian && ch in '۰'..'۹' -> ('0' + (ch - '۰'))
                    else -> ch
                }
            )
        }
        return sb.toString()
    }

    fun monthName(month: Int): String = PersianCalendar.MONTH_NAMES[(month - 1).coerceIn(0, 11)]
    fun monthNameAvestan(month: Int): String = PersianCalendar.MONTH_NAMES_AVESTAN[(month - 1).coerceIn(0, 11)]
    fun gregorianMonthName(month: Int): String = GREGORIAN_MONTHS[(month - 1).coerceIn(0, 11)]
    fun hijriMonthName(month: Int): String = HijriCalendar.monthName(month)

    fun weekdayName(epochDay: Long): String = PersianCalendar.WEEKDAY_NAMES[PersianCalendar.weekdayIndex(epochDay)]

    /** «۲۱ فروردین ۱۴۰۴» */
    fun formatDate(date: PersianDate, persian: Boolean = true): String =
        "${digits(date.day, persian)} ${monthName(date.month)} ${digits(date.year, persian)}"

    /** «۲۱ فروردین ۱۴۰۴، جمعه» */
    fun formatDateWithWeekday(date: PersianDate, persian: Boolean = true): String =
        "${formatDate(date, persian)}، ${weekdayName(date.epochDay)}"

    fun formatDateShort(date: PersianDate, persian: Boolean = true): String =
        "${digits(date.year, persian)}/${digits(date.month.toString().padStart(2, '0'), persian)}/" +
            digits(date.day.toString().padStart(2, '0'), persian)

    fun formatGregorian(date: GregorianDate, persian: Boolean = true): String =
        "${digits(date.day, persian)} ${gregorianMonthName(date.month)} ${digits(date.year, persian)}"

    fun formatHijri(date: HijriDate, persian: Boolean = true): String =
        "${digits(date.day, persian)} ${hijriMonthName(date.month)} ${digits(date.year, persian)}"

    /** ساعت به وقت ایران از یک لحظهٔ UTC */
    fun formatTehranTime(utcMillis: Long, persian: Boolean = true): String {
        val local = utcMillis + (3.5 * 3600 * 1000).toLong()
        val dayMillis = CivilMath.floorMod(local, 86400000L)
        val minutes = (dayMillis / 60000L).toInt()
        val hh = minutes / 60
        val mm = minutes % 60
        return digits(hh.toString().padStart(2, '0'), persian) + ":" + digits(mm.toString().padStart(2, '0'), persian)
    }

    fun relativeLabel(epochDay: Long, today: Long, persian: Boolean = true): String = when (epochDay - today) {
        0L -> "امروز"
        1L -> "فردا"
        -1L -> "دیروز"
        2L -> "پس‌فردا"
        -2L -> "پریروز"
        else -> ""
    }

    fun daysUntilLabel(days: Long): String = when {
        days == 0L -> "امروز"
        days == 1L -> "۱ روز دیگر"
        days < 0 -> "${digits(abs(days))} روز پیش"
        days < 31 -> "${digits(days)} روز دیگر"
        days < 365 -> "${digits(days / 30)} ماه و ${digits(days % 30)} روز دیگر"
        else -> "${digits(days / 365)} سال و ${digits((days % 365) / 30)} ماه دیگر"
    }

    fun ordinal(day: Int): String = digits(day)

    /** تبدیل عدد به واژه برای شمارش روزها و سال‌ها (تا ۹۹) */
    private val ONES = arrayOf(
        "", "یک", "دو", "سه", "چهار", "پنج", "شش", "هفت", "هشت", "نه",
        "ده", "یازده", "دوازده", "سیزده", "چهارده", "پانزده", "شانزده", "هفده", "هجده", "نوزده"
    )
    private val TENS = arrayOf("", "", "بیست", "سی", "چهل", "پنجاه", "شصت", "هفتاد", "هشتاد", "نود")
    private val HUNDREDS = arrayOf("", "صد", "دویست", "سیصد", "چهارصد", "پانصد", "ششصد", "هفتصد", "هشتصد", "نهصد")

    fun numberToWords(number: Int): String {
        if (number == 0) return "صفر"
        if (number < 0) return "منفی " + numberToWords(-number)
        val parts = ArrayList<String>()
        var n = number
        if (n >= 1000) {
            parts.add(numberToWords(n / 1000) + " هزار")
            n %= 1000
        }
        if (n >= 100) {
            parts.add(HUNDREDS[n / 100])
            n %= 100
        }
        if (n in 1..19) {
            parts.add(ONES[n])
        } else if (n >= 20) {
            val t = TENS[n / 10]
            val o = n % 10
            parts.add(if (o == 0) t else "$t و ${ONES[o]}")
        }
        return parts.joinToString(" و ")
    }
}
