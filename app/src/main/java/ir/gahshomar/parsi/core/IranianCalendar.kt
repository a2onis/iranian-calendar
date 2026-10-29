package ir.gahshomar.parsi.core

/**
 * گاه‌شماری ایرانی — همان گاه‌شماری کهن ایران‌زمین:
 *
 *  • سال در لحظهٔ اعتدال بهاری (نوروز) آغاز می‌شود؛
 *  • دوازده ماهِ سی‌روزه با نام‌های اوستایی/پهلوی (فروردین … سپندارمذ)؛
 *  • هر روز از ماه نامی دارد: از «اورمزد» تا «انارام» (سی روز)؛
 *  • در پایان سال، روزهای گاتها («پنجهٔ وهیک») می‌آید: پنج روز، و در سال کبیسه شش روز.
 *
 * چون ماه‌ها همه سی‌روزه‌اند و روزها نام دارند، هر جشن کهن ایرانی (جشن‌های
 * «روز و ماه هم‌نام») هر سال در همان جای خود می‌افتد.
 */
object IranianCalendar {

    const val MONTH_DAYS = 30
    const val MONTH_COUNT = 12

    /** نام دوازده ماه */
    val MONTH_NAMES = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "امرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "سپندارمذ"
    )

    /** نام سی روز ماه، به ترتیب */
    val DAY_NAMES = arrayOf(
        "اورمزد", "بهمن", "اردیبهشت", "شهریور", "سپندارمذ", "خرداد", "امرداد",
        "دی به آذر", "آذر", "آبان", "خور", "ماه", "تیر", "گوش", "دی به مهر",
        "مهر", "سروش", "رشن", "فروردین", "بهرام", "رام", "باد", "دی به دین",
        "دین", "ارد", "اشتاد", "آسمان", "زامیاد", "مانتره سپند", "انارام"
    )

    /** معنای کوتاه هر روز */
    val DAY_MEANINGS = arrayOf(
        "خداوند دانا", "اندیشهٔ نیک", "بهترین راستی", "کشور و شهریاری", "فروتنی و مهر",
        "کمال و رسایی", "بی‌مرگی", "آفریدگار، ماه آتش", "آتش", "آب‌ها", "خورشید", "ماه",
        "ستارهٔ باران", "گوش‌دهندهٔ جهان", "آفریدگار، ماه مهر", "پیمان و مهر",
        "پیام‌آوری و شنوایی", "دادگری", "فروهرها", "پیروزی", "شادی و آرامش", "باد",
        "آفریدگار، ماه دین", "دین", "خوشبختی", "راستی", "آسمان", "زمین",
        "کلام مقدس", "بی‌کران"
    )

    /** پنج روز گاتها (پنجهٔ وهیک) در پایان سال */
    val GATHA_NAMES = arrayOf("اهونود", "اشتود", "سپنتمد", "وهوخشتر", "وهیشتواشت")

    /** نام روز ششمِ پنجه، تنها در سال کبیسه */
    const val LEAP_GATHA_NAME = "وهیشتواشت دوم"

    /** نام برج‌های دوازده‌گانه که هر ماه با آن پیوند دارد (برای نقش‌مایه) */
    val MONTH_SIGNS = arrayOf(
        "بره", "گاو", "دوپیکر", "خرچنگ", "شیر", "خوشه‌دانه",
        "ترازو", "کژدم", "کمان", "بزغاله", "دلو", "دوماهی"
    )

    /** نام روزهای هفته، از شنبه؛ آدینه نام ایرانی جمعه است */
    val WEEKDAY_NAMES = arrayOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "آدینه"
    )
    val WEEKDAY_NAMES_SHORT = arrayOf("ش", "ی", "د", "س", "چ", "پ", "ج")

    fun monthName(month: Int): String = MONTH_NAMES[(month - 1).coerceIn(0, 11)]

    fun dayName(dayOfMonth: Int): String = DAY_NAMES[(dayOfMonth - 1).coerceIn(0, 29)]

    fun dayMeaning(dayOfMonth: Int): String = DAY_MEANINGS[(dayOfMonth - 1).coerceIn(0, 29)]

    /** نام روز گاتها (۱ تا ۶) */
    fun gathaName(index: Int): String =
        if (index in 1..5) GATHA_NAMES[index - 1] else LEAP_GATHA_NAME

    // ─────────────────────────────────────────────── مرزهای سال

    /** نخستین روز سال (نوروز) */
    fun yearStart(year: Int): Long = PersianCalendar.nowruzEpochDay(year)

    /** درازای سال: ۳۶۵ یا ۳۶۶ روز */
    fun yearLength(year: Int): Int = PersianCalendar.yearLength(year)

    /** شمار روزهای پنجه در این سال: ۵ یا ۶ */
    fun panjehLength(year: Int): Int = yearLength(year) - MONTH_COUNT * MONTH_DAYS

    fun monthStart(year: Int, month: Int): Long = yearStart(year) + (month - 1).toLong() * MONTH_DAYS

    fun panjehStart(year: Int): Long = yearStart(year) + MONTH_COUNT.toLong() * MONTH_DAYS

    /** سالی که این روز در آن است */
    fun yearOf(epochDay: Long): Int {
        var year = CivilMath.epochDayToGregorian(epochDay).year - 621
        while (epochDay < yearStart(year)) year--
        while (epochDay >= yearStart(year + 1)) year++
        return year
    }

    fun dayOfYear(epochDay: Long): Int = (epochDay - yearStart(yearOf(epochDay)) + 1).toInt()

    fun isLeapYear(year: Int): Boolean = yearLength(year) == 366

    // ─────────────────────────────────────────────── تبدیل‌ها

    /** روزِ سال (۱ تا ۳۶۵/۳۶۶) از تاریخ ایرانی */
    fun dayOfYear(date: IranianDate): Int =
        if (date.isPanjeh) MONTH_COUNT * MONTH_DAYS + date.panjehIndex
        else (date.month - 1) * MONTH_DAYS + date.day

    fun toEpochDay(date: IranianDate): Long =
        yearStart(date.year) + dayOfYear(date) - 1

    fun toEpochDay(year: Int, month: Int, day: Int): Long =
        toEpochDay(IranianDate(year, month, day))

    /** تاریخ ایرانیِ یک روز */
    fun fromEpochDay(epochDay: Long): IranianDate {
        val year = yearOf(epochDay)
        val index = (epochDay - yearStart(year)).toInt() // ۰ تا ۳۶۴/۳۶۵
        return if (index < MONTH_COUNT * MONTH_DAYS) {
            IranianDate(year, index / MONTH_DAYS + 1, index % MONTH_DAYS + 1)
        } else {
            IranianDate(year, 0, 0, panjehIndex = index - MONTH_COUNT * MONTH_DAYS + 1)
        }
    }

    fun todayEpochDay(): Long = PersianCalendar.todayEpochDay()

    fun today(): IranianDate = fromEpochDay(todayEpochDay())

    // ─────────────────────────────────────────────── هفته و زمان

    /** ۰ = شنبه … ۶ = آدینه */
    fun weekdayIndex(epochDay: Long): Int = PersianCalendar.weekdayIndex(epochDay)

    fun weekdayName(epochDay: Long): String = WEEKDAY_NAMES[weekdayIndex(epochDay)]

    fun isFriday(epochDay: Long): Boolean = weekdayIndex(epochDay) == 6

    /** پنج گاهِ روز در سنت زرتشتی */
    fun gahOfDay(hourOfDay: Int): String = when (hourOfDay.coerceIn(0, 23)) {
        in 0..5 -> "اشاهین‌گاه"
        in 6..11 -> "هاون‌گاه"
        in 12..15 -> "رپیتون‌گاه"
        in 16..19 -> "ازیرین‌گاه"
        else -> "ایوی‌سرترون‌گاه"
    }
}

/**
 * یک روز در گاه‌شماری ایرانی.
 * وقتی [panjehIndex] بزرگ‌تر از صفر باشد، روز از روزهای گاتها است.
 */
data class IranianDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val panjehIndex: Int = 0
) {
    val isPanjeh: Boolean get() = panjehIndex > 0

    val monthName: String get() = if (isPanjeh) "پنجه" else IranianCalendar.monthName(month)

    val dayName: String
        get() = if (isPanjeh) IranianCalendar.gathaName(panjehIndex) else IranianCalendar.dayName(day)

    val dayMeaning: String
        get() = if (isPanjeh) "روز گاتها" else IranianCalendar.dayMeaning(day)

    val epochDay: Long get() = IranianCalendar.toEpochDay(this)

    /** «بهرام روز از اردیبهشت ماه» */
    val ritualLabel: String
        get() = if (isPanjeh) "روز ${dayName} از پنجهٔ وهیک" else "روز $dayName از $monthName ماه"

    /** «۶ اردیبهشت ۱۴۰۴» — رقم‌ها لاتین‌اند و در نمایش فارسی می‌شوند */
    fun shortLabel(): String = if (isPanjeh) "${dayName}، پنجهٔ $year" else "$day $monthName $year"
}
