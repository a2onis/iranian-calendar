package ir.gahshomar.parsi.core

/**
 * ریاضیات پایهٔ گاه‌شماری.
 * همهٔ محاسبات با «شمارهٔ روز از مبدأ ۱۹۷۰-۰۱-۰۱ میلادی» (epoch day) انجام می‌شود
 * تا هیچ وابستگی‌ای به java.time (که در اندروید پیش از ۲۶ وجود ندارد) نباشد.
 */
object CivilMath {

    const val JDN_1970_01_01 = 2440588L

    fun floorDiv(a: Long, b: Long): Long {
        var q = a / b
        if ((a % b != 0L) && ((a < 0L) != (b < 0L))) q--
        return q
    }

    fun floorMod(a: Long, b: Long): Long = a - floorDiv(a, b) * b

    /** شمارهٔ روز میلادی از مبدأ ۱۹۷۰ */
    fun gregorianToEpochDay(year: Int, month: Int, day: Int): Long {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        val jdn = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
        return jdn.toLong() - JDN_1970_01_01
    }

    fun epochDayToGregorian(epochDay: Long): GregorianDate {
        val jdn = epochDay + JDN_1970_01_01
        val f = jdn + 1401 + (floorDiv(4 * jdn + 274277, 146097) * 3) / 4 - 38
        val e = 4 * f + 3
        val g = floorMod(e, 1461) / 4
        val h = 5 * g + 2
        val day = (floorMod(h, 153) / 5 + 1).toInt()
        val month = (floorMod(floorDiv(h, 153) + 2, 12) + 1).toInt()
        val year = (floorDiv(e, 1461) - 4716 + floorDiv((12 + 2 - month).toLong(), 12L)).toInt()
        return GregorianDate(year, month, day)
    }

    fun isGregorianLeap(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || year % 400 == 0
}

data class GregorianDate(val year: Int, val month: Int, val day: Int) {
    val epochDay: Long get() = CivilMath.gregorianToEpochDay(year, month, day)
    val monthLength: Int
        get() = when (month) {
            2 -> if (CivilMath.isGregorianLeap(year)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }

    override fun toString(): String =
        "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}
