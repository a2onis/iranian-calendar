package ir.gahshomar.parsi.data

import ir.gahshomar.parsi.core.CivilMath
import ir.gahshomar.parsi.core.PersianCalendar
import ir.gahshomar.parsi.core.PersianDate
import ir.gahshomar.parsi.core.SolarMoment

/** دستهٔ مناسبت‌ها */
enum class EventCategory(val label: String, val emoji: String) {
    NOWRUZ("نوروز", "🌱"),
    FESTIVAL("جشن و آیین", "🎊"),
    ZOROASTRIAN("زرتشتی", "🔥"),
    ANCIENT("ایران باستان", "🏛"),
    LITERATURE("ادب و اندیشه", "📜"),
    CULTURE("فرهنگ و هنر", "🎨"),
    NATURE("طبیعت و کیهان", "☀"),
    MEMORIAL("بزرگداشت", "🕯")
}

enum class Observance(val label: String) {
    PUBLIC("تعطیل رسمی"),
    TRADITIONAL("آیین کهن"),
    NONE("")
}

sealed class DateRule {
    /** تاریخ ثابت در گاه‌شماری خورشیدی */
    data class Solar(val month: Int, val day: Int) : DateRule()

    /** تاریخ ثابت در گاه‌شماری میلادی */
    data class Western(val month: Int, val day: Int) : DateRule()

    /** شب آخرین سه‌شنبهٔ پیش از نوروز */
    object ChaharshanbeSuri : DateRule()

    /** روزِ اعتدال یا انقلاب (۰ بهار، ۱ تابستان، ۲ پاییز، ۳ زمستان) */
    data class SolarMomentRule(val quarter: Int) : DateRule()
}

data class Event(
    val id: String,
    val title: String,
    val rule: DateRule,
    val category: EventCategory,
    val observance: Observance,
    val summary: String,
    val detail: String = "",
    val articleId: String? = null
)

/** نمایهٔ مناسبت‌ها: یافتن رویدادهای هر روز */
object Events {

    val ALL: List<Event> = EventData.build()

    private val byId: Map<String, Event> = ALL.associateBy { it.id }

    fun byId(id: String): Event? = byId[id]

    /** چهارشنبه‌سوریِ نزدیک به یک نوروز (شبِ آخرین سه‌شنبه پیش از نوروز) */
    fun chaharshanbeSuriOf(persianYear: Int): Long {
        var day = PersianCalendar.nowruzEpochDay(persianYear) - 1
        while (PersianCalendar.weekdayIndex(day) != 3) day--
        return day
    }

    private val momentDaysCache = HashMap<Int, LongArray>()

    /** روزهای اعتدالین و انقلابین یک سال میلادی (به وقت تهران) */
    fun momentDays(gregorianYear: Int): LongArray =
        momentDaysCache.getOrPut(gregorianYear) {
            LongArray(4) { quarter -> SolarMoment("", quarter, gregorianYear).epochDay }
        }

    fun forEpochDay(epochDay: Long, withAstronomy: Boolean = true): List<Event> {
        val date = PersianCalendar.fromEpochDay(epochDay)
        val gregorian = CivilMath.epochDayToGregorian(epochDay)
        var moments: LongArray? = null

        return ALL.filter { event ->
            when (val rule = event.rule) {
                is DateRule.Solar ->
                    date.month == rule.month && date.day == rule.day
                is DateRule.Western ->
                    gregorian.month == rule.month && gregorian.day == rule.day
                DateRule.ChaharshanbeSuri ->
                    chaharshanbeSuriOf(date.year + 1) == epochDay
                is DateRule.SolarMomentRule -> {
                    if (!withAstronomy) false else {
                        if (moments == null) moments = momentDays(gregorian.year)
                        moments!![rule.quarter] == epochDay
                    }
                }
            }
        }
    }

    /** همهٔ رویدادهای یک ماه خورشیدی؛ کلید = شمارهٔ روز ماه */
    fun forSolarMonth(year: Int, month: Int, withAstronomy: Boolean = true): Map<Int, List<Event>> {
        val result = LinkedHashMap<Int, MutableList<Event>>()
        val length = PersianCalendar.monthLength(year, month)
        for (day in 1..length) {
            val epochDay = PersianCalendar.toEpochDay(year, month, day)
            val list = forEpochDay(epochDay, withAstronomy)
            if (list.isNotEmpty()) result[day] = list.toMutableList()
        }
        return result
    }

    /** رویدادهای پیشِ رو از یک روز به بعد */
    fun upcoming(fromEpochDay: Long, count: Int, withAstronomy: Boolean = true): List<Occurrence> {
        val list = ArrayList<Occurrence>(count)
        var day = fromEpochDay
        var guard = 0
        while (list.size < count && guard < 800) {
            for (event in forEpochDay(day, withAstronomy)) {
                list.add(Occurrence(event, day))
                if (list.size >= count) break
            }
            day++
            guard++
        }
        return list.sortedBy { it.epochDay }
    }

    /** رویدادهای یک ماه خورشیدی به‌صورت فهرست مرتب */
    fun monthOccurrences(year: Int, month: Int, withAstronomy: Boolean = true): List<Occurrence> {
        val occurrences = ArrayList<Occurrence>()
        val length = PersianCalendar.monthLength(year, month)
        for (day in 1..length) {
            val epochDay = PersianCalendar.toEpochDay(year, month, day)
            for (event in forEpochDay(epochDay, withAstronomy)) {
                occurrences.add(Occurrence(event, epochDay))
            }
        }
        return occurrences
    }

    /** تاریخ رخداد یک مناسبت در یک سال خورشیدی مشخص (اگر در آن سال رخ دهد) */
    fun occurrenceInYear(event: Event, persianYear: Int): Long? {
        val yearStart = PersianCalendar.nowruzEpochDay(persianYear)
        val yearEnd = PersianCalendar.nowruzEpochDay(persianYear + 1)
        return when (val rule = event.rule) {
            is DateRule.Solar ->
                if (rule.day <= PersianCalendar.monthLength(persianYear, rule.month))
                    PersianCalendar.toEpochDay(persianYear, rule.month, rule.day) else null
            is DateRule.Western -> {
                val firstGregorian = CivilMath.epochDayToGregorian(yearStart).year
                val lastGregorian = CivilMath.epochDayToGregorian(yearEnd - 1).year
                var found: Long? = null
                for (gy in firstGregorian..lastGregorian) {
                    val epoch = CivilMath.gregorianToEpochDay(gy, rule.month, rule.day)
                    if (epoch in yearStart until yearEnd) {
                        found = epoch
                        break
                    }
                }
                found
            }
            DateRule.ChaharshanbeSuri -> chaharshanbeSuriOf(persianYear + 1)
            is DateRule.SolarMomentRule -> {
                val firstGregorian = CivilMath.epochDayToGregorian(yearStart).year
                val lastGregorian = CivilMath.epochDayToGregorian(yearEnd - 1).year
                var found: Long? = null
                for (gy in firstGregorian..lastGregorian) {
                    val day = momentDays(gy)[rule.quarter]
                    if (day in yearStart until yearEnd) {
                        found = day
                        break
                    }
                }
                found
            }
        }
    }

    /** فهرست مناسبت‌های یک سال خورشیدی، مرتب بر پایهٔ تاریخ */
    fun yearOccurrences(persianYear: Int): List<Occurrence> =
        ALL.mapNotNull { event ->
            occurrenceInYear(event, persianYear)?.let { Occurrence(event, it) }
        }.sortedBy { it.epochDay }

    fun search(query: String): List<Event> {
        if (query.isBlank()) return ALL
        val q = normalize(query)
        return ALL.filter {
            normalize(it.title).contains(q) || normalize(it.summary).contains(q) ||
                normalize(it.detail).contains(q) || normalize(it.category.label).contains(q)
        }
    }

    private fun normalize(text: String): String = text
        .replace('ي', 'ی')
        .replace('ك', 'ک')
        .replace("\u200c", "")
        .replace(" ", "")
        .lowercase()
}

data class Occurrence(val event: Event, val epochDay: Long) {
    val persianDate: PersianDate get() = PersianCalendar.fromEpochDay(epochDay)
    val daysFromToday: Long get() = epochDay - PersianCalendar.todayEpochDay()
}
