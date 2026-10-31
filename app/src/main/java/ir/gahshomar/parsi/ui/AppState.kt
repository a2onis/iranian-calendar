package ir.gahshomar.parsi.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ir.gahshomar.parsi.core.IranianCalendar
import ir.gahshomar.parsi.core.IranianDate
import ir.gahshomar.parsi.core.PersianText
import ir.gahshomar.parsi.notify.DayStatus
import ir.gahshomar.parsi.notify.DayStatusService

/** جای تاریخ در نوار وضعیت */
enum class StatusPosition(val key: String, val label: String) {
    LEFT("left", "چپ"),
    CENTER("center", "میان"),
    RIGHT("right", "راست")
}

/** کوتاه یا کامل بودن متنِ نوار وضعیت */
enum class StatusText(val key: String, val label: String) {
    SHORT("short", "کوتاه — ۸ آبان"),
    FULL("full", "کامل — ۸ آبان / دی به آذر")
}

/** تاریخ همیشه دیده شود یا تنها بر صفحهٔ قفل */
enum class StatusMode(val key: String, val label: String) {
    ALWAYS("always", "همیشه در نوار وضعیت"),
    LOCKED("locked", "فقط بر صفحهٔ قفل")
}

enum class ThemeMode(val label: String) {
    DARK("تیره"),
    LIGHT("روشن"),
    SYSTEM("هماهنگ با سامانه")
}

/**
 * وضعیت برنامه؛ برنامه یک صفحه دارد: گاه‌شمار ایرانی.
 * تنها کارِ کاربر «برگزیدن روز» است و نمایش جشن‌های همان روز.
 */
class AppState(val context: Context) {

    private val prefs = context.getSharedPreferences("gahshomar_prefs", Context.MODE_PRIVATE)

    var todayEpochDay by mutableStateOf(IranianCalendar.todayEpochDay())
        private set

    var selectedEpochDay by mutableStateOf(IranianCalendar.todayEpochDay())
        private set

    /** سالی که در دوازده‌ماهِ فشرده نمایش داده می‌شود */
    var browseYear by mutableStateOf(IranianCalendar.today().year)
        private set

    // ───────────────────────────────────────── تنظیمات

    private var themeModeState by mutableStateOf(
        runCatching { ThemeMode.valueOf(prefs.getString("theme", "DARK")!!) }.getOrDefault(ThemeMode.DARK)
    )
    val themeMode: ThemeMode get() = themeModeState

    private var persianDigitsState by mutableStateOf(prefs.getBoolean("persian_digits", true))
    val persianDigits: Boolean get() = persianDigitsState

    private var todayOnlyState by mutableStateOf(prefs.getBoolean("today_only", true))
    val todayOnly: Boolean get() = todayOnlyState

    private var dayStatusState by mutableStateOf(prefs.getBoolean("day_status", true))
    val dayStatusEnabled: Boolean get() = dayStatusState

    private var statusPositionState by mutableStateOf(
        StatusPosition.values().firstOrNull {
            it.key == prefs.getString("status_position", StatusPosition.RIGHT.key)
        } ?: StatusPosition.RIGHT
    )
    val statusPosition: StatusPosition get() = statusPositionState

    private var statusTextState by mutableStateOf(
        StatusText.values().firstOrNull {
            it.key == prefs.getString("status_text", StatusText.FULL.key)
        } ?: StatusText.FULL
    )
    val statusText: StatusText get() = statusTextState

    private var statusModeState by mutableStateOf(
        StatusMode.values().firstOrNull {
            it.key == prefs.getString("status_mode", StatusMode.ALWAYS.key)
        } ?: StatusMode.ALWAYS
    )
    val statusMode: StatusMode get() = statusModeState

    // ───────────────────────────────────────── حرکت در تقویم

    val selectedDate: IranianDate get() = IranianCalendar.fromEpochDay(selectedEpochDay)

    val todayDate: IranianDate get() = IranianCalendar.fromEpochDay(todayEpochDay)

    fun select(epochDay: Long) {
        selectedEpochDay = epochDay
        browseYear = IranianCalendar.yearOf(epochDay)
    }

    fun shiftDay(delta: Int) {
        select(selectedEpochDay + delta)
    }

    fun goToday() {
        selectedEpochDay = todayEpochDay
        browseYear = todayDate.year
    }

    /** رفتن به یک ماه از سالِ نمایش‌داده‌شده */
    fun showMonth(year: Int, month: Int) {
        browseYear = year
        select(IranianCalendar.toEpochDay(year, month, 1))
    }

    /** سالِ پیش یا پس، با نگه‌داشتن روزِ برگزیده در همان جای سال */
    fun shiftYear(delta: Int) {
        val year = browseYear + delta
        val current = selectedDate
        val target = if (current.isPanjeh) {
            IranianCalendar.panjehStart(year) +
                (current.panjehIndex - 1).coerceAtMost(IranianCalendar.panjehLength(year) - 1)
        } else {
            val day = current.day.coerceAtMost(IranianCalendar.monthLength(year, current.month))
            IranianCalendar.toEpochDay(year, current.month, day)
        }
        browseYear = year
        selectedEpochDay = target
    }

    fun refreshToday() {
        val now = IranianCalendar.todayEpochDay()
        if (now != todayEpochDay) {
            todayEpochDay = now
            if (todayOnlyState) {
                selectedEpochDay = now
                browseYear = IranianCalendar.yearOf(now)
            }
        }
    }

    fun themeIsDark(systemDark: Boolean): Boolean = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
    }

    // ───────────────────────────────────────── تنظیمات

    fun setThemeMode(mode: ThemeMode) {
        themeModeState = mode
        prefs.edit().putString("theme", mode.name).apply()
    }

    fun setPersianDigits(value: Boolean) {
        persianDigitsState = value
        prefs.edit().putBoolean("persian_digits", value).apply()
    }

    fun setTodayOnly(value: Boolean) {
        todayOnlyState = value
        prefs.edit().putBoolean("today_only", value).apply()
    }

    /** تاریخ در نوار وضعیت و بر صفحهٔ قفل */
    fun setDayStatus(enabled: Boolean) {
        dayStatusState = enabled
        prefs.edit().putBoolean("day_status", enabled).apply()
        runCatching { DayStatus.refresh(context, enabled) }
        DayStatusService.apply(context)
    }

    /** جای متن در نوار وضعیت */
    fun setStatusPosition(position: StatusPosition) {
        statusPositionState = position
        prefs.edit().putString("status_position", position.key).apply()
        runCatching { DayStatus.show(context) }
    }

    /** کوتاه یا کامل بودن متن نوار وضعیت */
    fun setStatusText(text: StatusText) {
        statusTextState = text
        prefs.edit().putString("status_text", text.key).apply()
        runCatching { DayStatus.show(context) }
    }

    /** همیشه، یا تنها بر صفحهٔ قفل */
    fun setStatusMode(mode: StatusMode) {
        statusModeState = mode
        prefs.edit().putString("status_mode", mode.key).apply()
        runCatching {
            if (mode == StatusMode.ALWAYS) DayStatus.show(context) else DayStatus.hide(context)
        }
        DayStatusService.apply(context)
    }

    /** هم‌گام‌سازی تاریخ با امروز (هنگام گشودن یا بازگشت به برنامه) */
    fun syncDayStatus() {
        if (!dayStatusState) return
        runCatching {
            if (statusModeState == StatusMode.ALWAYS) DayStatus.show(context)
        }
        DayStatusService.apply(context)
    }

    // ───────────────────────────────────────── ابزارهای نمایش

    fun digits(value: Int): String = PersianText.digits(value, persianDigits)

    fun digits(value: Long): String = PersianText.digits(value, persianDigits)

    fun digits(value: String): String = PersianText.convertDigits(value, persianDigits)

    fun weekdayName(epochDay: Long): String = IranianCalendar.weekdayName(epochDay)

    fun weekdayShort(epochDay: Long): String =
        IranianCalendar.WEEKDAY_NAMES_SHORT[IranianCalendar.weekdayIndex(epochDay)]

    fun dateLabel(date: IranianDate): String = PersianText.formatDate(date, persianDigits)

    fun weekdayShortOf(index: Int): String = IranianCalendar.WEEKDAY_NAMES_SHORT[index]
}
