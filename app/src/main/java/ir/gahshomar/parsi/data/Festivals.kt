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
    TAJGOZARI("تاج‌گذاری"),
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
                "اورمزد جهان را آفرید، فروهرهای درگذشتگان به زمین بازمی‌گردند و شاهان " +
                "ایران در این روز تاج بر سر می‌نهادند."
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
        ),

        // ─────────── جشن‌ها و آیین‌های زرتشتی و ایران باستان
        Festival(
            "abangan-farvardin", "جشن آبانگاه", FestivalRule.MonthDay(1, 10), FestivalKind.AYIN,
            "جشن آب در دهم فروردین",
            "روز آبان از ماه فروردین، روز ایزد آناهیتا و آب‌هاست؛ در این روز به چشمه و " +
                "جوی آب می‌روند، آب را می‌ستایند و برای فراوانی باران نیایش می‌کنند."
        ),
        Festival(
            "soroushgan", "جشن سروشگان", FestivalRule.MonthDay(1, 17), FestivalKind.JASHN,
            "جشن سروش، پیام‌آور ایزدی",
            "روز سروش از ماه فروردین؛ سروش ایزد پیام‌آور و پاسدار بیداری است. در این روز " +
                "با نیایش و خواندن اوستا، زبان را به راستی می‌آرایند."
        ),
        Festival(
            "giyahavari", "جشن گیاه‌آوری", FestivalRule.MonthDay(2, 2), FestivalKind.JASHN,
            "آغاز رویش و چیدن گیاهان بهاری",
            "در دوم اردیبهشت، گیاهان بهاری و سبزی‌های صحرایی چیده می‌شود و بر سفره " +
                "می‌آید؛ آیینی برای سپاس از رویش زمین."
        ),
        Festival(
            "kaveh-faridun", "جشن پیروزی کاوه و فریدون", FestivalRule.MonthDay(2, 21), FestivalKind.JASHN,
            "رام‌روز؛ یاد شورش کاوه آهنگر بر ضحاک",
            "روز رام از ماه اردیبهشت؛ بنا بر روایت‌های ایرانی، در این روز کاوه آهنگر " +
                "درفش برافراشت و فریدون ضحاک را فروگرفت. روزی برای پاسداشت داد و آزادی."
        ),
        Festival(
            "ram-rooz-mehregan", "پایان مهرگان، رام‌روز", FestivalRule.MonthDay(7, 21), FestivalKind.JASHN,
            "ششمین روز مهرگان",
            "جشن مهرگان شش روز بود و در روز رام از ماه مهر به پایان می‌رسید؛ ایرانیان " +
                "شش روز نخست مهر را «مهرگان عام» و روز شانزدهم را «مهرگان خاص» می‌دانستند."
        ),
        Festival(
            "miane-payiz", "جشن میانهٔ پاییز", FestivalRule.MonthDay(8, 15), FestivalKind.JASHN,
            "نیمهٔ پاییز و میانهٔ سال ایرانی",
            "پانزدهم آبان، میانهٔ پاییز و نزدیک به میانهٔ سال است؛ روزی برای سپاس از " +
                "خرمن و آماده‌شدن برای زمستان."
        ),
        Festival(
            "azar-jashn", "آذر جشن", FestivalRule.MonthDay(9, 1), FestivalKind.AYIN,
            "آغاز ماه آذر، ماه آتش",
            "نخستین روز آذر؛ ماهِ آتش و آتشگاه. در این روز آتشگاه‌ها را می‌آرایند و " +
                "آتش را که نماد پاکی و راستی است گرامی می‌دارند."
        ),
        Festival(
            "khorram-rooz", "خرم‌روز، نخستین دیگان", FestivalRule.MonthDay(10, 1), FestivalKind.JASHN,
            "روز اورمزد از ماه دی؛ روز برادری و خواهری",
            "روز هرمز از ماه دی، روز و ماه هر دو به نام اورمزد است و آن را «خرم‌روز» " +
                "می‌نامند؛ از گرامی‌ترین روزهای ایران کهن، با نیایش، شادی و برادری."
        ),
        Festival(
            "zartosht-padvash", "سالروز درگذشت زرتشت", FestivalRule.MonthDay(10, 5), FestivalKind.AYIN,
            "یاد پیمبر ایران کهن، بنا بر روایت سنتی",
            "بنا بر روایت زرتشتیان، پنجم دی هنگام درگذشت اشو زرتشت است؛ زرتشتیان در این " +
                "روز به آتشگاه می‌روند، گات‌ها می‌خوانند و یاد او را گرامی می‌دارند."
        ),
        Festival(
            "sirsour", "سیرسور", FestivalRule.MonthDay(10, 14), FestivalKind.AYIN,
            "جشن گیاه‌خواری و راندن دیوان",
            "روز گوش از ماه دی؛ در این روز خوراک‌های پرشیر و سبزی می‌پختند و «سیرسور» " +
                "را جشن می‌گرفتند تا دیوها و بیماری‌ها را از خانه برانند."
        ),
        Festival(
            "daramzian", "جشن درامزینان", FestivalRule.MonthDay(10, 16), FestivalKind.JASHN,
            "جشن درفش‌ها و پیکرتراشی",
            "روز مهر از ماه دی؛ در این روز درفش‌ها و پیکره‌ها را می‌آراستند و جشنی " +
                "برای پاسداشت هنر و نشان‌های ایران برپا می‌شد."
        ),
        Festival(
            "vakhshnokam", "جشن وخشنکام", FestivalRule.MonthDay(12, 10), FestivalKind.AYIN,
            "دهم سپندارمذ؛ جشن دهم‌روز",
            "روز آبان از ماه سپندارمذ؛ جشنی کهن برای شادکامی و آشتی پیش از پایان سال."
        ),
        Festival(
            "norooz-roodha", "جشن نوروز رودها", FestivalRule.MonthDay(12, 19), FestivalKind.AYIN,
            "پاک‌سازی رودها و کاریزها پیش از نوروز",
            "نوزدهم سپندارمذ؛ روزِ پاک‌کردن رودها، کاریزها و آب‌انبارها پیش از نوروز، " +
                "با این باور که آب روان، خانه و کشور را تازه می‌کند."
        ),

        // ─────────── تاج‌گذاری شاهان ایران
        Festival(
            "jamshid-taj", "تاج‌گذاری جمشید", FestivalRule.MonthDay(1, 1), FestivalKind.TAJGOZARI,
            "نوروز، روز تاج‌گذاری جمشید در روایت ایرانی",
            "در روایت‌های ایرانی، جمشید در نخستین روز فروردین تاج شاهی نهاد و آن روز را " +
                "«نوروز» نامیدند؛ از همین رو نوروز روز تاج‌گذاری و فرمانروایی داد دانسته شده است."
        ),
        Festival(
            "faridun-taj", "تاج‌گذاری فریدون", FestivalRule.MonthDay(7, 16), FestivalKind.TAJGOZARI,
            "مهرگان، روز تاج‌گذاری فریدون در روایت ایرانی",
            "بنا بر روایت‌ها، فریدون پس از فروگرفتن ضحاک در روز مهر از ماه مهر تاج " +
                "شاهی نهاد و مهرگان جشن تاج‌گذاری او شد."
        ),
        Festival(
            "reza-shah-taj", "سالروز تاج‌گذاری رضاشاه", FestivalRule.MonthDay(2, 4), FestivalKind.TAJGOZARI,
            "چهارم اردیبهشت ۱۳۰۵، کاخ گلستان",
            "مراسم تاج‌گذاری رضاشاه پهلوی، بنیان‌گذار دودمان پهلوی، در چهارم اردیبهشت " +
                "۱۳۰۵ در تالار سلام کاخ گلستان برگزار شد."
        ),
        Festival(
            "reza-shah-sowgand", "سوگند پادشاهی رضاشاه", FestivalRule.MonthDay(9, 24), FestivalKind.YADBUD,
            "بیست‌وچهارم آذر ۱۳۰۴، مجلس مؤسسان",
            "رضاشاه در بیست‌وچهارم آذر ۱۳۰۴ در مجلس مؤسسان سوگند پادشاهی یاد کرد و " +
                "رسماً پادشاه ایران شد."
        ),
        Festival(
            "ahmad-shah-taj", "سالروز تاج‌گذاری احمدشاه", FestivalRule.MonthDay(4, 30), FestivalKind.TAJGOZARI,
            "سی‌ام تیر ۱۲۹۳، تهران",
            "احمدشاه قاجار، واپسین شاه قاجار، در سی‌ام تیر ۱۲۹۳ در هجده‌سالگی در تهران " +
                "تاج‌گذاری کرد."
        ),
        Festival(
            "mohammadreza-taj", "سالروز تاج‌گذاری محمدرضاشاه", FestivalRule.MonthDay(8, 4), FestivalKind.TAJGOZARI,
            "چهارم آبان ۱۳۴۶، کاخ گلستان",
            "محمدرضاشاه پهلوی در چهارم آبان ۱۳۴۶، هم‌زمان با زادروزش، تاج شاهی بر سر نهاد."
        ),
        Festival(
            "nader-shah-taj", "سالروز تاج‌گذاری نادرشاه", FestivalRule.MonthDay(12, 18), FestivalKind.TAJGOZARI,
            "هجدهم اسفند ۱۱۱۴، دشت مغان",
            "نادرشاه افشار در هجدهم اسفند ۱۱۱۴ در دشت مغان تاج‌گذاری کرد و دودمان " +
                "افشاریه را بنیاد نهاد."
        ),
        Festival(
            "qajar-payan", "پایان دودمان قاجار", FestivalRule.MonthDay(8, 9), FestivalKind.YADBUD,
            "نهم آبان ۱۳۰۴، مجلس شورای ملی",
            "در نهم آبان ۱۳۰۴ مجلس شورای ملی خلع احمدشاه و پایان دودمان قاجار را " +
                "اعلام کرد."
        ),

        // ─────────── یادبودهای فرهنگی ایران
        Festival(
            "attar", "روز عطار نیشابوری", FestivalRule.MonthDay(1, 25), FestivalKind.YADBUD,
            "پاسداشت سرایندهٔ منطق‌الطیر",
            "بیست‌وپنجم فروردین، روز بزرگداشت فریدالدین عطار نیشابوری، سرایندهٔ " +
                "منطق‌الطیر و الهی‌نامه."
        ),
        Festival(
            "saadi", "روز سعدی", FestivalRule.MonthDay(2, 1), FestivalKind.YADBUD,
            "پاسداشت سرایندهٔ گلستان و بوستان",
            "یکم اردیبهشت، روز بزرگداشت سعدی شیرازی؛ «بنی‌آدم اعضای یک پیکرند»."
        ),
        Festival(
            "ferdowsi", "روز فردوسی و پاسداشت زبان فارسی", FestivalRule.MonthDay(2, 25), FestivalKind.YADBUD,
            "پاسداشت سرایندهٔ شاهنامه",
            "بیست‌وپنجم اردیبهشت، روز بزرگداشت حکیم ابوالقاسم فردوسی و پاسداشت زبان فارسی؛ " +
                "شاهنامه، دفتر پهلوانی و فرهنگ ایران."
        ),
        Festival(
            "khayyam", "روز خیام", FestivalRule.MonthDay(2, 28), FestivalKind.YADBUD,
            "پاسداشت حکیم عمر خیام نیشابوری",
            "بیست‌وهشتم اردیبهشت، روز بزرگداشت خیام؛ ریاضی‌دان، اخترشناس و رباعی‌سرای ایرانی " +
                "که در اصلاح گاه‌شماری نیز نقش داشت."
        ),
        Festival(
            "ibn-sina", "روز ابن‌سینا", FestivalRule.MonthDay(6, 1), FestivalKind.YADBUD,
            "پاسداشت شیخ‌الرئیس، نویسندهٔ قانون",
            "یکم شهریور، روز بزرگداشت ابوعلی سینا؛ پزشک و فیلسوف ایرانی و نویسندهٔ " +
                "«قانون» در پزشکی."
        ),
        Festival(
            "razi", "روز رازی", FestivalRule.MonthDay(6, 5), FestivalKind.YADBUD,
            "پاسداشت زکریای رازی",
            "پنجم شهریور، روز بزرگداشت محمد زکریای رازی؛ پزشک و شیمی‌دان ایرانی و " +
                "کاشف الکل و جوهر گوگرد."
        ),
        Festival(
            "biruni", "روز ابوریحان بیرونی", FestivalRule.MonthDay(6, 13), FestivalKind.YADBUD,
            "پاسداشت اخترشناس و مردم‌شناس بزرگ ایرانی",
            "سیزدهم شهریور، روز بزرگداشت ابوریحان بیرونی؛ اخترشناس، ریاضی‌دان و " +
                "نویسندهٔ «التفهیم» و «آثارالباقیه»."
        ),
        Festival(
            "molana", "روز مولانا", FestivalRule.MonthDay(7, 8), FestivalKind.YADBUD,
            "پاسداشت سرایندهٔ مثنوی",
            "هشتم مهر، روز بزرگداشت جلال‌الدین محمد بلخی، مولانا؛ سرایندهٔ مثنوی معنوی " +
                "و دیوان شمس."
        ),
        Festival(
            "hafez", "روز حافظ", FestivalRule.MonthDay(7, 20), FestivalKind.YADBUD,
            "پاسداشت لسان‌الغیب",
            "بیستم مهر، روز بزرگداشت حافظ شیرازی؛ غزل‌سرای بزرگ ایران و «لسان‌الغیب»."
        ),
        Festival(
            "korosh", "روز کوروش بزرگ", FestivalRule.MonthDay(8, 7), FestivalKind.YADBUD,
            "هفتم آبان؛ یاد ورود کوروش به بابل",
            "هفتم آبان (۲۹ اکتبر) یادآور ورود کوروش بزرگ به بابل در ۵۳۹ پیش از میلاد است؛ " +
                "همان رویدادی که استوانهٔ کوروش از آن برجای مانده و بنیان‌گذار شاهنشاهی " +
                "هخامنشی را می‌ستاید."
        ),
        Festival(
            "nezami", "روز نظامی گنجوی", FestivalRule.MonthDay(12, 21), FestivalKind.YADBUD,
            "پاسداشت سرایندهٔ خمسه",
            "بیست‌ویکم سپندارمذ، روز بزرگداشت نظامی گنجوی؛ سرایندهٔ خمسه و " +
                "«خسرو و شیرین» و «لیلی و مجنون»."
        ),
        Festival(
            "shahname-payan", "پایان سرایش شاهنامه", FestivalRule.MonthDay(12, 25), FestivalKind.YADBUD,
            "بیست‌وپنجم سپندارمذ؛ روز به‌پایان‌رسیدن شاهنامه",
            "بنا بر روایت سنتی، سرایش شاهنامه در بیست‌وپنجم سپندارمذ به پایان رسید؛ " +
                "روزی برای پاسداشت فردوسی و زبان پارسی."
        ),
    )

    private const val DAY_MILLIS = 86400000L
    private const val TEHRAN_OFFSET_MILLIS = (3.5 * 3600 * 1000).toLong()

    private val yearCache = HashMap<Int, List<FestivalOccurrence>>()
    private val solsticeCache = HashMap<Int, Long>()

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
            is FestivalRule.MonthDay -> date.month == rule.month && date.day == rule.day
            FestivalRule.Panjeh -> date.isPanjeh && date.panjehIndex == 1
            FestivalRule.LastWednesday -> epochDay == lastWednesdayBeforeNowruz(year + 1)
            FestivalRule.WinterSolstice -> epochDay == winterSolsticeDay(year)
        }

    /** روزِ سال برای یک جشن (برای فهرست سالانه) */
    fun dateInYear(festival: Festival, year: Int): Long? = when (val rule = festival.rule) {
        is FestivalRule.MonthDay -> IranianCalendar.toEpochDay(year, rule.month, rule.day)
        FestivalRule.Panjeh -> IranianCalendar.panjehStart(year)
        FestivalRule.LastWednesday -> lastWednesdayBeforeNowruz(year + 1)
        FestivalRule.WinterSolstice -> winterSolsticeDay(year)
    }

    /** فهرست جشن‌های یک سال، به ترتیب روز (یک بار حساب و نگه داشته می‌شود) */
    fun ofYear(year: Int): List<FestivalOccurrence> {
        yearCache[year]?.let { return it }
        val list = ALL
            .mapNotNull { festival -> dateInYear(festival, year)?.let { FestivalOccurrence(festival, it) } }
            .sortedBy { it.epochDay }
        yearCache[year] = list
        return list
    }

    /** جشن‌های یک ماه (۱ تا ۱۲) */
    fun ofMonth(year: Int, month: Int): List<FestivalOccurrence> =
        ofYear(year).filter { IranianCalendar.fromEpochDay(it.epochDay).month == month }

    /**
     * شامگاه آخرین سه‌شنبه پیش از نوروز [year] — «چهارشنبه‌سوری».
     * این آیین در پایان سالِ پیشین برگزار می‌شود.
     */
    fun lastWednesdayBeforeNowruz(year: Int): Long {
        var day = IranianCalendar.yearStart(year) - 1
        while (IranianCalendar.weekdayIndex(day) != 3) day--
        return day
    }

    /** روزی از سال که انقلاب زمستانی در آن رخ می‌دهد */
    fun winterSolsticeDay(year: Int): Long {
        solsticeCache[year]?.let { return it }
        val start = IranianCalendar.yearStart(year)
        val end = IranianCalendar.yearStart(year + 1)
        for (gregorianYear in intArrayOf(year + 621, year + 622)) {
            val millis = Astronomy.momentUtcMillis(gregorianYear, 3)
            val day = CivilMath.floorDiv(millis + TEHRAN_OFFSET_MILLIS, DAY_MILLIS)
            if (day in start until end) {
                solsticeCache[year] = day
                return day
            }
        }
        // در حالت نامنتظره: میانهٔ ماه دی
        val fallback = IranianCalendar.toEpochDay(year, 10, 10)
        solsticeCache[year] = fallback
        return fallback
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
