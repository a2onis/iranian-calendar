package ir.gahshomar.parsi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.dp
import ir.gahshomar.parsi.core.PersianCalendar
import ir.gahshomar.parsi.core.PersianText
import ir.gahshomar.parsi.data.EventCategory
import ir.gahshomar.parsi.data.Events
import ir.gahshomar.parsi.data.Observance
import ir.gahshomar.parsi.ui.AppState
import ir.gahshomar.parsi.ui.LocalGahPalette
import ir.gahshomar.parsi.ui.StarMotif
import ir.gahshomar.parsi.ui.components.GahCard
import ir.gahshomar.parsi.ui.components.Pill
import ir.gahshomar.parsi.ui.components.SectionTitle

@Composable
fun EventsScreen(state: AppState) {
    val palette = LocalGahPalette.current
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<EventCategory?>(null) }
    var bookmarksOnly by remember { mutableStateOf(false) }
    val persianYear = state.currentDate().year

    val occurrences = remember(persianYear, state.showAstronomy) {
        Events.yearOccurrences(persianYear)
    }

    val filtered = occurrences.filter { occurrence ->
        (category == null || occurrence.event.category == category) &&
            (!bookmarksOnly || state.isBookmarked(occurrence.event.id)) &&
            (query.isBlank() || Events.search(query).any { it.id == occurrence.event.id })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StarMotif(modifier = Modifier.size(20.dp), color = palette.gold)
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "مناسبت‌های ایران", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = "جشن‌ها، آیین‌های کهن و بزرگداشت‌های سال " + state.digits(persianYear),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("جست‌وجو در مناسبت‌ها…") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "پاک کردن")
                            }
                        }
                    }
                )
            }
        }

        item {
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    CategoryChip(
                        label = "همه",
                        selected = category == null && !bookmarksOnly,
                        onClick = { category = null; bookmarksOnly = false }
                    )
                }
                item {
                    CategoryChip(
                        label = "★ نشان‌شده‌ها",
                        selected = bookmarksOnly,
                        onClick = { bookmarksOnly = !bookmarksOnly }
                    )
                }
                items(EventCategory.values().toList()) { item ->
                    CategoryChip(
                        label = item.emoji + " " + item.label,
                        selected = category == item,
                        onClick = { category = if (category == item) null else item }
                    )
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }

        items((1..12).toList()) { month ->
            val monthItems = filtered.filter {
                PersianCalendar.fromEpochDay(it.epochDay).month == month
            }
            if (monthItems.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SectionTitle(PersianText.monthName(month) + " • " + state.digits(monthItems.size) + " مناسبت")
                    Spacer(Modifier.height(6.dp))
                    GahCard {
                        monthItems.forEach { occurrence ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { state.openDay(occurrence.epochDay) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    modifier = Modifier
                                        .width(42.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = state.digits(occurrence.persianDate.day),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = PersianCalendar.WEEKDAY_NAMES_SHORT[
                                            PersianCalendar.weekdayIndex(occurrence.epochDay)
                                        ],
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.width(9.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = occurrence.event.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (occurrence.event.observance == Observance.NONE)
                                            MaterialTheme.colorScheme.onSurface else palette.holiday
                                    )
                                    Text(
                                        text = occurrence.event.summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(modifier = Modifier.padding(top = 4.dp)) {
                                        Pill(text = occurrence.event.category.label, color = palette.gold)
                                        if (occurrence.event.observance != Observance.NONE) {
                                            Spacer(Modifier.width(5.dp))
                                            Pill(
                                                text = occurrence.event.observance.label,
                                                color = palette.pomegranate
                                            )
                                        }
                                    }
                                }
                                IconButton(onClick = { state.toggleBookmark(occurrence.event.id) }) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = "نشان",
                                        tint = if (state.isBookmarked(occurrence.event.id)) palette.gold
                                        else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "مناسبتی با این ویژگی یافت نشد.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val palette = LocalGahPalette.current
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else palette.turquoise
        )
    }
}
