package ir.gahshomar.parsi.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ir.gahshomar.parsi.data.FestivalKind
import ir.gahshomar.parsi.data.FestivalRule
import ir.gahshomar.parsi.data.Festivals

/**
 * آزمون‌های گاه‌شماری ایرانی و جشن‌های آن.
 * نوروزها با تقویم رسمی ایران (مرکز تقویم مؤسسهٔ ژئوفیزیک) تطبیق داده شده‌اند.
 */
class IranianCalendarTest {

    private val official = mapOf(
        1395 to "2016-03-20",
        1396 to "2017-03-21",
        1397 to "2018-03-21",
        1398 to "2019-03-21",
        1399 to "2020-03-20",
        1400 to "2021-03-21",
        1401 to "2022-03-21",
        1402 to "2023-03-21",
        1403 to "2024-03-20",
        1404 to "2025-03-21",
        1405 to "2026-03-21",
        1406 to "2027-03-21",
        1407 to "2028-03-20"
    )

    private fun gregorian(epochDay: Long): String {
        val date = CivilMath.epochDayToGregorian(epochDay)
        return "%04d-%02d-%02d".format(date.year, date.month, date.day)
    }

    @Test
    fun nowruzMatchesOfficialIranianDates() {
        for ((year, expected) in official) {
            assertEquals("نوروز $year", expected, gregorian(IranianCalendar.yearStart(year)))
        }
    }

    @Test
    fun yearLengthsAreFiveOrSixDayPanjeh() {
        assertEquals(365, IranianCalendar.yearLength(1404))
        assertEquals(366, IranianCalendar.yearLength(1403))
        assertEquals(5, IranianCalendar.panjehLength(1404))
        assertEquals(6, IranianCalendar.panjehLength(1403))
        assertTrue(IranianCalendar.isLeapYear(1399))
        assertTrue(IranianCalendar.isLeapYear(1403))
        assertFalse(IranianCalendar.isLeapYear(1400))
        assertFalse(IranianCalendar.isLeapYear(1404))
    }

    @Test
    fun everyMonthHasThirtyNamedDays() {
        for (month in 1..12) {
            assertEquals(30, IranianCalendar.MONTH_DAYS)
            assertTrue(IranianCalendar.monthName(month).isNotBlank())
        }
        assertEquals(30, IranianCalendar.DAY_NAMES.size)
        assertEquals(30, IranianCalendar.DAY_MEANINGS.size)
        assertEquals("اورمزد", IranianCalendar.dayName(1))
        assertEquals("بهرام", IranianCalendar.dayName(20))
        assertEquals("انارام", IranianCalendar.dayName(30))
        assertEquals("سپندارمذ", IranianCalendar.monthName(12))
    }

    @Test
    fun monthsAreThirtyDaysAndPanjehFollows() {
        for (year in intArrayOf(1403, 1404)) {
            for (month in 1..12) {
                val start = IranianCalendar.monthStart(year, month)
                val next = if (month == 12) IranianCalendar.panjehStart(year)
                else IranianCalendar.monthStart(year, month + 1)
                assertEquals("ماه $month سال $year", 30L, next - start)
            }
            assertEquals(
                IranianCalendar.yearStart(year) + 360L,
                IranianCalendar.panjehStart(year)
            )
        }
    }

    @Test
    fun roundTripEveryDay() {
        var epochDay = IranianCalendar.yearStart(1300)
        val end = IranianCalendar.yearStart(1500)
        var failures = 0
        while (epochDay < end) {
            val date = IranianCalendar.fromEpochDay(epochDay)
            when {
                date.isPanjeh -> {
                    val expected = IranianCalendar.panjehStart(date.year) + date.panjehIndex - 1
                    if (expected != epochDay) failures++
                }
                IranianCalendar.toEpochDay(date) != epochDay -> failures++
            }
            epochDay++
        }
        assertEquals("شمار روزهایی که رفت‌وبرگشت‌شان نادرست بود", 0, failures)
    }

    @Test
    fun knownDatesAndWeekdays() {
        val nowruz1404 = IranianCalendar.fromEpochDay(IranianCalendar.yearStart(1404))
        assertEquals(IranianDate(1404, 1, 1), nowruz1404)
        assertEquals("2025-03-21", gregorian(nowruz1404.epochDay))
        assertEquals(6, IranianCalendar.weekdayIndex(nowruz1404.epochDay)) // آدینه
        assertEquals("آدینه", IranianCalendar.weekdayName(nowruz1404.epochDay))

        val lastOf1403 = IranianCalendar.fromEpochDay(IranianCalendar.yearStart(1404) - 1)
        assertEquals("2025-03-20", gregorian(lastOf1403.epochDay))
        assertEquals("وهیشتواشت دوم", lastOf1403.dayName)

        val mehregan = IranianCalendar.fromEpochDay(IranianCalendar.toEpochDay(1404, 7, 16))
        assertEquals("مهر", mehregan.dayName)
        assertEquals("2025-10-08", gregorian(mehregan.epochDay))

        assertEquals(
            "2025-10-02",
            gregorian(IranianCalendar.toEpochDay(1404, 7, 10))
        )
    }

    @Test
    fun allFestivalsHaveDistinctIdsAndValidRules() {
        val ids = Festivals.ALL.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
        assertTrue(Festivals.ALL.size >= 20)
        val monthDayCount = Festivals.ALL.count { it.rule is FestivalRule.MonthDay }
        assertTrue(monthDayCount >= 18)
        assertNotNull(Festivals.byId("nowruz"))
        assertEquals(null, Festivals.byId("nope"))
        Festivals.ALL.forEach {
            assertTrue("${it.id} summary", it.summary.isNotBlank())
            assertTrue("${it.id} detail", it.detail.isNotBlank())
        }
        assertEquals(5, Festivals.ALL.count { it.kind == FestivalKind.GAHAMBAR })
        assertTrue(Festivals.ALL.any { it.rule == FestivalRule.Panjeh })
    }

    @Test
    fun nameDayFestivalsFallOnMatchingDays() {
        // هر جشنِ روزْ‌ثابت باید در همان روز و ماه باشد
        for (festival in Festivals.ALL) {
            val rule = festival.rule
            if (rule !is FestivalRule.MonthDay) continue
            val epochDay = Festivals.dateInYear(festival, 1404)
            assertNotNull(epochDay)
            val date = IranianCalendar.fromEpochDay(epochDay!!)
            assertEquals("${festival.id} ماه", rule.month, date.month)
            assertEquals("${festival.id} روز", rule.day, date.day)
            assertTrue("${festival.id} باید در فهرست روز باشد", Festivals.onEpochDay(epochDay).any { it.id == festival.id })
        }
    }

    @Test
    fun mehreganAndTireganAreOnTheirNamedDays() {
        val mehregan = Festivals.onDate(IranianDate(1404, 7, 16))
        assertEquals(listOf("mehregan"), mehregan.map { it.id })

        val tiregan = Festivals.onDate(IranianDate(1404, 4, 13))
        assertEquals(listOf("tiregan"), tiregan.map { it.id })

        val nowruz = Festivals.onDate(IranianDate(1404, 1, 1))
        assertTrue(nowruz.any { it.id == "nowruz" })
    }

    @Test
    fun chaharshanbeSuriIsTheLastTuesdayEveningBeforeNowruz() {
        for (year in 1398..1410) {
            val suri = Festivals.lastWednesdayBeforeNowruz(year)
            assertTrue(suri < IranianCalendar.yearStart(year))
            assertTrue(suri > IranianCalendar.yearStart(year) - 10)
            assertEquals(3, IranianCalendar.weekdayIndex(suri)) // چهارشنبه
            assertTrue(Festivals.onEpochDay(suri).any { it.id == "chaharshanbe-suri" })
        }
        assertEquals(
            "2025-03-18",
            gregorian(Festivals.lastWednesdayBeforeNowruz(1404))
        )
    }

    @Test
    fun yaldaFallsOnWinterSolsticeDay() {
        val yalda = Festivals.dateInYear(Festivals.byId("yalda")!!, 1404)
        assertNotNull(yalda)
        val date = IranianCalendar.fromEpochDay(yalda!!)
        assertTrue("یلدا باید در آذر یا دی باشد", date.month in intArrayOf(9, 10))
        assertTrue(Festivals.onEpochDay(yalda).any { it.id == "yalda" })
        // انقلاب زمستانی ۲۰۲۵ در ۲۱ دسامبر
        assertEquals("2025-12-21", gregorian(yalda))
    }

    @Test
    fun panjehIsAnnouncedOncePerYear() {
        val panjehStart = IranianCalendar.panjehStart(1404)
        val first = Festivals.onEpochDay(panjehStart)
        assertTrue(first.any { it.id == "panjeh" })
        val date = IranianCalendar.fromEpochDay(panjehStart)
        assertTrue(date.isPanjeh)
        assertEquals(1, date.panjehIndex)
        assertEquals("اهونود", date.dayName)
        assertEquals("روز اهونود از پنجهٔ وهیک", date.ritualLabel)
        assertFalse(Festivals.onEpochDay(panjehStart + 1).any { it.id == "panjeh" })
    }

    @Test
    fun yearlyFestivalListIsOrderedAndUpcomingWorks() {
        val list = Festivals.ofYear(1404)
        assertEquals(list.size, list.sortedBy { it.epochDay }.size)
        val days = list.map { it.epochDay }
        assertEquals(days, days.sorted())
        list.forEach {
            assertTrue(it.epochDay >= IranianCalendar.yearStart(1404))
            assertTrue(it.epochDay < IranianCalendar.yearStart(1405))
        }
        val upcoming = Festivals.upcoming(IranianCalendar.yearStart(1404), 3)
        assertEquals(3, upcoming.size)
        assertTrue(upcoming.first().epochDay >= IranianCalendar.yearStart(1404))
        assertTrue(upcoming.first().festival.id == "nowruz")
    }

    @Test
    fun monthListingFindsEachMonthFestivals() {
        val mehregan = Festivals.ofMonth(1404, 7)
        assertTrue(mehregan.any { it.festival.id == "mehregan" })
        val farvardin = Festivals.ofMonth(1404, 1)
        assertEquals(
            listOf("nowruz", "khordadruz", "sizdah-bedar", "farvardigan"),
            farvardin.map { it.festival.id }
        )
    }

    @Test
    fun persianTextFormatsDatesAndDays() {
        val date = IranianCalendar.fromEpochDay(IranianCalendar.yearStart(1404))
        assertEquals("۱ فروردین ۱۴۰۴", PersianText.formatDate(date))
        assertEquals("۱ فروردین ۱۴۰۴، آدینه", PersianText.formatDateWithWeekday(date))
        assertEquals("روز اورمزد از فروردین ماه", PersianText.ritualLabel(date))
        assertEquals("امروز", PersianText.daysAway(0))
        assertEquals("۳ روز دیگر", PersianText.daysAway(3))
        assertEquals("۲ روز پیش", PersianText.daysAway(-2))
        assertEquals("۱۴۰۴", PersianText.digits(1404))
        assertEquals("بهرام — پیروزی", PersianText.dayWithMeaning(20))
    }

    @Test
    fun gahOfDayCoversWholeDay() {
        val names = (0..23).map { IranianCalendar.gahOfDay(it) }.distinct()
        assertEquals(5, names.size)
        assertEquals("هاون‌گاه", IranianCalendar.gahOfDay(8))
        assertEquals("اشاهین‌گاه", IranianCalendar.gahOfDay(2))
    }
}
