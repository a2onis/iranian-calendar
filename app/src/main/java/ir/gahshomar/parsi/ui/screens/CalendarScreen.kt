package ir.gahshomar.parsi.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.gahshomar.parsi.core.CivilMath
import ir.gahshomar.parsi.core.GlobalSymbols
import ir.gahshomar.parsi.core.HijriCalendar
import ir.gahshomar.parsi.core.HijriDate
import ir.gahshomar.parsi.core.PersianCalendar
import ir.gahshomar.parsi.core.PersianText
import ir.gahshomar.parsi.core.ZoroastrianCalendar
import ir.gahshomar.parsi.data.Event
import ir.gahshomar.parsi.data.Events
import ir.gahshomar.parsi.data.Observance
import ir.gahshomar.parsi.data.Quotes
import ir.gahshomar.parsi.ui.AppState
import ir.gahshomar.parsi.ui.CalendarMode
import ir.gahshomar.parsi.ui.LocalGahPalette
import ir.gahshomar.parsi.ui.Screen
import ir.gahshomar.parsi.ui.StarMotif
import ir.gahshomar.parsi.ui.components.ActionChip
import ir.gahshomar.parsi.ui.components.EventRow
import ir.gahshomar.parsi.ui.components.GahCard
import ir.gahshomar.parsi.ui.components.InfoRow
import ir.gahshomar.parsi.ui.components.SectionTitle
import ir.gahshomar.parsi.ui.components.SegmentedRow
import ir.gahshomar.parsi.ui.components.SoftDividerRow

private fun share(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    runCatching { context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری")) }
}

/** خلاصهٔ یک روز به‌صورت متن قابل اشتراک */
fun dayShareText(epochDay: Long, persianDigits: Boolean): String {
    val date = PersianCalendar.fromEpochDay(epochDay)
    val gregorian = date.toGregorian()
    val hijri = HijriCalendar.fromEpochDay(epochDay)
    val events = Events.forEpochDay(epochDay)
    val builder = StringBuilder()
    builder.append(PersianText.formatDateWithWeekday(date, persianDigits)).append("\n")
    builder.append("میلادی: ").append(PersianText.formatGregorian(gregorian, persianDigits)).append("\n")
    builder.append("قمری: ").append(PersianText.formatHijri(hijri, persianDigits)).append("\n")
    if (events.isNotEmpty()) {
        builder.append("\nمناسبت‌ها:\n")
        events.forEach { builder.append("• ").append(it.title).append("\n") }
    }
    builder.append("\nگاهشمار پارسی")
    return builder.toString()
}

@Composable
fun CalendarScreen(state: AppState, modifier: Modifier = Modifier) {
    val palette = LocalGahPalette.current
    val context = LocalContext.current
    val today = state.todayEpochDay
    val selected = state.selectedEpochDay
    val selectedDate = PersianCalendar.fromEpochDay(selected)
    val mode = state.calendarMode

    val monthStart: Long
    val monthLength: Int
    val monthTitle: String
    when (mode) {
        CalendarMode.SOLAR -> {
            monthStart = PersianCalendar.toEpochDay(state.viewYear, state.viewMonth, 1)
            monthLength = PersianCalendar.monthLength(state.viewYear, state.viewMonth)
            monthTitle = PersianText.monthName(state.viewMonth) + " " + state.digits(state.viewYear)
        }
        CalendarMode.GREGORIAN -> {
            monthStart = CivilMath.gregorianToEpochDay(state.viewGYear, state.viewGMonth, 1)
            monthLength = ir.gahshomar.parsi.core.GregorianDate(state.viewGYear, state.viewGMonth, 1).monthLength
            monthTitle = PersianText.gregorianMonthName(state.viewGMonth) + " " + state.digits(state.viewGYear)
        }
        else -> {
            val hijri = HijriDate(state.viewHYear, state.viewHMonth, 1)
            monthStart = HijriCalendar.toEpochDay(hijri)
            monthLength = HijriCalendar.monthLength(state.viewHYear, state.viewHMonth)
            monthTitle = HijriCalendar.monthName(state.viewHMonth) + " " + state.digits(state.viewHYear)
        }
    }

    val weekOrder = state.weekOrder()
    val firstWeekday = PersianCalendar.weekdayIndex(monthStart)
    val leading = ((firstWeekday - weekOrder.first()) + 7) % 7
    val gridStart = monthStart - leading

    val eventsMap = remember(gridStart, mode, state.showAstronomy, state.hijriOffset) {
        val map = HashMap<Long, List<Event>>()
        for (i in 0 until 42) {
            val day = gridStart + i
            map[day] = Events.forEpochDay(day, state.showAstronomy)
        }
        map
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 22.dp)
    ) {
        item {
            CalendarHeader(
                state = state,
                selectedDate = selectedDate,
                selected = selected,
                onSettings = { state.push(Screen.Settings) },
                onConverter = { state.push(Screen.Converter) },
                onAbout = { state.push(Screen.About) }
            )
        }

        item {
            MonthBar(
                title = monthTitle,
                onPrevious = { state.shiftMonth(-1) },
                onNext = { state.shiftMonth(1) },
                onToday = { state.goToday() }
            )
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                SegmentedRow(
                    options = CalendarMode.values().map { it.label },
                    selectedIndex = CalendarMode.values().indexOf(mode),
                    onSelect = { state.setCalendarMode(CalendarMode.values()[it]) }
                )
            }
        }

        if (mode == CalendarMode.SOLAR) {
            item {
                LazyRow(
                    modifier = Modifier.padding(top = 8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items((1..12).toList()) { month ->
                        MonthChip(
                            title = PersianText.monthName(month),
                            selected = month == state.viewMonth,
                            onClick = {
                                state.viewMonthChange(month)
                            }
                        )
                    }
                }
            }
        }

        item {
            WeekdayHeader(weekOrder = weekOrder, state = state)
        }

        items(6) { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 7.dp)
            ) {
                for (column in 0 until 7) {
                    val epochDay = gridStart + week * 7 + column
                    DayCell(
                        epochDay = epochDay,
                        inMonth = epochDay in monthStart until (monthStart + monthLength),
                        state = state,
                        events = eventsMap[epochDay].orEmpty(),
                        isSelected = epochDay == selected,
                        isToday = epochDay == today,
                        modifier = Modifier.weight(1f),
                        onClick = { state.selectedEpochDay = epochDay }
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            DaySummary(
                state = state,
                epochDay = selected,
                events = eventsMap[selected] ?: Events.forEpochDay(selected, state.showAstronomy),
                onOpenDay = { state.push(Screen.Day(selected)) },
                onShare = { share(context, dayShareText(selected, state.persianDigits)) }
            )
        }

        item {
            UpcomingSection(state = state)
        }

        item {
            QuoteCard(state = state)
        }

        item {
            Text(
                text = "مبنای محاسبه: قاعدهٔ نجومی مرکز تقویم ایران — اعتدال بهاری به وقت تهران.\n" +
                    "همهٔ داده‌ها در خود برنامه و بدون اینترنت است.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp, vertical = 18.dp)
            )
        }
    }
}

@Composable
private fun HeaderPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .border(1.dp, color.copy(alpha = 0.55f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun CalendarHeader(
    state: AppState,
    selectedDate: ir.gahshomar.parsi.core.PersianDate,
    selected: Long,
    onSettings: () -> Unit,
    onConverter: () -> Unit,
    onAbout: () -> Unit
) {
    val palette = LocalGahPalette.current
    val gregorian = selectedDate.toGregorian()
    val hijri = HijriCalendar.fromEpochDay(selected, state.hijriOffset)
    ir.gahshomar.parsi.ui.ArchHeader(height = 226.dp) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "گاهشمار پارسی",
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.onHeader
                    )
                    Text(
                        text = "تقویم ایران، آیین‌ها و فرهنگ کهن",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.onHeader.copy(alpha = 0.72f)
                    )
                }
                IconButton(onClick = onConverter) {
                    Icon(Icons.Filled.Refresh, contentDescription = "تبدیل تاریخ", tint = palette.gold)
                }
                IconButton(onClick = onAbout) {
                    Icon(Icons.Filled.Info, contentDescription = "درباره", tint = palette.gold)
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "تنظیمات", tint = palette.gold)
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = PersianCalendar.WEEKDAY_NAMES[PersianCalendar.weekdayIndex(selected)] +
                            " " + state.digits(selectedDate.day),
                        style = MaterialTheme.typography.displaySmall,
                        color = palette.onHeader
                    )
                    Text(
                        text = PersianText.monthName(selectedDate.month) + " " + state.digits(selectedDate.year),
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.gold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    StarMotif(modifier = Modifier.size(34.dp), color = palette.gold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = PersianCalendar.ZODIAC_NAMES[(selectedDate.month - 1).coerceIn(0, 11)] + " • " +
                            PersianCalendar.seasonOf(selectedDate.month).title,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.onHeader.copy(alpha = 0.85f)
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HeaderPill(PersianText.formatGregorian(gregorian, state.persianDigits), palette.onHeader.copy(alpha = 0.9f))
                HeaderPill(PersianText.formatHijri(hijri, state.persianDigits), palette.turquoise)
            }
        }
    }
}

@Composable
private fun MonthBar(title: String, onPrevious: () -> Unit, onNext: () -> Unit, onToday: () -> Unit) {
    val palette = LocalGahPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "ماه پیش", tint = palette.turquoise)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "ماه بعد", tint = palette.turquoise)
        }
        ActionChip(text = "امروز", onClick = onToday)
        Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun MonthChip(title: String, selected: Boolean, onClick: () -> Unit) {
    val palette = LocalGahPalette.current
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (selected) Color.Transparent else palette.cardBorder, CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 11.dp, vertical = 5.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WeekdayHeader(weekOrder: List<Int>, state: AppState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 7.dp, vertical = 4.dp)
    ) {
        weekOrder.forEach { index ->
            Text(
                text = if (index == 6) "آدینه" else PersianCalendar.WEEKDAY_NAMES_SHORT[index],
                style = MaterialTheme.typography.labelSmall,
                color = if (index == 6) LocalGahPalette.current.holiday else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DayCell(
    epochDay: Long,
    inMonth: Boolean,
    state: AppState,
    events: List<Event>,
    isSelected: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val palette = LocalGahPalette.current
    val date = PersianCalendar.fromEpochDay(epochDay)
    val gregorian = CivilMath.epochDayToGregorian(epochDay)
    val hijri = HijriCalendar.fromEpochDay(epochDay, state.hijriOffset)
    val isHoliday = events.any { it.observance != Observance.NONE }

    val primaryNumber = when (state.calendarMode) {
        CalendarMode.SOLAR -> date.day
        CalendarMode.GREGORIAN -> gregorian.day
        else -> hijri.day
    }
    val solarSecondary = if (state.showGregorianNumbers) state.digits(gregorian.day) else ""
    val secondaryText = when (state.calendarMode) {
        CalendarMode.SOLAR -> solarSecondary
        CalendarMode.GREGORIAN -> state.digits(date.day)
        else -> state.digits(date.day)
    }

    val background = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> palette.turquoise.copy(alpha = 0.14f)
        inMonth -> MaterialTheme.colorScheme.surface
        else -> Color.Transparent
    }
    val numberColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        !inMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
        isHoliday || PersianCalendar.weekdayIndex(epochDay) == 6 -> palette.holiday
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(0.82f)
            .clip(RoundedCornerShape(13.dp))
            .background(background)
            .then(
                if (isToday && !isSelected)
                    Modifier.border(1.5.dp, palette.turquoise, RoundedCornerShape(13.dp))
                else Modifier
            )
            .clickable { onClick() }
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = state.digits(primaryNumber),
            style = MaterialTheme.typography.titleMedium,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = numberColor
        )
        if (secondaryText.isNotEmpty()) {
            Text(
                text = secondaryText,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = numberColor.copy(alpha = 0.6f)
            )
        }
        Spacer(Modifier.height(3.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            val dots = events.take(3)
            dots.forEach { event ->
                Box(
                    modifier = Modifier
                        .size(3.5.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                event.observance != Observance.NONE -> palette.pomegranate
                                else -> palette.gold
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun DaySummary(
    state: AppState,
    epochDay: Long,
    events: List<Event>,
    onOpenDay: () -> Unit,
    onShare: () -> Unit
) {
    val palette = LocalGahPalette.current
    val date = PersianCalendar.fromEpochDay(epochDay)
    val gregorian = date.toGregorian()
    val hijri = HijriCalendar.fromEpochDay(epochDay, state.hijriOffset)
    val relative = PersianText.relativeLabel(epochDay, state.todayEpochDay)

    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
        GahCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ir.gahshomar.parsi.ui.SealBadge(ring = palette.gold) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.digits(date.day),
                            style = MaterialTheme.typography.headlineMedium,
                            color = palette.gold
                        )
                        Text(
                            text = state.digits(date.month),
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.onHeader.copy(alpha = 0.85f)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = PersianText.weekdayName(epochDay),
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (relative.isNotEmpty()) {
                            Spacer(Modifier.width(6.dp))
                            ir.gahshomar.parsi.ui.components.Pill(
                                text = relative,
                                color = palette.turquoise,
                                filled = true
                            )
                        }
                    }
                    Text(
                        text = PersianText.formatDate(date, state.persianDigits),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = PersianText.formatGregorian(gregorian, state.persianDigits) + " • " +
                            PersianText.formatHijri(hijri, state.persianDigits),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SoftDividerRow()

            if (state.showZoroastrianDay) {
                InfoRow(
                    label = "روز زرتشتی",
                    value = "روز " + ZoroastrianCalendar.dayName(date.day) + " از ماه " +
                        PersianText.monthNameAvestan(date.month) + " (" +
                        ZoroastrianCalendar.dayMeaning(date.day) + ")"
                )
            }
            InfoRow(label = "برج و فصل", value = PersianCalendar.ZODIAC_NAMES[(date.month - 1).coerceIn(0, 11)] + " • " +
                PersianCalendar.seasonOf(date.month).title)
            InfoRow(
                label = "روز و هفته",
                value = "روز " + state.digits(date.dayOfYear) + " از سال • هفتهٔ " +
                    state.digits(PersianCalendar.weekOfYear(date)) + " • هفتهٔ ISO " +
                    state.digits(PersianCalendar.isoWeek(epochDay))
            )
            InfoRow(
                label = "ماه قمری",
                value = PersianText.formatHijri(hijri, state.persianDigits) + " • " +
                    state.digits(HijriCalendar.monthLength(hijri.year, hijri.month)) + " روزه"
            )
            InfoRow(
                label = "گاه‌شماری چینی",
                value = GlobalSymbols.chineseCycle(gregorian.year)
            )
            if (date.month == 1 && date.day == 1) {
                InfoRow(
                    label = "لحظهٔ تحویل سال",
                    value = PersianText.formatTehranTime(
                        PersianCalendar.nowruzMomentUtcMillis(date.year),
                        state.persianDigits
                    ) + " به وقت تهران",
                    valueColor = palette.pomegranate
                )
            }

            if (events.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                SoftDividerRow()
                events.forEach { event ->
                    EventRow(
                        event = event,
                        onClick = onOpenDay,
                        trailing = {
                            IconButton(onClick = { state.toggleBookmark(event.id) }) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = "نشان‌کردن",
                                    tint = if (state.isBookmarked(event.id)) palette.gold
                                    else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    )
                }
            } else {
                Text(
                    text = "مناسبت ویژه‌ای برای این روز ثبت نشده است.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            val note = state.noteFor(epochDay)
            if (note.isNotBlank()) {
                SoftDividerRow()
                Text(
                    text = "یادداشت من: " + note,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.turquoise
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(text = "جزئیات روز", onClick = onOpenDay, filled = true)
                ActionChip(text = "اشتراک‌گذاری", icon = Icons.Filled.Share, onClick = onShare)
            }
        }
    }
}

@Composable
private fun UpcomingSection(state: AppState) {
    val today = state.todayEpochDay
    val upcoming = remember(today, state.showAstronomy) {
        Events.upcoming(today + 1, 6, state.showAstronomy)
    }
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
        SectionTitle("مناسبت‌های پیشِ رو")
        Spacer(Modifier.height(6.dp))
        upcoming.forEach { occurrence ->
            val date = occurrence.persianDate
            GahCard(
                modifier = Modifier.padding(bottom = 6.dp),
                onClick = { state.openDay(occurrence.epochDay) }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        modifier = Modifier
                            .width(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.digits(date.day),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = PersianText.monthName(date.month),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = occurrence.event.title, style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = PersianText.daysUntilLabel(occurrence.daysFromToday),
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalGahPalette.current.turquoise
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuoteCard(state: AppState) {
    val palette = LocalGahPalette.current
    val quote = Quotes.ofDay(state.todayEpochDay)
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
        SectionTitle("سخن روز")
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(palette.headerTop)
                .padding(16.dp)
        ) {
            Column {
                StarMotif(modifier = Modifier.size(18.dp), color = palette.gold)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = quote.text,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.onHeader,
                    lineHeight = 30.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "— " + quote.source,
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.gold
                )
            }
        }
    }
}
