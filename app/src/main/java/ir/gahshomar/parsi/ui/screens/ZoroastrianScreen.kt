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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ir.gahshomar.parsi.core.PersianCalendar
import ir.gahshomar.parsi.core.PersianText
import ir.gahshomar.parsi.core.ZoroastrianCalendar
import ir.gahshomar.parsi.data.ArticleCategory
import ir.gahshomar.parsi.data.Articles
import ir.gahshomar.parsi.data.Events
import ir.gahshomar.parsi.ui.AppState
import ir.gahshomar.parsi.ui.LocalGahPalette
import ir.gahshomar.parsi.ui.Screen
import ir.gahshomar.parsi.ui.StarMotif
import ir.gahshomar.parsi.ui.components.GahCard
import ir.gahshomar.parsi.ui.components.Pill
import ir.gahshomar.parsi.ui.components.SectionTitle

@Composable
fun ZoroastrianScreen(state: AppState) {
    val palette = LocalGahPalette.current
    val articles = Articles.byCategory(ArticleCategory.ZOROASTRIAN)
    val persianYear = state.currentDate().year

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
                    Column {
                        Text(text = "دین زرتشتی", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = "آیین کهن ایران؛ از گاهان زرتشت تا آتشگاه‌های امروز",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                GahCard(background = palette.headerTop) {
                    Text(
                        text = "اندیشهٔ نیک، گفتار نیک، کردار نیک",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.gold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "زرتشتی‌گری، دین ایران باستان، بر یگانگی اورمزد، آزادی " +
                            "گزینش انسان و پاسداشت آب، آتش، خاک و گیاه استوار است. " +
                            "آیین‌ها و جشن‌های آن، هنوز در تقویم و فرهنگ ایرانی زنده‌اند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.onHeader.copy(alpha = 0.88f)
                    )
                }
            }
        }

        item {
            SectionTitle(
                text = "امشاسپندان و فروزه‌های اورمزد",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
        item {
            val bullets = Articles.byId("ahura-mazda")?.bullets.orEmpty()
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                GahCard {
                    bullets.forEach { (name, meaning) ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            StarMotif(modifier = Modifier.padding(top = 6.dp).size(7.dp), color = palette.gold)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(text = name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = meaning,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionTitle(
                text = "سی روز ماه به نام ایزدان",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                GahCard {
                    ZoroastrianCalendar.DAY_NAMES.toList().chunked(3).forEachIndexed { rowIndex, row ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            row.forEachIndexed { columnIndex, name ->
                                val dayNumber = rowIndex * 3 + columnIndex + 1
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = state.digits(dayNumber) + ". " + name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = palette.turquoise
                                    )
                                    Text(
                                        text = ZoroastrianCalendar.dayMeaning(dayNumber),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (row.size < 3) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        item {
            SectionTitle(
                text = "جشن‌های ماهانه (روز و ماه هم‌نام)",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                GahCard {
                    ZoroastrianCalendar.NAME_DAY_FESTIVALS.forEach { festival ->
                        val epochDay = PersianCalendar.toEpochDay(persianYear, festival.month, festival.day)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { state.openDay(epochDay) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .width(46.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = state.digits(festival.day),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = PersianText.monthName(festival.month),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(9.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = festival.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = festival.meaning,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionTitle(
                text = "شش گاهنبار سال",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                GahCard {
                    ZoroastrianCalendar.GAHAMBARS.forEach { gahambar ->
                        Row(modifier = Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Pill(text = gahambar.name, color = palette.gold)
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = gahambar.seasonTime, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = gahambar.creation,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionTitle(
                text = "پنج گاه روز",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                GahCard {
                    ZoroastrianCalendar.GAHS.forEach { gah ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            StarMotif(modifier = Modifier.padding(top = 6.dp).size(6.dp), color = palette.gold)
                            Spacer(Modifier.width(8.dp))
                            Text(text = gah, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        item {
            SectionTitle(
                text = "نوشتارها",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        items(articles.size) { index ->
            val article = articles[index]
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                GahCard(onClick = { state.push(Screen.Article(article.id)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🔥", style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(Modifier.width(9.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = article.title, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = article.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                GahCard(background = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(
                        text = "زرتشتیان امروز",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "امروز حدود صد تا دویست هزار زرتشتی در جهان زندگی می‌کنند؛ " +
                            "در ایران بیشتر در یزد، کرمان و تهران. آتشگاه یزد و آتشگاه کرمان " +
                            "از مهم‌ترین نیایشگاه‌های آنان‌اند و آیین‌های نوروز، مهرگان، " +
                            "سده و گاهنبارها همچنان برپا می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Pill(text = "یزد", color = palette.turquoise)
                        Spacer(Modifier.width(6.dp))
                        Pill(text = "کرمان", color = palette.turquoise)
                        Spacer(Modifier.width(6.dp))
                        Pill(text = "تهران", color = palette.turquoise)
                    }
                }
                Spacer(Modifier.height(10.dp))
                val today = state.todayEpochDay
                val todayEvents = Events.forEpochDay(today).size
                Text(
                    text = if (todayEvents > 0)
                        "امروز " + state.digits(todayEvents) + " مناسبت در تقویم ثبت شده است."
                    else "امروز مناسبت ویژه‌ای در تقویم نیست.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
