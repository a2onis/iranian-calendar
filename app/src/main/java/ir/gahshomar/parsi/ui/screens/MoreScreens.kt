package ir.gahshomar.parsi.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.gahshomar.parsi.BuildConfig
import ir.gahshomar.parsi.core.CivilMath
import ir.gahshomar.parsi.core.GlobalSymbols
import ir.gahshomar.parsi.core.HijriCalendar
import ir.gahshomar.parsi.core.HijriDate
import ir.gahshomar.parsi.core.PersianCalendar
import ir.gahshomar.parsi.core.PersianDate
import ir.gahshomar.parsi.core.PersianText
import ir.gahshomar.parsi.core.ZoroastrianCalendar
import ir.gahshomar.parsi.data.Articles
import ir.gahshomar.parsi.ui.AppState
import ir.gahshomar.parsi.ui.LocalGahPalette
import ir.gahshomar.parsi.ui.Screen
import ir.gahshomar.parsi.ui.StarMotif
import ir.gahshomar.parsi.ui.ThemeMode
import ir.gahshomar.parsi.ui.components.ActionChip
import ir.gahshomar.parsi.ui.components.GahCard
import ir.gahshomar.parsi.ui.components.InfoRow
import ir.gahshomar.parsi.ui.components.SectionTitle
import ir.gahshomar.parsi.ui.components.SegmentedRow
import ir.gahshomar.parsi.ui.components.SettingsToggle
import ir.gahshomar.parsi.ui.components.SoftDividerRow

@Composable
private fun ScreenHeader(title: String, subtitle: String? = null, onBack: () -> Unit) {
    val palette = LocalGahPalette.current
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "بازگشت", tint = palette.turquoise)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            StarMotif(modifier = Modifier.size(16.dp), color = palette.gold)
        }
    }
}

@Composable
private fun StepperRow(
    title: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        IconButton(onClick = onDecrease) {
            Text(text = "−", style = MaterialTheme.typography.titleLarge)
        }
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onIncrease) {
            Text(text = "+", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun SettingsScreen(state: AppState) {
    val scroll = rememberScrollState()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* نتیجهٔ اجازه، خودکار در سامانه اعمال می‌شود */ }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scroll)
    ) {
        ScreenHeader("تنظیمات", "برنامه را به سلیقهٔ خود درآورید", onBack = { state.back() })
        Column(modifier = Modifier.padding(horizontal = 14.dp)) {

            SectionTitle("پوسته")
            Spacer(Modifier.height(6.dp))
            SegmentedRow(
                options = ThemeMode.values().map { it.label },
                selectedIndex = ThemeMode.values().indexOf(state.themeMode),
                onSelect = { state.setThemeMode(ThemeMode.values()[it]) }
            )

            Spacer(Modifier.height(6.dp))
            SoftDividerRow()

            SectionTitle("گاه‌شماری")
            Spacer(Modifier.height(4.dp))
            GahCard {
                Text(text = "آغاز هفته", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(6.dp))
                SegmentedRow(
                    options = listOf("شنبه", "یکشنبه"),
                    selectedIndex = if (state.weekStartsSaturday) 0 else 1,
                    onSelect = { state.setWeekStartsSaturday(it == 0) }
                )
                Spacer(Modifier.height(6.dp))
                SettingsToggle(
                    title = "نمایش عدد میلادی زیر روز",
                    subtitle = "در خانه‌های تقویم خورشیدی",
                    checked = state.showGregorianNumbers,
                    onCheckedChange = { state.setShowGregorianNumbers(it) }
                )
                SettingsToggle(
                    title = "نام روز زرتشتی",
                    subtitle = "مانند «روز مهر از ماه مهر»",
                    checked = state.showZoroastrianDay,
                    onCheckedChange = { state.setShowZoroastrianDay(it) }
                )
                SettingsToggle(
                    title = "مناسبت‌های نجومی",
                    subtitle = "اعتدالین، انقلابین و نوروز نجومی",
                    checked = state.showAstronomy,
                    onCheckedChange = { state.setShowAstronomy(it) }
                )
                Spacer(Modifier.height(4.dp))
                StepperRow(
                    title = "تصحیح هجری قمری (روز)",
                    value = state.digits(state.hijriOffset),
                    onDecrease = { state.setHijriOffset(state.hijriOffset - 1) },
                    onIncrease = { state.setHijriOffset(state.hijriOffset + 1) }
                )
                Text(
                    text = "تقویم قمری به روش حسابی محاسبه می‌شود و ممکن است با رؤیت هلال " +
                        "یک یا دو روز اختلاف داشته باشد.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(10.dp))
            SectionTitle("نمایش")
            Spacer(Modifier.height(4.dp))
            GahCard {
                SettingsToggle(
                    title = "رقم‌های فارسی",
                    subtitle = "نمایش ۱۲۳ به‌جای 123",
                    checked = state.persianDigits,
                    onCheckedChange = { state.setPersianDigits(it) }
                )
                SettingsToggle(
                    title = "فقط مناسبت‌های مهم",
                    subtitle = "در تقویم ماهانه، مناسبت‌های آیینی برجسته‌تر شوند",
                    checked = state.eventsOnly,
                    onCheckedChange = { state.setEventsOnly(it) }
                )
            }

            Spacer(Modifier.height(10.dp))
            SectionTitle("یادآور")
            Spacer(Modifier.height(4.dp))
            GahCard {
                SettingsToggle(
                    title = "یادآور روزانهٔ مناسبت‌ها",
                    subtitle = "بدون اینترنت، هر روز در ساعت دلخواه",
                    checked = state.notificationsEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= 33) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        state.setNotifications(enabled)
                    }
                )
                if (state.notificationsEnabled) {
                    StepperRow(
                        title = "ساعت یادآور",
                        value = state.digits(state.notifyHour.toString().padStart(2, '0')) + ":" +
                            state.digits(state.notifyMinute.toString().padStart(2, '0')),
                        onDecrease = { state.setNotifications(true, (state.notifyHour + 23) % 24, state.notifyMinute) },
                        onIncrease = { state.setNotifications(true, (state.notifyHour + 1) % 24, state.notifyMinute) }
                    )
                    StepperRow(
                        title = "دقیقه",
                        value = state.digits(state.notifyMinute.toString().padStart(2, '0')),
                        onDecrease = { state.setNotifications(true, state.notifyHour, (state.notifyMinute + 45) % 60) },
                        onIncrease = { state.setNotifications(true, state.notifyHour, (state.notifyMinute + 15) % 60) }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            SectionTitle("ابزارها")
            Spacer(Modifier.height(4.dp))
            GahCard {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionChip(text = "تبدیل تاریخ", onClick = { state.push(Screen.Converter) })
                    ActionChip(text = "دربارهٔ برنامه", onClick = { state.push(Screen.About) })
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionChip(
                        text = "پاک‌کردن نشان‌ها (" + state.digits(state.bookmarks.size) + ")",
                        onClick = {
                            state.bookmarks.toList().forEach { state.toggleBookmark(it) }
                        }
                    )
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "این برنامه هیچ داده‌ای از دستگاه بیرون نمی‌فرستد و هیچ " +
                            "دسترسی اینترنتی ندارد. همهٔ محاسبات و نوشتارها در خود برنامه‌اند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "گاهشمار پارسی • نگارش " + BuildConfig.VERSION_NAME,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun ConverterScreen(state: AppState) {
    var year by remember { mutableStateOf(state.currentDate().year.toString()) }
    var month by remember { mutableStateOf(state.currentDate().month.toString()) }
    var day by remember { mutableStateOf(state.currentDate().day.toString()) }
    var result by remember { mutableStateOf<Long?>(state.todayEpochDay) }

    fun convert() {
        val y = year.toIntOrNull()
        val m = month.toIntOrNull()
        val d = day.toIntOrNull()
        result = if (y != null && m != null && d != null && m in 1..12) {
            val epoch = runCatching { PersianCalendar.toEpochDay(y, m, d) }.getOrNull()
            if (epoch != null && d in 1..PersianCalendar.monthLength(y, m)) epoch else null
        } else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader("تبدیل تاریخ", "میان خورشیدی، میلادی و قمری", onBack = { state.back() })
        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            GahCard {
                Text(text = "تاریخ خورشیدی", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it.filter { ch -> ch.isDigit() } },
                        label = { Text("سال") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = month,
                        onValueChange = { month = it.filter { ch -> ch.isDigit() } },
                        label = { Text("ماه") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = day,
                        onValueChange = { day = it.filter { ch -> ch.isDigit() } },
                        label = { Text("روز") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionChip(text = "تبدیل", filled = true, onClick = { convert() })
                    ActionChip(text = "امروز", onClick = {
                        val today = state.currentDate()
                        year = today.year.toString()
                        month = today.month.toString()
                        day = today.day.toString()
                        result = state.todayEpochDay
                    })
                }
            }

            Spacer(Modifier.height(10.dp))
            val epoch = result
            if (epoch == null) {
                GahCard {
                    Text(
                        text = "تاریخ واردشده معتبر نیست. سال، ماه و روز را بررسی کنید.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                val date = PersianCalendar.fromEpochDay(epoch)
                val gregorian = date.toGregorian()
                val hijri = HijriCalendar.fromEpochDay(epoch, state.hijriOffset)
                GahCard {
                    InfoRow("خورشیدی", PersianText.formatDateWithWeekday(date, state.persianDigits))
                    InfoRow("میلادی", PersianText.formatGregorian(gregorian, state.persianDigits))
                    InfoRow("هجری قمری", PersianText.formatHijri(hijri, state.persianDigits))
                    SoftDividerRow()
                    InfoRow("روز سال", state.digits(date.dayOfYear) + " از " + state.digits(PersianCalendar.yearLength(date.year)))
                    InfoRow("هفتهٔ سال", state.digits(PersianCalendar.weekOfYear(date)))
                    InfoRow("هفتهٔ ISO", state.digits(PersianCalendar.isoWeek(epoch)))
                    InfoRow("روز جولیَن", state.digits(epoch + CivilMath.JDN_1970_01_01))
                    InfoRow("روز زرتشتی", "روز " + ZoroastrianCalendar.dayName(date.day))
                    InfoRow("برج", PersianCalendar.ZODIAC_NAMES[(date.month - 1).coerceIn(0, 11)])
                    InfoRow("نماد چینی", GlobalSymbols.chineseCycle(gregorian.year))
                    InfoRow("کبیسه", if (PersianCalendar.isLeapYear(date.year)) "سال کبیسه است" else "سال کبیسه نیست")
                }
                Spacer(Modifier.height(10.dp))
                GahCard {
                    Text(
                        text = "تبدیل معکوس",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "می‌توانید با وارد کردن تاریخ در هریک از سه گاه‌شماری، " +
                            "معادل‌های دیگر را ببینید. مبنای این تبدیل، گاه‌شماری خورشیدی " +
                            "رسمی ایران است.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun AboutScreen(state: AppState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader("دربارهٔ گاهشمار پارسی", "تقویم، آیین و فرهنگ ایران", onBack = { state.back() })
        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(LocalGahPalette.current.headerTop)
                    .padding(18.dp)
            ) {
                Column {
                    StarMotif(modifier = Modifier.size(26.dp), color = LocalGahPalette.current.gold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "گاهشمار پارسی",
                        style = MaterialTheme.typography.headlineMedium,
                        color = LocalGahPalette.current.onHeader
                    )
                    Text(
                        text = "برنامه‌ای برای تقویم خورشیدی ایران، مناسبت‌های کهن، " +
                            "آیین‌های زرتشتی و فرهنگ ۲۵۰۰ سالهٔ ایران.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalGahPalette.current.onHeader.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            GahCard {
                Text(text = "ویژگی‌ها", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                listOf(
                    "تقویم خورشیدی با محاسبهٔ نجومی رسمی ایران",
                    "گاه‌شماری میلادی و هجری قمری و چینی در یک نما",
                    "بیش از پنجاه مناسبت ایرانی، زرتشتی و ادبی",
                    "نوشتارهای فرهنگی، آیینی، باستانی و دینی",
                    "خط زمان ایران باستان و پیوند به مقاله‌ها",
                    "تبدیل تاریخ، نشان‌کردن مناسبت‌ها و یادداشت روزانه",
                    "یادآور روزانه، بدون نیاز به اینترنت",
                    "کاملاً آفلاین و بی‌نیاز از مجوز اینترنت"
                ).forEach { line ->
                    Row(modifier = Modifier.padding(vertical = 3.dp)) {
                        StarMotif(modifier = Modifier.padding(top = 6.dp).size(6.dp), color = LocalGahPalette.current.gold)
                        Spacer(Modifier.width(8.dp))
                        Text(text = line, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            GahCard {
                Text(text = "روش محاسبه", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "آغاز سال خورشیدی، نزدیک‌ترین روز به لحظهٔ اعتدال بهاری است که " +
                        "نیمروزش به وقت تهران به آن لحظه نزدیک باشد. لحظهٔ اعتدال با " +
                        "الگوریتم نجومی مئوس و تصحیح ΔT محاسبه می‌شود. گاه‌شماری قمری " +
                        "به روش حسابی است و ممکن است یک روز با رؤیت هلال اختلاف داشته " +
                        "باشد.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(10.dp))
            GahCard {
                Text(text = "سپاس‌ها", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "قلم «وزیرمتن» ساختهٔ صابر راستی‌کردار و قلم «لاله‌زار» ساختهٔ " +
                        "Borna Izadpanah، هر دو با پروانهٔ OFL. داده‌های تاریخی و فرهنگی " +
                        "از منابع عمومی تاریخ ایران و اوستا گردآوری شده است.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(10.dp))
            GahCard(onClick = { state.push(Screen.Article("calendar-history")) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "تاریخ گاه‌شماری در ایران", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.width(6.dp))
                    Text(text = "←", style = MaterialTheme.typography.titleSmall)
                }
                val article = Articles.byId("calendar-history")
                if (article != null) {
                    Text(
                        text = article.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "نگارش " + BuildConfig.VERSION_NAME + " • ساخته‌شده برای دوستداران فرهنگ ایران",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )
        }
    }
}
