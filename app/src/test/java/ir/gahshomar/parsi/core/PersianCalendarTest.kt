package ir.gahshomar.parsi.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ir.gahshomar.parsi.data.Events
import ir.gahshomar.parsi.data.Timeline

/**
 * آزمون‌های گاه‌شماری خورشیدی.
 * تاریخ‌های نوروز با تقویم رسمی ایران (مرکز تقویم مؤسسهٔ ژئوفیزیک) تطبیق داده شده‌اند.
 */
class PersianCalendarTest {

    @Test
    fun nowruzMatchesOfficialIranianDates() {
        val expected = mapOf(
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
        for ((year, gregorian) in expected) {
            val computed = CivilMath.epochDayToGregorian(PersianCalendar.nowruzEpochDay(year))
            assertEquals("نوروز $year", gregorian, computed.toString())
        }
    }

    @Test
    fun leapYears() {
        assertTrue(PersianCalendar.isLeapYear(1399))
        assertFalse(PersianCalendar.isLeapYear(1400))
        assertTrue(PersianCalendar.isLeapYear(1403))
        assertFalse(PersianCalendar.isLeapYear(1404))
        assertTrue(PersianCalendar.isLeapYear(1408))
        assertEquals(366, PersianCalendar.yearLength(1399))
        assertEquals(365, PersianCalendar.yearLength(1404))
    }

    @Test
    fun monthLengths() {
        assertEquals(31, PersianCalendar.monthLength(1404, 1))
        assertEquals(31, PersianCalendar.monthLength(1404, 6))
        assertEquals(30, PersianCalendar.monthLength(1404, 7))
        assertEquals(30, PersianCalendar.monthLength(1404, 11))
        assertEquals(29, PersianCalendar.monthLength(1404, 12))
        assertEquals(30, PersianCalendar.monthLength(1403, 12))
    }

    @Test
    fun conversionsRoundTrip() {
        val start = PersianCalendar.nowruzEpochDay(1390)
        for (offset in 0 until 5200 step 7) {
            val epoch = start + offset
            val persian = PersianCalendar.fromEpochDay(epoch)
            assertEquals(epoch, PersianCalendar.toEpochDay(persian))
            assertEquals(epoch, CivilMath.gregorianToEpochDay(
                persian.toGregorian().year,
                persian.toGregorian().month,
                persian.toGregorian().day
            ))
        }
    }

    @Test
    fun knownDates() {
        val date = PersianCalendar.fromEpochDay(CivilMath.gregorianToEpochDay(2025, 3, 21))
        assertEquals(PersianDate(1404, 1, 1), date)
        assertEquals("جمعه", PersianCalendar.weekdayName(date.epochDay))
        assertEquals(6, PersianCalendar.weekdayIndex(date.epochDay))

        val second = PersianCalendar.fromEpochDay(CivilMath.gregorianToEpochDay(2025, 10, 2))
        assertEquals(PersianDate(1404, 7, 10), second)

        val lastDay = PersianCalendar.fromEpochDay(CivilMath.gregorianToEpochDay(2025, 3, 20))
        assertEquals(PersianDate(1403, 12, 30), lastDay)
    }

    @Test
    fun dayOfYearAndWeek() {
        val date = PersianDate(1404, 1, 1)
        assertEquals(1, date.dayOfYear)
        assertEquals(1, PersianCalendar.weekOfYear(date))
        val firstOfMehr = PersianDate(1404, 7, 1)
        assertEquals(187, firstOfMehr.dayOfYear)
        assertEquals(365, PersianDate(1404, 12, 29).dayOfYear)
    }

    @Test
    fun chaharshanbeSuriIsTuesdayBeforeNowruz() {
        val suri = Events.chaharshanbeSuriOf(1404)
        val gregorian = CivilMath.epochDayToGregorian(suri)
        assertEquals("2025-03-18", gregorian.toString())
        assertEquals(3, PersianCalendar.weekdayIndex(suri)) // سه‌شنبه
        assertTrue(suri < PersianCalendar.nowruzEpochDay(1404))
        assertTrue(suri > PersianCalendar.nowruzEpochDay(1404) - 8)
    }

    @Test
    fun hijriConversion() {
        val epoch = CivilMath.gregorianToEpochDay(2025, 3, 1)
        val hijri = HijriCalendar.fromEpochDay(epoch)
        assertEquals(1446, hijri.year)
        assertEquals(9, hijri.month)
        assertEquals(1, hijri.day)
        assertEquals(epoch, HijriCalendar.toEpochDay(hijri))
    }

    @Test
    fun persianDigits() {
        assertEquals("۱۴۰۴", PersianText.digits(1404))
        assertEquals("1404", PersianText.digits(1404, false))
        assertEquals("۱۴۰۴", PersianText.convertDigits("1404"))
        assertEquals("۲۱ فروردین ۱۴۰۴", PersianText.formatDate(PersianDate(1404, 1, 21)))
    }

    @Test
    fun eventsAndContentAreAvailable() {
        assertTrue(Events.ALL.size > 40)
        val nowruzEvents = Events.forEpochDay(PersianCalendar.nowruzEpochDay(1404))
        assertTrue(nowruzEvents.any { it.id == "nowruz-1" })
        assertTrue(Timeline.ENTRIES.size > 20)
    }

    @Test
    fun astronomySanity() {
        // اعتدال بهاری ۲۰۲۵ در ۲۰ مارس، ساعت ۹:۰۱ جهانی
        val moment = SolarMoment("", 0, 2025)
        val gregorian = CivilMath.epochDayToGregorian(moment.epochDay)
        assertEquals("2025-03-20", gregorian.toString())
    }
}
