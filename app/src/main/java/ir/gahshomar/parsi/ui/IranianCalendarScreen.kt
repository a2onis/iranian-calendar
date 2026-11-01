package ir.gahshomar.parsi.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.gahshomar.parsi.R
import ir.gahshomar.parsi.core.IranianCalendar
import ir.gahshomar.parsi.core.IranianDate
import ir.gahshomar.parsi.core.PersianText
import ir.gahshomar.parsi.data.Festival
import ir.gahshomar.parsi.data.FestivalKind
import ir.gahshomar.parsi.data.FestivalOccurrence
import ir.gahshomar.parsi.data.Festivals

/**
 * تنها صفحهٔ برنامه: گاه‌شمار ایرانی.
 * بالا: تاریخ برگزیده. سپس دوازده ماهِ سال و شبکهٔ روزها.
 * پایین: جشن‌ها و آیین‌های همان روز برگزیده.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IranianCalendarScreen(state: AppState, modifier: Modifier = Modifier) {
    val palette = LocalGahPalette.current
    val context = LocalContext.current
    var detail by remember { mutableStateOf<Festival?>(null) }
    var showSettings by remember { mutableStateOf(false) }

    val selected = state.selectedEpochDay
    val date = IranianCalendar.fromEpochDay(selected)
    val year = date.year
    val month = if (date.isPanjeh) 12 else date.month

    val days = remember(year, month) { monthCells(year, month) }
    val festivalDays = remember(year, month) {
        val set = HashSet<Long>()
        Festivals.ofMonth(year, month).forEach { set.add(it.epochDay) }
        if (month == 12) {
            Festivals.ofYear(year)
                .filter { IranianCalendar.fromEpochDay(it.epochDay).isPanjeh }
                .forEach { set.add(it.epochDay) }
        }
        set
    }
    val todayFestivals = remember(selected) { Festivals.onEpochDay(selected) }

    // تاریخ با گذشت روزها خودبه‌خود تازه می‌شود: هر دقیقه «امروز» از نو خوانده می‌شود
    // (برنامه در نیمه‌شب باز مانده باشد، یا زمانی که دستگاه به وقت تازه‌ای برود).
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000L)
            state.refreshToday()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        StarryBackdrop(
            color = palette.gold,
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                state = state,
                onToday = { state.goToday() },
                onSettings = { showSettings = true }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 26.dp)
            ) {
                item { SelectedDateHeader(state = state, date = date, selected = selected) }
                item { MonthRibbon(state = state, year = year, activeMonth = month) }
                item { WeekdayRow(state) }

                items(days.chunked(7)) { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        row.forEach { day ->
                            if (day == null) {
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                DayCell(
                                    state = state,
                                    day = day,
                                    year = year,
                                    month = month,
                                    selected = selected,
                                    hasFestival = festivalDays.contains(
                                        cellEpochDay(year, month, day)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        repeat(7 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }

                item {
                    Text(
                        text = "برای دیدن جشن‌ها و آیین‌های هر روز، روی آن روز بزنید.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 2.dp)
                    )
                }

                item {
                    DayFestivals(
                        state = state,
                        date = date,
                        epochDay = selected,
                        festivals = todayFestivals,
                        onDetail = { detail = it },
                        onShare = { share(context, PersianText.shareText(selected, state.persianDigits)) }
                    )
                }

                item { UpcomingStrip(state = state, selected = selected) }
                item { FooterNote(state = state) }
            }
        }
    }

    if (detail != null) {
        val festival = detail
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { detail = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            FestivalSheetContent(
                state = state,
                festival = festival!!,
                onClose = { detail = null }
            )
        }
    }

    if (showSettings) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            SettingsSheetContent(state = state, onClose = { showSettings = false })
        }
    }
}

// ───────────────────────────────────────────── شبکهٔ روزها

/**
 * خانه‌های ماه: null برای جای خالی، و عددِ روز.
 * روزهای پنجهٔ وهیک (پایان سپندارمذ) با عددِ منفی نشان داده می‌شوند: ۱− تا ۶−.
 */
private fun monthCells(year: Int, month: Int): List<Int?> {
    val first = IranianCalendar.toEpochDay(year, month, 1)
    val leading = IranianCalendar.weekdayIndex(first)
    val panjeh = if (month == 12) IranianCalendar.panjehLength(year) else 0
    val regularDays = IranianCalendar.monthLength(year, month) - panjeh
    val cells = ArrayList<Int?>(leading + regularDays + panjeh + 7)
    repeat(leading) { cells.add(null) }
    for (day in 1..regularDays) cells.add(day)
    for (index in 1..panjeh) cells.add(-index)
    return cells
}

private fun cellEpochDay(year: Int, month: Int, day: Int): Long =
    if (day > 0) {
        IranianCalendar.toEpochDay(year, month, day)
    } else {
        IranianCalendar.panjehStart(year) + (-day - 1)
    }

// ───────────────────────────────────────────── نوار بالایی

@Composable
private fun TopBar(state: AppState, onToday: () -> Unit, onSettings: () -> Unit) {
    val palette = LocalGahPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(palette.headerTop, MaterialTheme.colorScheme.background)
                )
            )
            .statusBarsPadding()
            .padding(start = 18.dp, end = 10.dp, top = 10.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StarMotif(modifier = Modifier.size(15.dp), color = palette.gold)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "گاه‌شمار ایرانی",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = state.dateLabel(IranianCalendar.fromEpochDay(state.selectedEpochDay)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (state.selectedEpochDay != state.todayEpochDay) {
            Text(
                text = "امروز",
                style = MaterialTheme.typography.labelLarge,
                color = palette.gold,
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, palette.gold.copy(alpha = 0.55f), CircleShape)
                    .clickable { onToday() }
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            )
            Spacer(Modifier.width(4.dp))
        }
        IconButton(onClick = onSettings) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "تنظیمات",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ───────────────────────────────────────────── تاریخ برگزیده

@Composable
private fun SelectedDateHeader(state: AppState, date: IranianDate, selected: Long) {
    val palette = LocalGahPalette.current
    val isToday = selected == state.todayEpochDay
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = state.digits(date.day),
                style = MaterialTheme.typography.displayMedium,
                color = palette.gold,
                fontSize = 52.sp
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                Text(
                    text = "${date.monthName} ${state.digits(IranianCalendar.pahlaviYear(date.year))}",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.weekdayName(selected),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isToday) {
                        Spacer(Modifier.width(6.dp))
                        Dot(color = palette.turquoise)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ArrowButton(icon = Icons.Filled.KeyboardArrowRight, label = "روز پیش") {
                    state.shiftDay(-1)
                }
                ArrowButton(icon = Icons.Filled.KeyboardArrowLeft, label = "روز بعد") {
                    state.shiftDay(1)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        if (date.isPanjeh) {
            Text(
                text = "پنجهٔ وهیک",
                style = MaterialTheme.typography.labelMedium,
                color = palette.gold,
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, palette.gold.copy(alpha = 0.6f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
            Spacer(Modifier.height(4.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = date.ritualLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.turquoise,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = date.dayMeaning,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun ArrowButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ───────────────────────────────────────────── ماه‌ها و روزها

@Composable
private fun MonthRibbon(state: AppState, year: Int, activeMonth: Int) {
    val palette = LocalGahPalette.current
    // ماهِ جاری از همان آغاز در ابتدای نوار می‌آید؛ با هر تغییر ماه هم دوباره
    val startIndex = (activeMonth - 1).coerceIn(0, 11)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    LaunchedEffect(activeMonth, year) {
        listState.scrollToItem(index = (activeMonth - 1).coerceIn(0, 11))
    }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { state.shiftYear(-1) }, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = "سال پیش",
                    tint = palette.gold
                )
            }
            Text(
                text = "سال ${state.digits(IranianCalendar.pahlaviYear(year))}",
                style = MaterialTheme.typography.titleMedium,
                color = palette.gold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = { state.shiftYear(1) }, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowLeft,
                    contentDescription = "سال پس",
                    tint = palette.gold
                )
            }
        }
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items((1..12).toList()) { index ->
                val active = index == activeMonth
                Text(
                    text = IranianCalendar.monthName(index),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (active) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (active) palette.gold else Color.Transparent
                        )
                        .border(
                            1.dp,
                            if (active) palette.gold else MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                        .clickable { state.showMonth(year, index) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun WeekdayRow(state: AppState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        for (index in 0..6) {
            Text(
                text = IranianCalendar.WEEKDAY_NAMES_SHORT[index],
                style = MaterialTheme.typography.labelSmall,
                color = if (index == 6) {
                    LocalGahPalette.current.pomegranate
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DayCell(
    state: AppState,
    day: Int,
    year: Int,
    month: Int,
    selected: Long,
    hasFestival: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = LocalGahPalette.current
    val epochDay = cellEpochDay(year, month, day)
    val isSelected = epochDay == selected
    val isToday = epochDay == state.todayEpochDay
    val isFriday = IranianCalendar.weekdayIndex(epochDay) == 6
    val shape = RoundedCornerShape(12.dp)
    // روزهای پنجه هم با شمارهٔ واقعی خود (۲۵ تا ۲۹/۳۰ سپندارمذ) نشان داده می‌شوند
    val isPanjehCell = day < 0
    val label = state.digits(if (day > 0) day else -day)

    Column(
        modifier = modifier
            .clip(shape)
            .background(
                when {
                    isSelected -> palette.gold
                    isToday -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            .border(
                1.dp,
                when {
                    isSelected -> palette.gold
                    isToday -> palette.turquoise
                    isPanjehCell -> palette.gold.copy(alpha = 0.55f)
                    else -> MaterialTheme.colorScheme.outlineVariant
                },
                shape
            )
            .clickable { state.select(epochDay) }
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 15.sp,
            color = when {
                isSelected -> palette.onGold
                isToday -> MaterialTheme.colorScheme.onSecondaryContainer
                isPanjehCell -> palette.gold
                isFriday -> palette.pomegranate
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
        Box(
            modifier = Modifier
                .padding(top = 3.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(
                    when {
                        !hasFestival -> Color.Transparent
                        isSelected -> palette.onGold
                        else -> palette.gold
                    }
                )
        )
    }
}

// ───────────────────────────────────────────── جشن‌های روز

@Composable
private fun DayFestivals(
    state: AppState,
    date: IranianDate,
    epochDay: Long,
    festivals: List<Festival>,
    onDetail: (Festival) -> Unit,
    onShare: () -> Unit
) {
    val palette = LocalGahPalette.current
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StarMotif(modifier = Modifier.size(12.dp), color = palette.gold)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "جشن و آیین این روز",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "اشتراک‌گذاری",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        if (festivals.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "این روز جشن ویژه‌ای در گاه‌شمار ایرانی ندارد.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "اما هر روز نامی دارد و نشانی: روز ${date.dayName} از ${date.monthName}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            festivals.forEach { festival ->
                FestivalCard(
                    festival = festival,
                    epochDay = epochDay,
                    state = state,
                    onClick = { onDetail(festival) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FestivalCard(
    festival: Festival,
    epochDay: Long,
    state: AppState,
    onClick: () -> Unit
) {
    val palette = LocalGahPalette.current
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, palette.cardBorder, shape)
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            KindBadge(kind = festival.kind)
            Spacer(Modifier.weight(1f))
            Text(
                text = PersianText.daysAway((epochDay - state.todayEpochDay).toInt(), state.persianDigits),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = festival.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = festival.summary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun KindBadge(kind: FestivalKind) {
    val palette = LocalGahPalette.current
    val color = when (kind) {
        FestivalKind.JASHN -> palette.saffron
        FestivalKind.AYIN -> palette.turquoise
        FestivalKind.GAHAMBAR -> palette.gold
        FestivalKind.TAJGOZARI -> palette.gold
        FestivalKind.ASATIR -> palette.saffron
        FestivalKind.YADBUD -> palette.pomegranate
    }
    Text(
        text = kind.label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = Modifier
            .clip(CircleShape)
            .border(1.dp, color.copy(alpha = 0.6f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
private fun UpcomingStrip(state: AppState, selected: Long) {
    val palette = LocalGahPalette.current
    val upcoming = remember(selected) { Festivals.upcoming(selected + 1, 3) }
    if (upcoming.isEmpty()) return
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StarMotif(modifier = Modifier.size(11.dp), color = palette.gold)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "جشن‌های پیشِ رو",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.height(6.dp))
        UpcomingRows(state = state, upcoming = upcoming, onSelect = { state.select(it) })
    }
}

@Composable
private fun UpcomingRows(
    state: AppState,
    upcoming: List<FestivalOccurrence>,
    onSelect: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        upcoming.forEach { item ->
            val date = IranianCalendar.fromEpochDay(item.epochDay)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelect(item.epochDay) }
                    .padding(vertical = 7.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (date.isPanjeh) "پ" else state.digits(date.day),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.festival.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${date.monthName} • ${state.weekdayName(item.epochDay)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = PersianText.daysAway(
                        (item.epochDay - state.todayEpochDay).toInt(),
                        state.persianDigits
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalGahPalette.current.turquoise
                )
            }
        }
    }
}

@Composable
private fun FooterNote(state: AppState) {
    Text(
        text = "سی روزِ نام‌دار و پنجهٔ وهیک؛ سال از اعتدال بهاری به وقت تهران آغاز می‌شود و " +
            "به سال پهلوی (۲۵۸۵) شمرده می‌شود.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 16.dp)
    )
}

// ───────────────────────────────────────────── برگه‌های پایین

@Composable
private fun FestivalSheetContent(state: AppState, festival: Festival, onClose: () -> Unit) {
    val palette = LocalGahPalette.current
    Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 30.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            KindBadge(kind = festival.kind)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "بستن",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = festival.name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = festival.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.turquoise
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = festival.detail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(14.dp))
        val epochDay = Festivals.dateInYear(festival, state.selectedDate.year)
        if (epochDay != null) {
            val date = IranianCalendar.fromEpochDay(epochDay)
            Text(
                text = "در سال ${state.digits(IranianCalendar.pahlaviYear(state.selectedDate.year))}: " +
                    "${state.digits(date.shortLabel())}، ${state.weekdayName(epochDay)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSheetContent(state: AppState, onClose: () -> Unit) {
    val palette = LocalGahPalette.current
    val context = LocalContext.current
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 30.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "تنظیمات",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "بستن",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "پوسته",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ThemeMode.values().forEach { mode ->
                ChoiceChip(
                    text = if (mode == ThemeMode.SEASONAL) "فصلی (${state.season.label})" else mode.label,
                    active = state.themeMode == mode,
                    onClick = { state.setThemeMode(mode) }
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "پوستهٔ «فصلی» با ماهِ برگزیده می‌گردد: بهار سبز، تابستان فیروزه‌ای، " +
                "پاییز نارنجی و زمستان آبیِ یخی. امروز: ${state.seasonToday.label}.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "تاریخ روی صفحه",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = when {
                    state.overlayLive -> "ایستاده بر صفحه"
                    state.overlayReady -> "سرویس روشن"
                    else -> "سرویس خاموش"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (state.overlayReady) palette.turquoise else palette.gold
            )
        }
        Spacer(Modifier.height(4.dp))
        SettingSwitch(
            title = "تاریخ بزرگ روی صفحه",
            subtitle = "متنِ درشت، بی هیچ اعلانی؛ برگهٔ اعلان‌ها و نوار وضعیت از اعلان پاک می‌مانَد",
            checked = state.overlayEnabled,
            onCheckedChange = { state.setOverlayEnabled(it) }
        )

        if (state.overlayEnabled && !state.overlayReady) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "اندروید برای این کار به سرویس دسترس‌پذیری نیاز دارد. یک‌بار «گاه‌شمار " +
                    "ایرانی» را در تنظیمات دسترس‌پذیری روشن کنید؛ سرویس هیچ محتوایی از صفحه " +
                    "نمی‌خواند، هیچ اطلاعاتی نمی‌فرستد و تنها یک متن می‌کشد.",
                style = MaterialTheme.typography.labelSmall,
                color = palette.gold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "روشن‌کردن سرویس دسترس‌پذیری",
                style = MaterialTheme.typography.labelLarge,
                color = palette.onGold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(palette.gold)
                    .clickable {
                        runCatching {
                            context.startActivity(
                                Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            )
                        }
                    }
                    .padding(vertical = 11.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "اگر کلیدِ روشن‌کردن خاکستری است: در همان صفحه، کنار نام برنامه سه‌نقطه ← " +
                    "«اجازهٔ تنظیمات محدود» را بزنید. اندروید ۱۳ به بالا این را برای برنامه‌هایی " +
                    "که از بیرونِ فروشگاه نصب می‌شوند لازم می‌داند.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (state.overlayEnabled && state.overlayReady) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "اندازهٔ متن",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "نمونه",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OverlaySize.values().forEach { size ->
                    ChoiceChip(
                        text = size.label,
                        active = state.overlaySize == size,
                        onClick = { state.setOverlaySize(size) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = PersianText.statusLabel(
                        state.todayDate,
                        state.persianDigits,
                        state.overlayShort
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.gold,
                    fontSize = state.overlaySize.sp.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = "جای تاریخ",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            OverlaySpot.values().toList().chunked(3).forEachIndexed { index, rowSpots ->
                if (index > 0) Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    rowSpots.forEach { spot ->
                        ChoiceChip(
                            text = spot.label,
                            active = state.overlaySpot == spot,
                            onClick = { state.setOverlaySpot(spot) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            SettingSwitch(
                title = "متن کوتاه",
                subtitle = if (state.overlayShort) "۸ آبان" else "دی به آذر ۸ آبان",
                checked = state.overlayShort,
                onCheckedChange = { state.setOverlayShort(it) }
            )
            SettingSwitch(
                title = "سال پهلوی هم نوشته شود",
                subtitle = "۸ آبان • ۲۵۸۵",
                checked = state.overlayShowYear,
                onCheckedChange = { state.setOverlayShowYear(it) }
            )
            SettingSwitch(
                title = "زمینهٔ نیم‌شفاف پشت متن",
                subtitle = "روی هر تصویری خوانا می‌مانَد",
                checked = state.overlayPlate,
                onCheckedChange = { state.setOverlayPlate(it) }
            )
            SettingSwitch(
                title = "فقط هنگام قفل بودن صفحه",
                subtitle = "بیرون از قفل، تاریخ پنهان می‌شود",
                checked = state.overlayLockOnly,
                onCheckedChange = { state.setOverlayLockOnly(it) }
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "بررسی دوباره",
                style = MaterialTheme.typography.labelMedium,
                color = palette.gold,
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, palette.gold.copy(alpha = 0.6f), CircleShape)
                    .clickable { state.refreshOverlayReady() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = "صفحهٔ همیشه‌روشن (AOD)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "صفحهٔ همیشه‌روشن را سامانه می‌کشد و هیچ برنامه‌ای اجازهٔ نوشتن بر آن را ندارد؛ " +
                "تنها چیزی که آنجا از برنامه‌ها دیده می‌شود نشانِ ریزِ اعلان است — همان که " +
                "ناخوانا بود. نزدیک‌ترین جایگزین، «محافظ صفحه»ٔ اندروید است: تاریخ با حروف " +
                "بسیار درشت بر زمینهٔ سیاه. در تنظیماتِ محافظ صفحه، «گاه‌شمار ایرانی — تاریخ " +
                "درشت» را برگزینید و زمانِ آغازش را «هنگام شارژ» یا «هنگام شارژ یا بی‌کاری» بگذارید.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "گشودن تنظیماتِ محافظ صفحه",
            style = MaterialTheme.typography.labelLarge,
            color = palette.onGold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(palette.gold)
                .clickable {
                    runCatching {
                        context.startActivity(Intent(android.provider.Settings.ACTION_DREAM_SETTINGS))
                    }
                }
                .padding(vertical = 11.dp)
        )

        Spacer(Modifier.height(14.dp))
        SettingSwitch(
            title = "رقم‌های فارسی",
            subtitle = "نمایش ۱۲۳ به‌جای 123",
            checked = state.persianDigits,
            onCheckedChange = { state.setPersianDigits(it) }
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StarMotif(modifier = Modifier.size(11.dp), color = palette.gold)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "درباره",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "گاه‌شمار ایرانی، گاه‌شماری کهن ایران‌زمین است: دوازده ماه با نام‌های " +
                "اوستایی، سی روزِ نام‌دار و پنجهٔ وهیک. سال در لحظهٔ اعتدال بهاری به وقت " +
                "تهران آغاز می‌شود و به سال پهلوی (۱۴۰۵ ایرانی = ۲۵۸۵ پهلوی) شمرده " +
                "می‌شود؛ سال هجری خورشیدی به کار نمی‌رود. همهٔ داده‌ها در خود برنامه‌اند و " +
                "برنامه بدون اینترنت کار می‌کند.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "نسخهٔ ${state.digits(appVersion(context))}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChoiceChip(text: String, active: Boolean, onClick: () -> Unit) {
    val palette = LocalGahPalette.current
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = if (active) palette.onGold else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (active) palette.gold else Color.Transparent)
            .border(
                1.dp,
                if (active) palette.gold else MaterialTheme.colorScheme.outline,
                CircleShape
            )
            .clickable { onClick() }
            .padding(horizontal = 11.dp, vertical = 6.dp)
    )
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// ───────────────────────────────────────────── پس‌زمینه و ابزارها

/** آسمان شبانه: ستاره‌های کم‌رنگ و هالهٔ زر */
@Composable
private fun StarryBackdrop(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val radius = 0.7f * density
        for (index in 0 until 46) {
            val fx = ((index * 73 + 11) % 97) / 97f
            val fy = ((index * 37 + 7) % 89) / 89f
            val alpha = if (index % 4 == 0) 0.20f else 0.10f
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = if (index % 6 == 0) radius * 1.8f else radius,
                center = Offset(fx * size.width, fy * size.height)
            )
        }
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.10f), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.12f),
                radius = size.width * 0.6f
            ),
            radius = size.width * 0.6f,
            center = Offset(size.width * 0.85f, size.height * 0.12f)
        )
    }
}

private fun share(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری"))
}

private fun appVersion(context: Context): String = runCatching {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "۱.۰.۰"
}.getOrDefault("۱.۰.۰")
