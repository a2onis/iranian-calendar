package ir.gahshomar.parsi.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ir.gahshomar.parsi.core.HijriCalendar
import ir.gahshomar.parsi.core.PersianCalendar
import ir.gahshomar.parsi.core.PersianDate
import ir.gahshomar.parsi.core.PersianText
import ir.gahshomar.parsi.notify.Notifications
import org.json.JSONObject

/** صفحه‌های برنامه */
sealed class Screen {
    object Calendar : Screen()
    object Events : Screen()
    object Culture : Screen()
    object Ancient : Screen()
    object Zoroastrian : Screen()
    data class Article(val id: String) : Screen()
    data class Day(val epochDay: Long) : Screen()
    object Settings : Screen()
    object Converter : Screen()
    object About : Screen()
}

enum class CalendarMode(val label: String) {
    SOLAR("خورشیدی"),
    GREGORIAN("میلادی"),
    HIJRI("قمری")
}

enum class ThemeMode(val label: String) {
    SYSTEM("هماهنگ با سامانه"),
    LIGHT("روشن"),
    DARK("تیره")
}

class AppState(val context: Context) {

    private val prefs = context.getSharedPreferences("gahshomar_prefs", Context.MODE_PRIVATE)

    var backStack by mutableStateOf<List<Screen>>(listOf(Screen.Calendar))
        private set

    val currentScreen: Screen get() = backStack.last()

    var todayEpochDay by mutableStateOf(PersianCalendar.todayEpochDay())
        private set

    var selectedEpochDay by mutableStateOf(PersianCalendar.todayEpochDay())

    /** سال و ماه خورشیدی نمایش‌داده‌شده در تقویم */
    var viewYear by mutableStateOf(PersianCalendar.today().year)
        private set
    var viewMonth by mutableStateOf(PersianCalendar.today().month)
        private set

    var calendarMode by mutableStateOf(CalendarMode.SOLAR)

    /** ماهِ نمایش‌داده‌شده در گاه‌شماری‌های میلادی و قمری */
    var viewGYear by mutableStateOf(PersianCalendar.today().toGregorian().year)
        private set
    var viewGMonth by mutableStateOf(PersianCalendar.today().toGregorian().month)
        private set
    var viewHYear by mutableStateOf(HijriCalendar.fromEpochDay(PersianCalendar.todayEpochDay()).year)
        private set
    var viewHMonth by mutableStateOf(HijriCalendar.fromEpochDay(PersianCalendar.todayEpochDay()).month)
        private set

    // ───────────────────────────────────────── تنظیمات
    var themeMode by mutableStateOf(
        runCatching { ThemeMode.valueOf(prefs.getString("theme", "SYSTEM")!!) }.getOrDefault(ThemeMode.SYSTEM)
    )
        private set

    var persianDigits by mutableStateOf(prefs.getBoolean("persian_digits", true))
        private set

    var weekStartsSaturday by mutableStateOf(prefs.getBoolean("week_saturday", true))
        private set

    var hijriOffset by mutableStateOf(prefs.getInt("hijri_offset", 0))
        private set

    var showGregorianNumbers by mutableStateOf(prefs.getBoolean("show_gregorian", true))
        private set

    var showZoroastrianDay by mutableStateOf(prefs.getBoolean("show_zoroastrian", true))
        private set

    var showAstronomy by mutableStateOf(prefs.getBoolean("show_astronomy", true))
        private set

    var notificationsEnabled by mutableStateOf(prefs.getBoolean("notifications", false))
        private set

    var notifyHour by mutableStateOf(prefs.getInt("notify_hour", 8))
        private set

    var notifyMinute by mutableStateOf(prefs.getInt("notify_minute", 0))
        private set

    var eventsOnly by mutableStateOf(prefs.getBoolean("events_only", false))
        private set

    /** فهرست نشان‌شده‌ها (شناسهٔ مناسبت) */
    var bookmarks by mutableStateOf(prefs.getStringSet("bookmarks", emptySet())!!.toSet())
        private set

    /** یادداشت‌های روزانه: کلید = شمارهٔ روز */
    var notes by mutableStateOf(loadNotes())
        private set

    fun refreshToday() {
        todayEpochDay = PersianCalendar.todayEpochDay()
        val date = PersianCalendar.fromEpochDay(todayEpochDay)
        if (viewYear < date.year - 1 || viewYear > date.year + 1) {
            viewYear = date.year
            viewMonth = date.month
        }
    }

    fun currentDate(): PersianDate = PersianCalendar.fromEpochDay(todayEpochDay)

    // ───────────────────────────────────────── ناوبری
    fun push(screen: Screen) {
        backStack = backStack + screen
    }

    /** جایگزینی صفحهٔ جاری (برای ورق زدن روزها) */
    fun replace(screen: Screen) {
        if (backStack.isEmpty()) backStack = listOf(screen)
        else backStack = backStack.dropLast(1) + screen
    }

    fun back(): Boolean {
        if (backStack.size > 1) {
            backStack = backStack.dropLast(1)
            return true
        }
        return false
    }

    fun openTab(screen: Screen) {
        backStack = listOf(screen)
    }

    fun openDay(epochDay: Long) {
        selectedEpochDay = epochDay
        val date = PersianCalendar.fromEpochDay(epochDay)
        viewYear = date.year
        viewMonth = date.month
        push(Screen.Day(epochDay))
    }

    fun goToMonth(year: Int, month: Int) {
        var y = year
        var m = month
        if (m < 1) {
            m = 12
            y -= 1
        } else if (m > 12) {
            m = 1
            y += 1
        }
        viewYear = y
        viewMonth = m
    }

    fun shiftMonth(delta: Int) {
        when (calendarMode) {
            CalendarMode.SOLAR -> {
                val index = viewYear * 12 + (viewMonth - 1) + delta
                viewYear = index / 12
                viewMonth = index % 12 + 1
            }
            CalendarMode.GREGORIAN -> {
                val index = viewGYear * 12 + (viewGMonth - 1) + delta
                viewGYear = index / 12
                viewGMonth = index % 12 + 1
            }
            CalendarMode.HIJRI -> {
                val index = viewHYear * 12 + (viewHMonth - 1) + delta
                viewHYear = index / 12
                viewHMonth = index % 12 + 1
            }
        }
    }

    fun goToday() {
        val date = currentDate()
        viewYear = date.year
        viewMonth = date.month
        val gregorian = date.toGregorian()
        viewGYear = gregorian.year
        viewGMonth = gregorian.month
        val hijri = HijriCalendar.fromEpochDay(todayEpochDay, hijriOffset)
        viewHYear = hijri.year
        viewHMonth = hijri.month
        selectedEpochDay = todayEpochDay
    }

    fun syncViewsToDate(epochDay: Long) {
        val date = PersianCalendar.fromEpochDay(epochDay)
        viewYear = date.year
        viewMonth = date.month
        val gregorian = date.toGregorian()
        viewGYear = gregorian.year
        viewGMonth = gregorian.month
        val hijri = HijriCalendar.fromEpochDay(epochDay, hijriOffset)
        viewHYear = hijri.year
        viewHMonth = hijri.month
    }

    fun viewMonthChange(month: Int) {
        viewMonth = month.coerceIn(1, 12)
    }

    fun setCalendarMode(mode: CalendarMode) {
        calendarMode = mode
        syncViewsToDate(selectedEpochDay)
    }

    // ───────────────────────────────────────── تنظیمات
    fun setThemeMode(mode: ThemeMode) {
        themeMode = mode
        prefs.edit().putString("theme", mode.name).apply()
    }

    fun setPersianDigits(value: Boolean) {
        persianDigits = value
        prefs.edit().putBoolean("persian_digits", value).apply()
    }

    fun setWeekStartsSaturday(value: Boolean) {
        weekStartsSaturday = value
        prefs.edit().putBoolean("week_saturday", value).apply()
    }

    fun setHijriOffset(value: Int) {
        val v = value.coerceIn(-3, 3)
        hijriOffset = v
        prefs.edit().putInt("hijri_offset", v).apply()
    }

    fun setShowGregorianNumbers(value: Boolean) {
        showGregorianNumbers = value
        prefs.edit().putBoolean("show_gregorian", value).apply()
    }

    fun setShowZoroastrianDay(value: Boolean) {
        showZoroastrianDay = value
        prefs.edit().putBoolean("show_zoroastrian", value).apply()
    }

    fun setShowAstronomy(value: Boolean) {
        showAstronomy = value
        prefs.edit().putBoolean("show_astronomy", value).apply()
    }

    fun setEventsOnly(value: Boolean) {
        eventsOnly = value
        prefs.edit().putBoolean("events_only", value).apply()
    }

    fun setNotifications(enabled: Boolean, hour: Int = notifyHour, minute: Int = notifyMinute) {
        notificationsEnabled = enabled
        notifyHour = hour
        notifyMinute = minute
        prefs.edit()
            .putBoolean("notifications", enabled)
            .putInt("notify_hour", hour)
            .putInt("notify_minute", minute)
            .apply()
        Notifications.schedule(context, enabled, hour, minute)
    }

    fun toggleBookmark(eventId: String) {
        val newSet = if (bookmarks.contains(eventId)) bookmarks - eventId else bookmarks + eventId
        bookmarks = newSet
        prefs.edit().putStringSet("bookmarks", newSet).apply()
    }

    fun isBookmarked(eventId: String): Boolean = bookmarks.contains(eventId)

    fun setNote(epochDay: Long, text: String) {
        val updated = notes.toMutableMap()
        if (text.isBlank()) updated.remove(epochDay.toString()) else updated[epochDay.toString()] = text
        notes = updated
        val json = JSONObject()
        for ((key, value) in updated) json.put(key, value)
        prefs.edit().putString("notes", json.toString()).apply()
    }

    fun noteFor(epochDay: Long): String = notes[epochDay.toString()] ?: ""

    private fun loadNotes(): Map<String, String> {
        val raw = prefs.getString("notes", null) ?: return emptyMap()
        return runCatching {
            val json = JSONObject(raw)
            val map = LinkedHashMap<String, String>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = json.optString(key)
            }
            map
        }.getOrDefault(emptyMap())
    }

    // ───────────────────────────────────────── ابزار
    fun digits(value: Int): String = PersianText.digits(value, persianDigits)

    fun digits(value: String): String = PersianText.convertDigits(value, persianDigits)
    fun digits(value: Long): String = PersianText.digits(value, persianDigits)
    fun text(value: String): String = PersianText.convertDigits(value, persianDigits)

    /** ترتیب ستون‌های هفته: شنبه یا یکشنبه */
    fun weekOrder(): List<Int> {
        val startIndex = if (weekStartsSaturday) 0 else 6
        return (0..6).map { (startIndex + it) % 7 }
    }

    fun weekdayLabel(index: Int): String = PersianCalendar.WEEKDAY_NAMES[index]

    fun themeIsDark(systemDark: Boolean): Boolean = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }
}
