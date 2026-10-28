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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import ir.gahshomar.parsi.data.Article
import ir.gahshomar.parsi.data.ArticleCategory
import ir.gahshomar.parsi.data.Articles
import ir.gahshomar.parsi.ui.AppState
import ir.gahshomar.parsi.ui.LocalGahPalette
import ir.gahshomar.parsi.ui.Screen
import ir.gahshomar.parsi.ui.StarMotif
import ir.gahshomar.parsi.ui.components.GahCard
import ir.gahshomar.parsi.ui.components.SectionTitle
import ir.gahshomar.parsi.ui.components.SegmentedRow

@Composable
fun CultureScreen(state: AppState) {
    val palette = LocalGahPalette.current
    var selectedCategory by remember { mutableStateOf(0) }
    val categories = ArticleCategory.values()
    val articles = Articles.byCategory(categories[selectedCategory])

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
                        Text(text = "فرهنگ و آیین", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = "آیین‌ها، هنرها و دانستنی‌های ایران‌زمین",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                SegmentedRow(
                    options = categories.map { it.label },
                    selectedIndex = selectedCategory,
                    onSelect = { selectedCategory = it }
                )
            }
        }

        items(articles.size) { index ->
            val article = articles[index]
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                GahCard(onClick = { state.push(Screen.Article(article.id)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = article.category.emoji, fontSize = 19.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = article.title, style = MaterialTheme.typography.titleMedium)
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
                GahCard(background = palette.headerTop) {
                    Text(
                        text = "ایران، سرزمین آیین‌ها",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.onHeader
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "از نوروز و مهرگان تا باغ ایرانی و فرش و موسیقی، همهٔ آنچه " +
                            "هویت ایرانی را می‌سازد، در این بخش گرد آمده است. " +
                            "همهٔ نوشتارها در خود برنامه ذخیره‌اند و بدون اینترنت خوانده می‌شوند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.onHeader.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
fun ArticleScreen(state: AppState, article: Article) {
    val palette = LocalGahPalette.current
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scroll)
    ) {
        ir.gahshomar.parsi.ui.ArchHeader(height = 178.dp) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
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
                        text = article.category.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.turquoise,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = palette.onHeader
                )
                Text(
                    text = article.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.gold
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            article.paragraphs.forEach { paragraph ->
                Text(
                    text = paragraph,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (article.bullets.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                SectionTitle("در یک نگاه")
                Spacer(Modifier.height(6.dp))
                GahCard {
                    article.bullets.forEach { (term, meaning) ->
                        Row(modifier = Modifier.padding(vertical = 5.dp)) {
                            StarMotif(modifier = Modifier.padding(top = 6.dp).size(7.dp), color = palette.gold)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(text = term, style = MaterialTheme.typography.titleSmall)
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

            if (article.quote != null) {
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(palette.headerTop)
                        .padding(16.dp)
                ) {
                    Column {
                        StarMotif(modifier = Modifier.size(16.dp), color = palette.gold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = article.quote ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.onHeader,
                            lineHeight = 30.sp
                        )
                        if (article.quoteSource != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "— " + article.quoteSource,
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.gold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "گاهشمار پارسی • فرهنگ و آیین ایران",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(bottom = 26.dp)
            )
        }
    }
}
