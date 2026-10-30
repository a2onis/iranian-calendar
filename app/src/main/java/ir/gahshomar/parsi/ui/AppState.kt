package ir.gahshomar.parsi.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ir.gahshomar.parsi.core.IranianCalendar
import ir.gahshomar.parsi.core.IranianDate
import ir.gahshomar.parsi.core.PersianText
import ir.gahshomar.parsi.notify.DayStatus

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

    /** تاریخ در نوار وضعیت: تنها وقتی برنامه بسته است دیده می‌شود */
    fun setDayStatus(enabled: Boolean) {
        dayStatusState = enabled
        prefs.edit().putBoolean("day_status", enabled).apply()
        runCatching {
            if (enabled) {
                DayStatus.scheduleMidnight(context)
                DayStatus.hide(context)
            } else {
                DayStatus.refresh(context, false)
            }
        }
    }

    /** نوارگاه‌شمار جز وقتی برنامه باز است، در نوار وضعیت می‌ماند */
    fun hideDayStatusWhileOpen() {
        runCatching { DayStatus.hide(context) }
    }

    fun showDayStatusWhenClosed() {
        if (!dayStatusState) return
        runCatching {
            DayStatus.show(context)
            DayStatus.scheduleMidnight(context)
        }
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
