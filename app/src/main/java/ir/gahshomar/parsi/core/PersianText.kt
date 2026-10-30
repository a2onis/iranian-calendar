package ir.gahshomar.parsi.core

import ir.gahshomar.parsi.data.FestivalKind
import ir.gahshomar.parsi.data.Festivals

/** ابزارهای متن و عدد فارسی */
object PersianText {

    private const val PERSIAN_DIGITS = "۰۱۲۳۴۵۶۷۸۹"

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

    fun monthName(month: Int): String = IranianCalendar.monthName(month)

    fun dayName(dayOfMonth: Int): String = IranianCalendar.dayName(dayOfMonth)

    /** «۶ اردیبهشت ۱۴۰۴» */
    fun formatDate(date: IranianDate, persian: Boolean = true): String =
        "${digits(date.day, persian)} ${date.monthName} ${digits(date.year, persian)}"

    /** «۶ اردیبهشت ۱۴۰۴، آدینه» */
    fun formatDateWithWeekday(date: IranianDate, persian: Boolean = true): String =
        "${formatDate(date, persian)}، ${IranianCalendar.weekdayName(date.epochDay)}"

    /**
     * نشانِ کوتاه نوار وضعیت: «۸ آبان / دی به آذر»
     * (روز و ماه، سپس نام کهنِ آن روز)
     */
    fun statusLabel(date: IranianDate, persian: Boolean = true): String =
        "${digits(date.day, persian)} ${date.monthName} / ${date.dayName}"

    /** «بهرام روز از اردیبهشت ماه» */
    fun ritualLabel(date: IranianDate): String = date.ritualLabel

    /** نام روز و معنای آن: «بهرام — پیروزی» */
    fun dayWithMeaning(dayOfMonth: Int): String =
        "${IranianCalendar.dayName(dayOfMonth)} — ${IranianCalendar.dayMeaning(dayOfMonth)}"

    fun kindLabel(kind: FestivalKind): String = kind.label

    /** «۳۰ روز دیگر» / «امروز» / «۵ روز پیش» */
    fun daysAway(days: Int, persian: Boolean = true): String = when {
        days == 0 -> "امروز"
        days == 1 -> "فردا"
        days == 2 -> "پس‌فردا"
        days == -1 -> "دیروز"
        days > 0 -> "${digits(days, persian)} روز دیگر"
        else -> "${digits(-days, persian)} روز پیش"
    }

    fun relativeLabel(epochDay: Long, today: Long, persian: Boolean = true): String =
        daysAway((epochDay - today).toInt(), persian)

    /** ساعت به وقت ایران از یک لحظهٔ UTC */
    fun formatTehranTime(utcMillis: Long, persian: Boolean = true): String {
        val local = utcMillis + (3.5 * 3600 * 1000).toLong()
        val dayMillis = CivilMath.floorMod(local, 86400000L)
        val minutes = (dayMillis / 60000L).toInt()
        val hh = minutes / 60
        val mm = minutes % 60
        return convertDigits(hh.toString().padStart(2, '0'), persian) + ":" +
            convertDigits(mm.toString().padStart(2, '0'), persian)
    }

    /** متن اشتراک‌گذاری یک روز */
    fun shareText(epochDay: Long, persian: Boolean = true): String {
        val date = IranianCalendar.fromEpochDay(epochDay)
        val sb = StringBuilder()
        sb.append(formatDateWithWeekday(date, persian)).append('\n')
        sb.append("روز ").append(date.dayName).append(" — ").append(date.dayMeaning)
        val festivals = Festivals.onEpochDay(epochDay)
        if (festivals.isNotEmpty()) {
            sb.append('\n')
            festivals.forEach { sb.append("• ").append(it.name).append('\n') }
        }
        sb.append('\n').append("گاه‌شمار ایرانی")
        return sb.toString()
    }
}
