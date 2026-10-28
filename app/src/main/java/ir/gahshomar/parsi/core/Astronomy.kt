package ir.gahshomar.parsi.core

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor

/**
 * محاسبهٔ لحظهٔ اعتدالین و انقلابین به روش «ژان مِئوس» (Astronomical Algorithms, ch. 27)
 * و اختلاف زمان زمینی/جهانی (ΔT) با چندجمله‌ای‌های اسپناک و مئوس.
 *
 * دقت: برای سال‌های ۱۰۰۰ تا ۳۰۰۰ میلادی خطای چند دقیقه‌ای دارد؛ برای تعیین
 * «روزِ نوروز» به وقت تهران بیش از اندازه کافی است (خطا باید از ۳٫۵ ساعت
 * بیشتر شود تا روز عوض شود).
 */
object Astronomy {

    /** اختلاف ساعت جهانی با وقت رسمی ایران: +۳:۳۰ */
    const val TEHRAN_OFFSET_HOURS = 3.5

    private const val JD_1970_01_01_12H = 2440587.5

    // ضرایب سری فوریهٔ مئوس: [A, B, C]
    private val PERIODIC_TERMS = arrayOf(
        doubleArrayOf(485.0, 324.96, 1934.136),
        doubleArrayOf(203.0, 337.23, 32964.467),
        doubleArrayOf(199.0, 342.08, 20.186),
        doubleArrayOf(182.0, 27.85, 445267.112),
        doubleArrayOf(156.0, 73.14, 45036.886),
        doubleArrayOf(136.0, 171.52, 22518.443),
        doubleArrayOf(77.0, 222.54, 65928.934),
        doubleArrayOf(74.0, 296.72, 3034.906),
        doubleArrayOf(70.0, 243.58, 9037.513),
        doubleArrayOf(58.0, 119.81, 33718.147),
        doubleArrayOf(52.0, 297.17, 150.678),
        doubleArrayOf(50.0, 21.02, 2281.226),
        doubleArrayOf(45.0, 247.54, 29929.562),
        doubleArrayOf(44.0, 325.15, 31555.956),
        doubleArrayOf(29.0, 60.93, 4443.417),
        doubleArrayOf(18.0, 155.12, 67555.328),
        doubleArrayOf(17.0, 288.79, 4562.452),
        doubleArrayOf(16.0, 198.04, 62894.029),
        doubleArrayOf(14.0, 199.76, 31436.921),
        doubleArrayOf(12.0, 95.39, 14577.848),
        doubleArrayOf(12.0, 287.11, 31931.756),
        doubleArrayOf(12.0, 320.81, 34777.259),
        doubleArrayOf(9.0, 227.73, 1222.114),
        doubleArrayOf(8.0, 15.45, 16859.074)
    )

    /** ΔT به ثانیه (زمان زمینی منهای زمان جهانی) */
    fun deltaTSeconds(year: Int): Double {
        val y = year.toDouble()
        return when {
            y < 1900.0 -> 0.0
            y < 1920.0 -> {
                val t = y - 1900.0
                -2.79 + 1.494119 * t - 0.0598939 * t * t + 0.0061966 * t * t * t - 0.000197 * t * t * t * t
            }
            y < 1941.0 -> {
                val t = y - 1920.0
                21.20 + 0.84493 * t - 0.076100 * t * t + 0.0020936 * t * t * t
            }
            y < 1961.0 -> {
                val t = y - 1950.0
                29.07 + 0.407 * t - t * t / 233.0 + t * t * t / 2547.0
            }
            y < 1986.0 -> {
                val t = y - 1975.0
                45.45 + 1.067 * t - t * t / 260.0 - t * t * t / 718.0
            }
            y < 2005.0 -> {
                val t = y - 2000.0
                63.86 + 0.3345 * t - 0.060374 * t * t + 0.0017275 * t * t * t +
                    0.000651814 * t * t * t * t + 0.00002373599 * t * t * t * t * t
            }
            y < 2050.0 -> {
                val t = y - 2000.0
                62.92 + 0.32217 * t + 0.005589 * t * t
            }
            else -> {
                val t = (y - 1820.0) / 100.0
                -20.0 + 32.0 * t * t - 0.5628 * (2150.0 - y)
            }
        }
    }

    /**
     * لحظهٔ اعتدال/انقلاب به وقت جهانی (JDE، زمان زمینی) به‌صورت روز جولیَن.
     * [quarter]: ۰ = اعتدال بهاری، ۱ = انقلاب تابستانی، ۲ = اعتدال پاییزی، ۳ = انقلاب زمستانی
     */
    fun equinoxSolsticeJde(gregorianYear: Int, quarter: Int): Double {
        val y = (gregorianYear - 2000) / 1000.0
        val jde0 = when (quarter) {
            0 -> 2451623.80984 + 365242.37404 * y + 0.05169 * y * y - 0.00411 * y * y * y - 0.00057 * y * y * y * y
            1 -> 2451716.56767 + 365241.62603 * y + 0.00325 * y * y + 0.00888 * y * y * y - 0.00030 * y * y * y * y
            2 -> 2451810.21715 + 365242.01767 * y - 0.11575 * y * y + 0.00337 * y * y * y + 0.00078 * y * y * y * y
            else -> 2451900.05952 + 365242.74049 * y - 0.06223 * y * y - 0.00823 * y * y * y + 0.00032 * y * y * y * y
        }
        val t = (jde0 - 2451545.0) / 36525.0
        val w = (35999.373 * t - 2.47) * PI / 180.0
        val dLambda = 1.0 + 0.0334 * cos(w) + 0.0007 * cos(2.0 * w)
        var s = 0.0
        for (term in PERIODIC_TERMS) {
            s += term[0] * cos((term[1] + term[2] * t) * PI / 180.0)
        }
        return jde0 + (0.00001 * s) / dLambda
    }

    /** تبدیل روز جولیَن به میلی‌ثانیهٔ یونیکس (UTC) */
    fun jdToUnixMillis(jd: Double): Long = ((jd - JD_1970_01_01_12H) * 86400000.0).toLong()

    /** لحظهٔ اعتدال بهاری به زمان جهانی (میلی‌ثانیهٔ یونیکس) */
    fun springEquinoxUtcMillis(gregorianYear: Int): Long {
        val jde = equinoxSolsticeJde(gregorianYear, 0)
        val jdUt = jde - deltaTSeconds(gregorianYear) / 86400.0
        return jdToUnixMillis(jdUt)
    }

    /** لحظهٔ یک اعتدال/انقلاب به زمان جهانی (میلی‌ثانیهٔ یونیکس) */
    fun momentUtcMillis(gregorianYear: Int, quarter: Int): Long {
        val jde = equinoxSolsticeJde(gregorianYear, quarter)
        val jdUt = jde - deltaTSeconds(gregorianYear) / 86400.0
        return jdToUnixMillis(jdUt)
    }

    /** روزِ جولیَن محلی تهران در لحظهٔ اعتدال بهاری */
    fun springEquinoxLocalJdn(gregorianYear: Int): Long {
        val jde = equinoxSolsticeJde(gregorianYear, 0)
        val jdUt = jde - deltaTSeconds(gregorianYear) / 86400.0
        val jdLocal = jdUt + TEHRAN_OFFSET_HOURS / 24.0
        return floor(jdLocal + 0.5).toLong()
    }

    /** عددمیلادی روزی که اعتدال در آن رخ می‌دهد و کسر شبانه‌روز از نیمه‌شب محلی */
    fun localDayAndFraction(gregorianYear: Int, quarter: Int): Pair<Long, Double> {
        val jde = equinoxSolsticeJde(gregorianYear, quarter)
        val jdUt = jde - deltaTSeconds(gregorianYear) / 86400.0
        val jdLocal = jdUt + TEHRAN_OFFSET_HOURS / 24.0
        val jdn = floor(jdLocal + 0.5).toLong()
        val frac = (jdLocal + 0.5) - jdn
        return Pair(jdn, frac)
    }
}
