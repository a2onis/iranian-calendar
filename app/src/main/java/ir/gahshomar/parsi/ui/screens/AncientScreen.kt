package ir.gahshomar.parsi.ui.screens

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ir.gahshomar.parsi.data.ArticleCategory
import ir.gahshomar.parsi.data.Articles
import ir.gahshomar.parsi.data.Timeline
import ir.gahshomar.parsi.ui.AppState
import ir.gahshomar.parsi.ui.LocalGahPalette
import ir.gahshomar.parsi.ui.Screen
import ir.gahshomar.parsi.ui.StarMotif
import ir.gahshomar.parsi.ui.components.GahCard
import ir.gahshomar.parsi.ui.components.SectionTitle

@Composable
fun AncientScreen(state: AppState) {
    val palette = LocalGahPalette.current
    val eras = Timeline.byEra()
    val articles = Articles.byCategory(ArticleCategory.ANCIENT)

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
                        Text(text = "ایران باستان", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = "سه هزار سال تمدن، از شهر سوخته تا تیسفون",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                GahCard(background = palette.headerTop) {
                    Text(
                        text = "تمدن ایران، یکی از کهن‌ترین تمدن‌های پیوستهٔ جهان",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.onHeader
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "از شهرهای پیش از تاریخ در سیستان و کاشان تا شاهنشاهی‌های " +
                            "ماد، هخامنشی، اشکانی و ساسانی، ایران زمینِ اندیشه، داد، " +
                            "معماری، هنر و دانش بوده است. این بخش، خط زمانی ایران را " +
                            "از اسطوره تا تاریخ روایت می‌کند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.onHeader.copy(alpha = 0.85f)
                    )
                }
            }
        }

        item {
            SectionTitle(
                text = "خط زمان ایران",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        eras.forEach { pair ->
            val era = pair.first
            val entries = pair.second
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = era.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(palette.gold.copy(alpha = 0.4f))
                    )
                }
            }
            items(entries.size) { index ->
                val entry = entries[index]
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    GahCard {
                        Row(verticalAlignment = Alignment.Top) {
                            Column(
                                modifier = Modifier
                                    .width(70.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(vertical = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = state.text(entry.year),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.turquoise
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = entry.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = entry.text,
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
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SectionTitle("بیشتر بخوانید")
                Spacer(Modifier.height(6.dp))
                articles.forEach { article ->
                    GahCard(
                        modifier = Modifier.padding(bottom = 6.dp),
                        onClick = { state.push(Screen.Article(article.id)) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StarMotif(modifier = Modifier.size(12.dp), color = palette.gold)
                            Spacer(Modifier.width(8.dp))
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
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "«توانا بود هر که دانا بود» — فردوسی",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
