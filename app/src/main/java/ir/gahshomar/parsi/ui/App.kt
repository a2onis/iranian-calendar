package ir.gahshomar.parsi.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.gahshomar.parsi.data.Articles
import ir.gahshomar.parsi.ui.screens.AboutScreen
import ir.gahshomar.parsi.ui.screens.AncientScreen
import ir.gahshomar.parsi.ui.screens.ArticleScreen
import ir.gahshomar.parsi.ui.screens.CalendarScreen
import ir.gahshomar.parsi.ui.screens.ConverterScreen
import ir.gahshomar.parsi.ui.screens.CultureScreen
import ir.gahshomar.parsi.ui.screens.DayScreen
import ir.gahshomar.parsi.ui.screens.EventsScreen
import ir.gahshomar.parsi.ui.screens.SettingsScreen
import ir.gahshomar.parsi.ui.screens.ZoroastrianScreen

private fun Screen.isTab(): Boolean = this is Screen.Calendar || this is Screen.Events ||
    this is Screen.Culture || this is Screen.Ancient || this is Screen.Zoroastrian

@Composable
fun GahshomarApp(state: AppState) {
    GahshomarTheme(darkTheme = state.themeIsDark(isSystemInDarkTheme())) {
        BackHandler(enabled = state.backStack.size > 1) {
            state.back()
        }
        val screen = state.currentScreen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                when (screen) {
                    is Screen.Calendar -> CalendarScreen(state)
                    is Screen.Events -> EventsScreen(state)
                    is Screen.Culture -> CultureScreen(state)
                    is Screen.Ancient -> AncientScreen(state)
                    is Screen.Zoroastrian -> ZoroastrianScreen(state)
                    is Screen.Day -> DayScreen(state)
                    is Screen.Settings -> SettingsScreen(state)
                    is Screen.Converter -> ConverterScreen(state)
                    is Screen.About -> AboutScreen(state)
                    is Screen.Article -> {
                        val article = Articles.byId(screen.id)
                        if (article != null) {
                            ArticleScreen(state, article)
                        } else {
                            MissingScreen(state)
                        }
                    }
                }
            }
            if (screen.isTab()) {
                BottomBar(state)
            }
        }
    }
}

@Composable
private fun MissingScreen(state: AppState) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            StarMotif(modifier = Modifier.size(30.dp), color = LocalGahPalette.current.gold)
            Spacer(Modifier.height(8.dp))
            Text(text = "این بخش یافت نشد.", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "بازگشت به تقویم",
                style = MaterialTheme.typography.bodyMedium,
                color = LocalGahPalette.current.turquoise,
                modifier = Modifier.clickable { state.openTab(Screen.Calendar) }
            )
        }
    }
}

private data class TabItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector?
)

@Composable
private fun BottomBar(state: AppState) {
    val palette = LocalGahPalette.current
    val tabs = listOf(
        TabItem(Screen.Calendar, "تقویم", Icons.Filled.DateRange),
        TabItem(Screen.Events, "مناسبت‌ها", Icons.Filled.Star),
        TabItem(Screen.Culture, "فرهنگ", Icons.Filled.Favorite),
        TabItem(Screen.Ancient, "باستان", Icons.Filled.Place),
        TabItem(Screen.Zoroastrian, "زرتشتی", null)
    )
    val current = state.currentScreen
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(palette.gold.copy(alpha = 0.35f))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = current == tab.screen
                val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { state.openTab(tab.screen) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (tab.icon != null) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = tint,
                            modifier = Modifier.size(21.dp)
                        )
                    } else {
                        FlameIcon(modifier = Modifier.size(19.dp), color = tint)
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = tint
                    )
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(if (selected) palette.gold else Color.Transparent)
                    )
                }
            }
        }
    }
}
