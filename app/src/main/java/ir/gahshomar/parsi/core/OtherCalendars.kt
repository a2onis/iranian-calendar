package ir.gahshomar.parsi.core

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt

/** تاریخ هجری قمری */
data class HijriDate(val year: Int, val month: Int, val day: Int) {
    override fun toString(): String = "$year/$month/$day"
}

/**
 * گاه‌شماری هجری قمری به روش «حسابی/جدولی» (کویتی) که مبنای تقویم‌های رقومی است.
 * رؤیت هلال در ایران می‌تواند یک یا دو روز با آن اختلاف داشته باشد؛
 * در تنظیمات می‌توان «تصحیح هجری قمری» را بین ۳− تا ۳+ روز تغییر داد.
 */
object HijriCalendar {

    val MONTH_NAMES = arrayOf(
        "محرم", "صفر", "ربیع‌الاول", "ربیع‌الثانی", "جمادی‌الاول", "جمادی‌الثانی",
        "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
    )

    private const val JDN_EPOCH = 1948440L

    fun toEpochDay(hijri: HijriDate): Long {
        val y = hijri.year
        val m = hijri.month
        val d = hijri.day
        val jdn = (11L * y + 3) / 30 + 354L * y + 30L * m - (m - 1) / 2 + d + JDN_EPOCH - 385
        return jdn - CivilMath.JDN_1970_01_01
    }

    fun fromEpochDay(epochDay: Long, offsetDays: Int = 0): HijriDate {
        val jdn = epochDay + CivilMath.JDN_1970_01_01 + offsetDays
        var l = jdn - JDN_EPOCH + 10632
        val n = (l - 1) / 10631
        l = l - 10631 * n + 354
        val j = ((10985 - l) / 5316) * ((50 * l) / 17719) + (l / 5670) * ((43 * l) / 15238)
        l = l - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
        val m = (24 * l) / 709
        val d = l - (709 * m) / 24
        val y = 30 * n + j - 30
        return HijriDate(y.toInt(), m.toInt(), d.toInt())
    }

    fun monthName(month: Int): String = MONTH_NAMES[(month - 1).coerceIn(0, 11)]

    fun monthLength(year: Int, month: Int): Int {
        val start = toEpochDay(HijriDate(year, month, 1))
        val next = if (month == 12) toEpochDay(HijriDate(year + 1, 1, 1))
        else toEpochDay(HijriDate(year, month + 1, 1))
        return (next - start).toInt()
    }
}

/** گاه‌شماری دینی زرتشتی: نام روزها و ماه‌ها */
object ZoroastrianCalendar {

    /** نام سی روز ماه به ترتیب */
    val DAY_NAMES = arrayOf(
        "اورمزد", "بهمن", "اردیبهشت", "شهریور", "سپندارمذ", "خرداد", "امرداد",
        "دین‌به‌دی", "آذر", "آبان", "خور", "ماه", "تیر", "گوش", "دین‌به‌مهر",
        "مهر", "سروش", "رشن", "فروردین", "بهرام", "رام", "گواد", "دین‌به‌دین",
        "دین", "ارد", "اشتاد", "آسمان", "زامیاد", "مانتره‌سپند", "انارم"
    )

    /** معنای کوتاه هر روز */
    val DAY_MEANINGS = arrayOf(
        "خداوند دانا", "اندیشهٔ نیک", "بهترین راستی", "کشور و شهریاری", "فروتنی و مهر",
        "کمال و رسایی", "بی‌مرگی", "آفرینش و داد", "آتش", "آب‌ها", "خورشید", "ماه",
        "ستارهٔ باران", "گوش‌دهندهٔ جهان", "مهر آفرینش", "پیمان و مهر", "فرمانبرداری",
        "دادگری", "فروهرها", "پیروزی", "شادی و آرامش", "باد", "دادِ دین", "دین",
        "خوشبختی", "راستی", "آسمان", "زمین", "کلام مقدس", "بی‌کران"
    )

    /** ماه‌های سال زرتشتی */
    val MONTH_NAMES = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "امرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "سپندارمذ"
    )

    /** جشن‌هایی که روز و ماه هم‌نام می‌شوند (روز [index] از ماه [index]) */
    val NAME_DAY_FESTIVALS = listOf(
        FestivalNameDay("فروردینگان", 1, 19, "جشن فروهرها و یادبود درگذشتگان"),
        FestivalNameDay("اردیبهشتگان", 2, 3, "جشن آتش و راستی، پاسداشت بهترین آفرینش"),
        FestivalNameDay("خردادگان", 3, 6, "جشن رسایی، آب و شادابی"),
        FestivalNameDay("تیرگان", 4, 13, "جشن باران و آب‌پاشی، یاد آرش کمانگیر"),
        FestivalNameDay("امردادگان", 5, 7, "جشن بی‌مرگی و گیاهان"),
        FestivalNameDay("شهریورگان", 6, 4, "جشن شهریاری و نیروی آهنین"),
        FestivalNameDay("مهرگان", 7, 16, "جشن پیمان و دوستی، مهرگان خاصه"),
        FestivalNameDay("آبانگان", 8, 10, "جشن آب‌ها و پاسداشت آناهیتا"),
        FestivalNameDay("آذرگان", 9, 9, "جشن آتش، پاسداشت آتشگاه"),
        FestivalNameDay("دیگان", 10, 8, "جشن آفرینش و داد"),
        FestivalNameDay("بهمنگان", 11, 2, "جشن اندیشهٔ نیک و گیاهان"),
        FestivalNameDay("سپندارمذگان", 12, 5, "جشن زمین و عشق ایرانی")
    )

    fun dayName(dayOfMonth: Int): String = DAY_NAMES[(dayOfMonth - 1).coerceIn(0, 29)]

    fun dayMeaning(dayOfMonth: Int): String = DAY_MEANINGS[(dayOfMonth - 1).coerceIn(0, 29)]

    /** پنج «گاه» روز */
    val GAHS = arrayOf(
        "هاونگاه (بامداد تا نیمروز)",
        "رپیتونگاه (نیمروز تا پسین)",
        "ازیرینگاه (پسین تا غروب)",
        "ایویسرترونگاه (غروب تا نیمه‌شب)",
        "اشاهینگاه (نیمه‌شب تا بامداد)"
    )

    /** شش «گاهنبار» سالانه */
    val GAHAMBARS = listOf(
        Gahambar("میدیوزرم", "میانهٔ بهار", "آفرینش آسمان"),
        Gahambar("میدیوشم", "میانهٔ تابستان", "آفرینش آب"),
        Gahambar("پتیه‌شهیم", "پایان تابستان", "آفرینش زمین"),
        Gahambar("ایاثرم", "آغاز پاییز", "آفرینش گیاه"),
        Gahambar("میدیاریم", "میانهٔ زمستان", "آفرینش جانور"),
        Gahambar("همسپت‌میدیم", "پایان سال (پنجه)", "آفرینش انسان")
    )

    /** نام ماه‌های اوستایی به همراه امشاسپندِ پاسدار آن */
    val MONTH_GUARDIANS = arrayOf(
        "فروردین — فروهرها", "اردیبهشت — اردیبهشت امشاسپند", "خرداد — خرداد امشاسپند",
        "تیر — تیشتر، ایزد باران", "امرداد — امرداد امشاسپند", "شهریور — شهریور امشاسپند",
        "مهر — مهر ایزد، پیمان", "آبان — آناهیتا، ایزد آب", "آذر — آذر ایزد، آتش",
        "دی — دادار، آفریدگار", "بهمن — بهمن امشاسپند", "سپندارمذ — سپندارمذ امشاسپند"
    )
}

data class FestivalNameDay(val name: String, val month: Int, val day: Int, val meaning: String)
data class Gahambar(val name: String, val seasonTime: String, val creation: String)

/** نمادهای وابسته به سال در گاه‌شماری‌های جهانی */
object GlobalSymbols {

    private val CHINESE_ANIMALS = arrayOf(
        "موش", "گاو", "ببر", "گربهٔ وحشی", "اژدها", "مار",
        "اسب", "بز", "میمون", "خروس", "سگ", "خوک"
    )
    private val CHINESE_ELEMENTS = arrayOf("چوب", "آتش", "خاک", "فلز", "آب")

    private const val SYNODIC_MONTH = 29.530588853

    fun chineseZodiac(gregorianYear: Int): String = CHINESE_ANIMALS[CivilMath.floorMod(gregorianYear - 4L, 12L).toInt()]

    fun chineseElement(gregorianYear: Int): String =
        CHINESE_ELEMENTS[CivilMath.floorMod(gregorianYear - 4L, 10L).toInt() / 2]

    /** چرخهٔ ۶۰ سالهٔ چینی */
    fun chineseCycle(gregorianYear: Int): String =
        "سال ${chineseElement(gregorianYear)} ${chineseZodiac(gregorianYear)}"

    /** نماد سال در گاه‌شماری کهن ایرانی: هر سال زیر پاسبانی یکی از امشاسپندان نیست، اما
     *  در باور مردمی، سالِ خورشیدی به دوازده برج بخش می‌شود؛ در اینجا نام برج سال را برمی‌گردانیم. */
    fun persianYearSign(persianYear: Int): String =
        PersianCalendar.ZODIAC_SIGNS[CivilMath.floorMod(persianYear.toLong(), 12L).toInt()]

    fun moonPhase(millis: Long): MoonPhase {
        val jd = millis / 86400000.0 + 2440587.5
        val ageDays = ((jd - 2451550.26) % SYNODIC_MONTH + SYNODIC_MONTH) % SYNODIC_MONTH
        val illumination = ((1 - cos(2 * PI * ageDays / SYNODIC_MONTH)) / 2 * 100).roundToInt()
        val name = when {
            ageDays < 1.0 || ageDays > 28.53 -> "ماه نو"
            ageDays < 5.5 -> "هلال افزاینده"
            ageDays < 9.0 -> "ربع نخست"
            ageDays < 13.5 -> "کوژِ افزاینده"
            ageDays < 16.0 -> "ماه کامل"
            ageDays < 20.5 -> "کوژِ کاهنده"
            ageDays < 24.0 -> "ربع آخر"
            else -> "هلال کاهنده"
        }
        return MoonPhase(name, illumination, ageDays)
    }
}

data class MoonPhase(val name: String, val illuminationPercent: Int, val ageDays: Double)
