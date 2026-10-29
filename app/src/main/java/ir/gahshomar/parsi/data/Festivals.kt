package ir.gahshomar.parsi.data

import ir.gahshomar.parsi.core.Astronomy
import ir.gahshomar.parsi.core.CivilMath
import ir.gahshomar.parsi.core.IranianCalendar
import ir.gahshomar.parsi.core.IranianDate

/** قاعدهٔ تکرار یک جشن در سال */
sealed class FestivalRule {
    /** جشنی که روز و ماهش هم‌نام است، یا هر جشنِ روزْ‌ثابت */
    data class MonthDay(val month: Int, val day: Int) : FestivalRule()

    /** روزهای گاتها در پایان سال (پنجهٔ وهیک) */
    data object Panjeh : FestivalRule()

    /** آخرین چهارشنبهٔ پیش از نوروز */
    data object LastWednesday : FestivalRule()

    /** روزی که انقلاب زمستانی در آن رخ می‌دهد (شب یلدا) */
    data object WinterSolstice : FestivalRule()
}

enum class FestivalKind(val label: String) {
    JASHN("جشن"),
    AYIN("آیین"),
    GAHAMBAR("گاهنبار"),
    YADBUD("یادبود")
}

/** یک جشن یا آیین کهن ایرانی */
data class Festival(
    val id: String,
    val name: String,
    val rule: FestivalRule,
    val kind: FestivalKind,
    val summary: String,
    val detail: String
)

data class FestivalOccurrence(val festival: Festival, val epochDay: Long)

/**
 * جشن‌ها، آیین‌ها و گاهنبارهای ایران کهن.
 * همهٔ تاریخ‌ها بر پایهٔ گاه‌شماری ایرانی (ماه سی‌روزه، روزهای نام‌دار) است.
 */
object Festivals {

    val ALL: List<Festival> = listOf(

        // ─────────── بهار
        Festival(
            "nowruz", "نوروز", FestivalRule.MonthDay(1, 1), FestivalKind.JASHN,
            "آغاز بهار و سال نو؛ کهن‌ترین جشن ایرانی",
            "نوروز در لحظهٔ اعتدال بهاری آغاز می‌شود. ایرانیان از هزاران سال پیش این روز را " +
                "با خانه‌تکانی، سبزه، آتش و دیدوبازدید جشن می‌گیرند. در باور کهن، در این روز " +
                "اورمزد جهان را آفرید و فروهرهای درگذشتگان به زمین بازمی‌گردند."
        ),
        Festival(
            "khordadruz", "خردادروز، نوروز بزرگ", FestivalRule.MonthDay(1, 6), FestivalKind.JASHN,
            "ششمین روز سال؛ «نوروز بزرگ» در سنت زرتشتی",
            "خردادروز به خرداد امشاسپند، فروزهٔ کمال و رسایی، تعلق دارد و آن را «نوروز بزرگ» " +
                "می‌نامند؛ روزی برای شادی، آشتی، هدیه و بازگشت به کار و کشت."
        ),
        Festival(
            "sizdah-bedar", "سیزده‌بدر", FestivalRule.MonthDay(1, 13), FestivalKind.AYIN,
            "روز طبیعت؛ پایان آیین‌های نوروزی",
            "در سیزدهم فروردین، سبزهٔ نوروزی را به آب و دشت می‌سپارند و روز را در طبیعت " +
                "می‌گذرانند؛ آیینی برای آشتی با زمین و بدرود با نوروز."
        ),
        Festival(
            "farvardigan", "فروردینگان", FestivalRule.MonthDay(1, 19), FestivalKind.JASHN,
            "جشن فروهرها؛ یاد درگذشتگان",
            "روز فروردین از ماه فروردین، روزِ فروهرهاست. ایرانیان برای روان درگذشتگان " +
                "آش و نان و شادی می‌فرستند و یاد آنان را گرامی می‌دارند."
        ),
        Festival(
            "ordibeheshtgan", "اردیبهشتگان", FestivalRule.MonthDay(2, 3), FestivalKind.JASHN,
            "جشن آتش و راستی",
            "روز اردیبهشت از ماه اردیبهشت، جشن «بهترین راستی» است؛ آتش می‌افروزند و " +
                "آتشگاه‌ها را می‌آرایند، زیرا آتش پاسدار راستی دانسته می‌شود."
        ),
        Festival(
            "gahambar-maidyozarem", "میدیوزرم", FestivalRule.MonthDay(2, 15), FestivalKind.GAHAMBAR,
            "گاهنبار میانهٔ بهار؛ آفرینش آسمان",
            "نخستین گاهنبار سال در میانهٔ بهار برگزار می‌شود و به آفرینش آسمان " +
                "اختصاص دارد؛ روزی برای نیایش، همیاری و بخشیدن خوراک به نیازمندان."
        ),
        Festival(
            "khordadgan", "خردادگان", FestivalRule.MonthDay(3, 6), FestivalKind.JASHN,
            "جشن رسایی و آب",
            "روز خرداد از ماه خرداد، جشن کمال و شادابی است؛ آب را می‌ستایند و " +
                "از چشمه‌ها و کاریزها سپاس می‌گزارند."
        ),

        // ─────────── تابستان
        Festival(
            "tiregan", "تیرگان", FestivalRule.MonthDay(4, 13), FestivalKind.JASHN,
            "جشن باران و آب‌پاشی؛ یاد آرش کمانگیر",
            "روز تیر از ماه تیر، جشن تیشتر (ستارهٔ باران) است؛ آب بر یکدیگر " +
                "می‌پاشند و یاد آرش کمانگیر را زنده می‌دارند که با یک تیر مرز ایران را بازستاند."
        ),
        Festival(
            "gahambar-maidyoshem", "میدیوشم", FestivalRule.MonthDay(4, 15), FestivalKind.GAHAMBAR,
            "گاهنبار میانهٔ تابستان؛ آفرینش آب",
            "دومین گاهنبار در گرمای تابستان برگزار می‌شود و به آفرینش آب اختصاص دارد؛ " +
                "هنگام شکرگزاری از آب و پاک‌داشتن آن."
        ),
        Festival(
            "amordadgan", "امردادگان", FestivalRule.MonthDay(5, 7), FestivalKind.JASHN,
            "جشن بی‌مرگی و گیاهان",
            "روز امرداد از ماه امرداد، جشن گیاه و بی‌مرگی است؛ باغ‌ها و کشتزارها " +
                "گرامی داشته می‌شوند و از سبزی و رویش سپاسگزاری می‌شود."
        ),
        Festival(
            "shahrivargan", "شهریورگان", FestivalRule.MonthDay(6, 4), FestivalKind.JASHN,
            "جشن شهریاری و فلز",
            "روز شهریور از ماه شهریور، جشن شهریاری آرمانی و فلز است؛ روزی برای " +
                "پاسداشت داد و آبادانی."
        ),
        Festival(
            "gahambar-paitishehim", "پتیه‌شهیم", FestivalRule.MonthDay(6, 30), FestivalKind.GAHAMBAR,
            "گاهنبار پایان تابستان؛ آفرینش زمین",
            "سومین گاهنبار در آخرین روز تابستان برگزار می‌شود و به آفرینش زمین " +
                "اختصاص دارد؛ هنگام آماده‌شدن برای برداشت و سپاس از خاک."
        ),

        // ─────────── پاییز
        Festival(
            "mehregan", "مهرگان", FestivalRule.MonthDay(7, 16), FestivalKind.JASHN,
            "جشن پیمان و دوستی؛ بزرگ‌ترین جشن پس از نوروز",
            "روز مهر از ماه مهر، جشن پیمان، مهر و برداشت است. در ایران باستان " +
                "مهرگان را همچون نوروز با بار عام و هدیه جشن می‌گرفتند؛ روزی برای " +
                "آشتی، دوستی و سپاس از خرمن."
        ),
        Festival(
            "gahambar-ayathrem", "ایاثرم", FestivalRule.MonthDay(8, 1), FestivalKind.GAHAMBAR,
            "گاهنبار آغاز پاییز؛ آفرینش گیاه",
            "چهارمین گاهنبار در آغاز پاییز برگزار می‌شود و به آفرینش گیاه اختصاص دارد؛ " +
                "هنگام گردآوردن خرمن و شکرگزاری."
        ),
        Festival(
            "abanagan", "آبانگان", FestivalRule.MonthDay(8, 10), FestivalKind.JASHN,
            "جشن آب‌ها و پاسداشت آناهیتا",
            "روز آبان از ماه آبان، جشن آناهیتا ایزد آب‌هاست؛ به کنار رود و چشمه " +
                "می‌روند، آب را می‌ستایند و برای باران و فراوانی دعا می‌کنند."
        ),
        Festival(
            "azargan", "آذرگان", FestivalRule.MonthDay(9, 9), FestivalKind.JASHN,
            "جشن آتش و پاسداشت آتشگاه",
            "روز آذر از ماه آذر، جشن آتش است؛ آتشگاه‌ها را می‌آرایند و آتش را " +
                "نماد پاکی و راستی می‌دانند."
        ),
        Festival(
            "deygan", "دیگان", FestivalRule.MonthDay(10, 8), FestivalKind.JASHN,
            "جشن دی؛ داد و آفرینش",
            "نخستین دیگان در روز «دی به آذر» از ماه دی برگزار می‌شود و به دادار " +
                "(آفریدگار) اختصاص دارد؛ روزی برای دادخواهی و آشتی."
        ),
        Festival(
            "deygan-2", "دیگان دوم", FestivalRule.MonthDay(10, 15), FestivalKind.JASHN,
            "روز «دی به مهر» از ماه دی",
            "دومین دیگان در روز دی به مهر برگزار می‌شود؛ جشنی برای پیمان و " +
                "پاسداشت مهر و دوستی در دل زمستان."
        ),
        Festival(
            "gahambar-maidhyairya", "میدیاریم", FestivalRule.MonthDay(10, 20), FestivalKind.GAHAMBAR,
            "گاهنبار میانهٔ زمستان؛ آفرینش جانور",
            "پنجمین گاهنبار در میانهٔ زمستان برگزار می‌شود و به آفرینش جانوران " +
                "اختصاص دارد؛ روزی برای مهر به چهارپایان و پرندگان."
        ),
        Festival(
            "deygan-3", "دیگان سوم", FestivalRule.MonthDay(10, 23), FestivalKind.JASHN,
            "روز «دی به دین» از ماه دی",
            "سومین دیگان در روز دی به دین برگزار می‌شود و به دین و آگاهی " +
                "اختصاص دارد."
        ),

        // ─────────── زمستان
        Festival(
            "bahmanagan", "بهمنگان", FestivalRule.MonthDay(11, 2), FestivalKind.JASHN,
            "جشن اندیشهٔ نیک و گیاهان",
            "روز بهمن از ماه بهمن، جشن بهمن امشاسپند (اندیشهٔ نیک) است؛ " +
                "روزی برای پاسداشت جانوران سودمند و گیاهان."
        ),
        Festival(
            "sadeh", "سده", FestivalRule.MonthDay(11, 10), FestivalKind.JASHN,
            "جشن آتش سده؛ پنجاه روز پیش از نوروز",
            "در روز آبان از ماه بهمن، آتش بزرگی می‌افروزند و گرد آن می‌رقصند. " +
                "سده جشن پیروزی روشنایی و گرما بر تاریکی و سرماست و یادآور " +
                "آماده‌شدن برای بهار."
        ),
        Festival(
            "sepandarmazgan", "سپندارمذگان", FestivalRule.MonthDay(12, 5), FestivalKind.JASHN,
            "جشن زمین و مهر ایرانی",
            "روز سپندارمذ از ماه سپندارمذ، جشن زمین، فروتنی و مهر است؛ " +
                "روزی برای گرامیداشت زنان و مادران و بوسیدن خاک."
        ),
        Festival(
            "panjeh", "پنجهٔ وهیک", FestivalRule.Panjeh, FestivalKind.AYIN,
            "پنج روز گاتها؛ پایان سال و آماده‌شدن برای نوروز",
            "پنج روز پایانی سال به گات‌های زرتشت و یاد فروهرها اختصاص دارد. " +
                "خانه‌ها پاکیزه می‌شوند، برای درگذشتگان خوراک و نیایش می‌فرستند و " +
                "سال کهنه را بدرود می‌گویند."
        ),
        Festival(
            "chaharshanbe-suri", "چهارشنبه‌سوری", FestivalRule.LastWednesday, FestivalKind.AYIN,
            "آخرین چهارشنبهٔ سال؛ آتش و پاک‌سازی",
            "شامگاه آخرین چهارشنبهٔ سال، آتش می‌افروزند و از آن می‌پرند تا " +
                "سرخی آتش را به سرما و زردی بیماری بسپارند؛ آیینی کهن برای " +
                "پاک‌شدن و شادی پیش از نوروز."
        ),
        Festival(
            "yalda", "شب یلدا", FestivalRule.WinterSolstice, FestivalKind.JASHN,
            "بلندترین شب سال؛ زایش خورشید",
            "در شب انقلاب زمستانی، بلندترین شب سال را با انار، هندوانه، شعر " +
                "و گردهم‌آمدن به بامداد می‌رسانند؛ جشن امید به روشنایی که " +
                "از فردای آن، روزها بلندتر می‌شود."
        )
    )

    private const val DAY_MILLIS = 86400000L
    private const val TEHRAN_OFFSET_MILLIS = (3.5 * 3600 * 1000).toLong()

    fun byId(id: String): Festival? = ALL.firstOrNull { it.id == id }

    /** جشن‌های یک روز از گاه‌شماری ایرانی */
    fun onDate(date: IranianDate): List<Festival> {
        val epochDay = date.epochDay
        val year = date.year
        return ALL.filter { matches(it, year, epochDay, date) }
    }

    fun onEpochDay(epochDay: Long): List<Festival> =
        onDate(IranianCalendar.fromEpochDay(epochDay))

    private fun matches(festival: Festival, year: Int, epochDay: Long, date: IranianDate): Boolean =
        when (val rule = festival.rule) {
            is FestivalRule.MonthDay -> !date.isPanjeh && date.month == rule.month && date.day == rule.day
            FestivalRule.Panjeh -> date.isPanjeh && date.panjehIndex == 1
            FestivalRule.LastWednesday -> epochDay == lastWednesdayBeforeNowruz(year)
            FestivalRule.WinterSolstice -> epochDay == winterSolsticeDay(year)
        }

    /** روزِ سال برای یک جشن (برای فهرست سالانه) */
    fun dateInYear(festival: Festival, year: Int): Long? = when (val rule = festival.rule) {
        is FestivalRule.MonthDay -> IranianCalendar.toEpochDay(year, rule.month, rule.day)
        FestivalRule.Panjeh -> IranianCalendar.panjehStart(year)
        FestivalRule.LastWednesday -> lastWednesdayBeforeNowruz(year)
        FestivalRule.WinterSolstice -> winterSolsticeDay(year)
    }

    /** فهرست جشن‌های یک سال، به ترتیب روز */
    fun ofYear(year: Int): List<FestivalOccurrence> = ALL
        .mapNotNull { festival -> dateInYear(festival, year)?.let { FestivalOccurrence(festival, it) } }
        .sortedBy { it.epochDay }

    /** جشن‌های یک ماه (۱ تا ۱۲) */
    fun ofMonth(year: Int, month: Int): List<FestivalOccurrence> =
        ofYear(year).filter { IranianCalendar.fromEpochDay(it.epochDay).month == month }

    /** آخرین چهارشنبه پیش از نوروز */
    fun lastWednesdayBeforeNowruz(year: Int): Long {
        var day = IranianCalendar.yearStart(year) - 1
        while (IranianCalendar.weekdayIndex(day) != 3) day--
        return day
    }

    /** روزی از سال که انقلاب زمستانی در آن رخ می‌دهد */
    fun winterSolsticeDay(year: Int): Long {
        val start = IranianCalendar.yearStart(year)
        val end = IranianCalendar.yearStart(year + 1)
        for (gregorianYear in intArrayOf(year + 621, year + 622)) {
            val millis = Astronomy.momentUtcMillis(gregorianYear, 3)
            val day = CivilMath.floorDiv(millis + TEHRAN_OFFSET_MILLIS, DAY_MILLIS)
            if (day in start until end) return day
        }
        // در حالت نامنتظره: میانهٔ ماه دی
        return IranianCalendar.toEpochDay(year, 10, 10)
    }

    /** جشن‌هایی که در روزهای آینده می‌آیند */
    fun upcoming(fromEpochDay: Long, count: Int): List<FestivalOccurrence> {
        val firstYear = IranianCalendar.yearOf(fromEpochDay)
        return (firstYear..firstYear + 1)
            .flatMap { ofYear(it) }
            .filter { it.epochDay >= fromEpochDay }
            .sortedBy { it.epochDay }
            .take(count)
    }
}
