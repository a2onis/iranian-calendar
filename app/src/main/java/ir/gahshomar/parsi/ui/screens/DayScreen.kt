package ir.gahshomar.parsi.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.gahshomar.parsi.core.GlobalSymbols
import ir.gahshomar.parsi.core.HijriCalendar
import ir.gahshomar.parsi.core.PersianCalendar
import ir.gahshomar.parsi.core.PersianText
import ir.gahshomar.parsi.core.SolarMoment
import ir.gahshomar.parsi.core.ZoroastrianCalendar
import ir.gahshomar.parsi.data.Events
import ir.gahshomar.parsi.data.Observance
import ir.gahshomar.parsi.ui.AppState
import ir.gahshomar.parsi.ui.LocalGahPalette
import ir.gahshomar.parsi.ui.Screen
import ir.gahshomar.parsi.ui.StarMotif
import ir.gahshomar.parsi.ui.components.ActionChip
import ir.gahshomar.parsi.ui.components.EventRow
import ir.gahshomar.parsi.ui.components.GahCard
import ir.gahshomar.parsi.ui.components.InfoRow
import ir.gahshomar.parsi.ui.components.SectionTitle
import ir.gahshomar.parsi.ui.components.SoftDividerRow

@Composable
fun DayScreen(state: AppState) {
    val epochDay = (state.currentScreen as? Screen.Day)?.epochDay ?: state.selectedEpochDay
    val context = LocalContext.current
    val palette = LocalGahPalette.current
    val date = PersianCalendar.fromEpochDay(epochDay)
    val gregorian = date.toGregorian()
    val hijri = HijriCalendar.fromEpochDay(epochDay, state.hijriOffset)
    val events = remember(epochDay, state.showAstronomy) { Events.forEpochDay(epochDay, state.showAstronomy) }
    val moon = remember(epochDay) { GlobalSymbols.moonPhase(epochDay * 86400000L) }
    val relative = PersianText.relativeLabel(epochDay, state.todayEpochDay)
    var note by remember(epochDay) { mutableStateOf(state.noteFor(epochDay)) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 26.dp)
    ) {
        item {
            ir.gahshomar.parsi.ui.ArchHeader(height = 200.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { state.back() }) {
                            Icon(
                                Icons.Filled.KeyboardArrowRight,
                                contentDescription = "بازگشت",
                                tint = palette.gold
                            )
                        }
                        Text(
                            text = "روزنگار",
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.onHeader,
                            modifier = Modifier.weight(1f)
                        )
                        if (relative.isNotEmpty()) {
                            Text(
                                text = relative,
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.turquoise
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = PersianText.weekdayName(epochDay) + " " + state.digits(date.day),
                        style = MaterialTheme.typography.displaySmall,
                        color = palette.onHeader
                    )
                    Text(
                        text = PersianText.monthName(date.month) + " " + state.digits(date.year),
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.gold
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = PersianText.formatGregorian(gregorian, state.persianDigits),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.onHeader.copy(alpha = 0.8f)
                    )
                    Text(
                        text = PersianText.formatHijri(hijri, state.persianDigits),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.onHeader.copy(alpha = 0.8f)
                    )
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        state.selectedEpochDay = epochDay - 1
                        state.syncViewsToDate(epochDay - 1)
                        state.replace(Screen.Day(epochDay - 1))
                    }) {
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "روز پیش", tint = palette.turquoise)
                    }
                    Text(
                        text = "گاه‌شماری‌ها",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    ActionChip(text = "برو به تقویم", onClick = {
                        state.selectedEpochDay = epochDay
                        state.syncViewsToDate(epochDay)
                        state.openTab(Screen.Calendar)
                    })
                    IconButton(onClick = {
                        state.selectedEpochDay = epochDay + 1
                        state.syncViewsToDate(epochDay + 1)
                        state.replace(Screen.Day(epochDay + 1))
                    }) {
                        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "روز بعد", tint = palette.turquoise)
                    }
                }
                GahCard {
                    InfoRow("خورشیدی", PersianText.formatDate(date, state.persianDigits) + " • " + PersianText.weekdayName(epochDay))
                    InfoRow("میلادی", PersianText.formatGregorian(gregorian, state.persianDigits) + " • " + gregorian.toString())
                    InfoRow("هجری قمری", PersianText.formatHijri(hijri, state.persianDigits))
                    InfoRow(
                        "زرتشتی",
                        "روز " + ZoroastrianCalendar.dayName(date.day) + " — " +
                            ZoroastrianCalendar.dayMeaning(date.day)
                    )
                    InfoRow("اوستایی ماه", PersianText.monthNameAvestan(date.month))
                    InfoRow("برج", PersianCalendar.ZODIAC_NAMES[(date.month - 1).coerceIn(0, 11)])
                    InfoRow("فصل", PersianCalendar.seasonOf(date.month).title)
                    SoftDividerRow()
                    InfoRow("روز سال", state.digits(date.dayOfYear) + " از " + state.digits(PersianCalendar.yearLength(date.year)))
                    InfoRow("هفتهٔ سال", state.digits(PersianCalendar.weekOfYear(date)))
                    InfoRow("هفتهٔ ISO", state.digits(PersianCalendar.isoWeek(epochDay)) + " از سال " + state.digits(PersianCalendar.isoWeekYear(epochDay)))
                    InfoRow("روز جولیَن", state.digits(epochDay + ir.gahshomar.parsi.core.CivilMath.JDN_1970_01_01))
                    InfoRow("ماه قمری", state.digits(HijriCalendar.monthLength(hijri.year, hijri.month)) + " روزه")
                    InfoRow("نماد چینی", GlobalSymbols.chineseCycle(gregorian.year))
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                SectionTitle("چرخهٔ کیهانی")
                Spacer(Modifier.height(6.dp))
                GahCard {
                    InfoRow("ماه", moon.name + " • " + state.digits(moon.illuminationPercent) + "٪ روشنایی")
                    listOf(0, 1, 2, 3).forEach { quarter ->
                        val moment = SolarMoment(
                            listOf("اعتدال بهاری", "انقلاب تابستانی", "اعتدال پاییزی", "انقلاب زمستانی")[quarter],
                            quarter,
                            gregorian.year
                        )
                        InfoRow(
                            moment.title,
                            PersianText.formatTehranTime(moment.utcMillis, state.persianDigits) + " • " +
                                PersianText.formatDate(
                                    PersianCalendar.fromEpochDay(moment.epochDay),
                                    state.persianDigits
                                )
                        )
                    }
                    Text(
                        text = "لحظه‌ها به وقت تهران و بر پایهٔ روش نجومی مئوس محاسبه شده‌اند (دقت چند دقیقه).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                SectionTitle("مناسبت‌های این روز")
                Spacer(Modifier.height(6.dp))
                GahCard {
                    if (events.isEmpty()) {
                        Text(
                            text = "برای این روز مناسبتی ثبت نشده است.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    events.forEach { event ->
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = event.category.emoji + "  " + event.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (event.observance == Observance.NONE)
                                        MaterialTheme.colorScheme.onSurface else palette.holiday,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { state.toggleBookmark(event.id) }) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = "نشان‌کردن",
                                        tint = if (state.isBookmarked(event.id)) palette.gold
                                        else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                            Text(
                                text = event.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (event.detail.isNotBlank()) {
                                Text(
                                    text = event.detail,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 5.dp)
                                )
                            }
                            if (event.articleId != null) {
                                Spacer(Modifier.height(6.dp))
                                ActionChip(text = "بیشتر بخوانید", onClick = { state.push(Screen.Article(event.articleId)) })
                            }
                            SoftDividerRow()
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                SectionTitle("یادداشت من")
                Spacer(Modifier.height(6.dp))
                GahCard {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("یادداشتی برای این روز بنویسید…") },
                        minLines = 3
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionChip(
                            text = "ذخیره",
                            filled = true,
                            onClick = { state.setNote(epochDay, note) }
                        )
                        ActionChip(
                            text = "اشتراک‌گذاری",
                            icon = Icons.Filled.Share,
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, dayShareText(epochDay, state.persianDigits))
                                }
                                runCatching { context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری")) }
                            }
                        )
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StarMotif(modifier = Modifier.size(16.dp), color = palette.gold)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "زمان تحویل سال " + state.digits(date.year) + ": " +
                        PersianText.formatTehranTime(
                            PersianCalendar.nowruzMomentUtcMillis(date.year),
                            state.persianDigits
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
