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
import ir.gahshomar.parsi.notify.LockDateOverlay
import ir.gahshomar.parsi.ui.theme.Season

/** جای تاریخ در نوار وضعیت */
enum class StatusPosition(val key: String, val label: String) {
    LEFT("left", "چپ"),
    CENTER("center", "میان"),
    RIGHT("right", "راست")
}

/** جای پنجرهٔ تاریخ روی صفحه */
enum class OverlaySpot(val key: String, val label: String) {
    BAR_START("barStart", "نوار وضعیت — چپ"),
    BAR_CENTER("barCenter", "نوار وضعیت — میان"),
    BAR_END("barEnd", "نوار وضعیت — راست"),
    BELOW_START("belowStart", "زیر نوار — چپ"),
    BELOW_END("belowEnd", "زیر نوار — راست"),
    CENTER("center", "میانِ صفحه"),
    BOTTOM("bottom", "پایینِ صفحه")
}

/** اندازهٔ متن تاریخ روی صفحه */
enum class OverlaySize(val sp: Int, val label: String) {
    SMALL(16, "کوچک"),
    MEDIUM(22, "متوسط"),
    LARGE(30, "بزرگ"),
    HUGE(38, "بزرگ‌تر")
}

/** کوتاه یا کامل بودن متنِ نوار وضعیت */
enum class StatusText(val key: String, val label: String) {
    FULL("full", "کامل — دی به آذر ۹ آبان"),
    SHORT("short", "کوتاه — ۹ آبان")
}

/** تاریخ همیشه دیده شود یا تنها بر صفحهٔ قفل */
enum class StatusMode(val key: String, val label: String) {
    ALWAYS("always", "همیشه در نوار وضعیت"),
    LOCKED("locked", "فقط بر صفحهٔ قفل")
}

enum class ThemeMode(val label: String) {
    SEASONAL("فصلی"),
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

    // کلید تازه («theme_mode»): نسخه‌های پیشین «theme» را با پیش‌فرضِ تیره ذخیره می‌کردند
    // و همین باعث می‌شد پوستهٔ فصلی هرگز به کار نیفتد.
    private var themeModeState by mutableStateOf(
        runCatching { ThemeMode.valueOf(prefs.getString("theme_mode", "SEASONAL")!!) }
            .getOrDefault(ThemeMode.SEASONAL)
    )
    val themeMode: ThemeMode get() = themeModeState

    private var persianDigitsState by mutableStateOf(prefs.getBoolean("persian_digits", true))
    val persianDigits: Boolean get() = persianDigitsState

    private var todayOnlyState by mutableStateOf(prefs.getBoolean("today_only", true))
    val todayOnly: Boolean get() = todayOnlyState

    private var dayStatusState by mutableStateOf(prefs.getBoolean("day_status", false))
    val dayStatusEnabled: Boolean get() = dayStatusState

    // ── پنجرهٔ تاریخ (بی اعلان)

    private var overlayEnabledState by mutableStateOf(prefs.getBoolean("overlay_enabled", true))
    val overlayEnabled: Boolean get() = overlayEnabledState

    private var overlaySpotState by mutableStateOf(
        OverlaySpot.values().firstOrNull {
            it.key == prefs.getString("overlay_position", OverlaySpot.BAR_END.key)
        } ?: OverlaySpot.BAR_END
    )
    val overlaySpot: OverlaySpot get() = overlaySpotState

    private var overlaySizeState by mutableStateOf(
        OverlaySize.values().firstOrNull { it.sp == prefs.getInt("overlay_size", 22) }
            ?: OverlaySize.MEDIUM
    )
    val overlaySize: OverlaySize get() = overlaySizeState

    private var overlayLockOnlyState by mutableStateOf(prefs.getBoolean("overlay_lock_only", false))
    val overlayLockOnly: Boolean get() = overlayLockOnlyState

    private var overlayYearState by mutableStateOf(prefs.getBoolean("overlay_show_year", false))
    val overlayShowYear: Boolean get() = overlayYearState

    private var overlayShortState by mutableStateOf(prefs.getBoolean("overlay_short", false))
    val overlayShort: Boolean get() = overlayShortState

    /** آیا سرویس دسترس‌پذیری (که تاریخ را روی صفحه می‌کشد) روشن است؟ */
    private var overlayReadyState by mutableStateOf(false)
    val overlayReady: Boolean get() = overlayReadyState

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
        overlayReadyState = LockDateOverlay.isEnabled(context)
        val now = IranianCalendar.todayEpochDay()
        if (now != todayEpochDay) {
            todayEpochDay = now
            if (todayOnlyState) {
                selectedEpochDay = now
                browseYear = IranianCalendar.yearOf(now)
            }
        }
    }

    /**
     * فصلِ ماهِ برگزیده — رنگ‌مایهٔ برنامه با آن می‌گردد.
     * در آغاز، برگزیده همان امروز است؛ پس رنگ‌مایه با فصلِ کنونی هم‌خوان است،
     * و با گشتن در ماه‌های سال هم بی‌درنگ می‌گردد (بهار سبز، پاییز نارنجی…).
     */
    val season: Season get() = Season.of(selectedEpochDay)

    /** فصلِ امروز، برای توضیح در تنظیمات */
    val seasonToday: Season get() = Season.of(todayEpochDay)

    fun themeIsDark(systemDark: Boolean): Boolean = when (themeMode) {
        ThemeMode.SEASONAL -> season.prefersDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
    }

    // ───────────────────────────────────────── تنظیمات

    fun setThemeMode(mode: ThemeMode) {
        themeModeState = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
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

    // ───────────────────────────────────────── پنجرهٔ تاریخ (بی اعلان)

    fun setOverlayEnabled(enabled: Boolean) {
        overlayEnabledState = enabled
        prefs.edit().putBoolean("overlay_enabled", enabled).apply()
    }

    fun setOverlaySpot(spot: OverlaySpot) {
        overlaySpotState = spot
        prefs.edit().putString("overlay_position", spot.key).apply()
    }

    fun setOverlaySize(size: OverlaySize) {
        overlaySizeState = size
        prefs.edit().putInt("overlay_size", size.sp).apply()
    }

    fun setOverlayLockOnly(value: Boolean) {
        overlayLockOnlyState = value
        prefs.edit().putBoolean("overlay_lock_only", value).apply()
    }

    fun setOverlayShowYear(value: Boolean) {
        overlayYearState = value
        prefs.edit().putBoolean("overlay_show_year", value).apply()
    }

    fun setOverlayShort(value: Boolean) {
        overlayShortState = value
        prefs.edit().putBoolean("overlay_short", value).apply()
    }

    /** پس از بازگشت از صفحهٔ تنظیماتِ اندروید دوباره سنجیده می‌شود */
    fun refreshOverlayReady() {
        overlayReadyState = LockDateOverlay.isEnabled(context)
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
