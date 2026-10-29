/* =========================================================================
   موتور گاه‌شماری — همین الگوریتم در نسخهٔ اندروید (Kotlin) پیاده شده است.
   قاعده: یکم فروردین = روزی که نیمروزش در تهران به لحظهٔ اعتدال بهاری نزدیک‌تر است.
   ========================================================================= */
(function (global) {
  'use strict';

  const JDN_1970 = 2440588;
  const TEHRAN_OFFSET_HOURS = 3.5;

  const MONTHS = ['فروردین', 'اردیبهشت', 'خرداد', 'تیر', 'مرداد', 'شهریور',
    'مهر', 'آبان', 'آذر', 'دی', 'بهمن', 'اسفند'];
  const MONTHS_AVESTAN = ['فروردین', 'اردیبهشت', 'خرداد', 'تیر', 'امرداد', 'شهریور',
    'مهر', 'آبان', 'آذر', 'دی', 'بهمن', 'سپندارمذ'];
  const WEEKDAYS = ['شنبه', 'یکشنبه', 'دوشنبه', 'سه‌شنبه', 'چهارشنبه', 'پنجشنبه', 'جمعه'];
  const WEEKDAYS_SHORT = ['ش', 'ی', 'د', 'س', 'چ', 'پ', 'ج'];
  const GREGORIAN_MONTHS = ['ژانویه', 'فوریه', 'مارس', 'آوریل', 'مه', 'ژوئن',
    'ژوئیه', 'اوت', 'سپتامبر', 'اکتبر', 'نوامبر', 'دسامبر'];
  const HIJRI_MONTHS = ['محرم', 'صفر', 'ربیع‌الاول', 'ربیع‌الثانی', 'جمادی‌الاول',
    'جمادی‌الثانی', 'رجب', 'شعبان', 'رمضان', 'شوال', 'ذی‌القعده', 'ذی‌الحجه'];
  const ZODIAC = ['حمل', 'ثور', 'جوزا', 'سرطان', 'اسد', 'سنبله',
    'میزان', 'عقرب', 'قوس', 'جدی', 'دلو', 'حوت'];
  const ZODIAC_SIGNS = ['بره', 'گاو', 'دوپیکر', 'خرچنگ', 'شیر', 'خوشه‌دانه',
    'ترازو', 'کژدم', 'کمان', 'بزغاله', 'دلو', 'دوماهی'];
  const CHINESE_ANIMALS = ['موش', 'گاو', 'ببر', 'گربهٔ وحشی', 'اژدها', 'مار',
    'اسب', 'بز', 'میمون', 'خروس', 'سگ', 'خوک'];
  const CHINESE_ELEMENTS = ['چوب', 'آتش', 'خاک', 'فلز', 'آب'];

  function floorDiv(a, b) { return Math.floor(a / b); }
  function floorMod(a, b) { return a - floorDiv(a, b) * b; }

  function gregorianToEpochDay(y, m, d) {
    const a = Math.floor((14 - m) / 12);
    const yy = y + 4800 - a;
    const mm = m + 12 * a - 3;
    const jdn = d + Math.floor((153 * mm + 2) / 5) + 365 * yy + Math.floor(yy / 4) -
      Math.floor(yy / 100) + Math.floor(yy / 400) - 32045;
    return jdn - JDN_1970;
  }

  function epochDayToGregorian(epochDay) {
    const jdn = epochDay + JDN_1970;
    const f = jdn + 1401 + Math.floor((Math.floor((4 * jdn + 274277) / 146097) * 3) / 4) - 38;
    const e = 4 * f + 3;
    const g = Math.floor(floorMod(e, 1461) / 4);
    const h = 5 * g + 2;
    const day = Math.floor(floorMod(h, 153) / 5) + 1;
    const month = floorMod(Math.floor(h / 153) + 2, 12) + 1;
    const year = Math.floor(e / 1461) - 4716 + Math.floor((12 + 2 - month) / 12);
    return { year: year, month: month, day: day };
  }

  // ─── اعتدال بهاری: روش مئوس (فصل ۲۷) ────────────────────────────────
  const TERMS = [
    [485.0, 324.96, 1934.136], [203.0, 337.23, 32964.467], [199.0, 342.08, 20.186],
    [182.0, 27.85, 445267.112], [156.0, 73.14, 45036.886], [136.0, 171.52, 22518.443],
    [77.0, 222.54, 65928.934], [74.0, 296.72, 3034.906], [70.0, 243.58, 9037.513],
    [58.0, 119.81, 33718.147], [52.0, 297.17, 150.678], [50.0, 21.02, 2281.226],
    [45.0, 247.54, 29929.562], [44.0, 325.15, 31555.956], [29.0, 60.93, 4443.417],
    [18.0, 155.12, 67555.328], [17.0, 288.79, 4562.452], [16.0, 198.04, 62894.029],
    [14.0, 199.76, 31436.921], [12.0, 95.39, 14577.848], [12.0, 287.11, 31931.756],
    [12.0, 320.81, 34777.259], [9.0, 227.73, 1222.114], [8.0, 15.45, 16859.074]
  ];

  function deltaTSeconds(year) {
    const y = year;
    if (y < 1900) return 0;
    if (y < 1920) {
      const t = y - 1900;
      return -2.79 + 1.494119 * t - 0.0598939 * t * t + 0.0061966 * t ** 3 - 0.000197 * t ** 4;
    }
    if (y < 1941) { const t = y - 1920; return 21.20 + 0.84493 * t - 0.0761 * t * t + 0.0020936 * t ** 3; }
    if (y < 1961) { const t = y - 1950; return 29.07 + 0.407 * t - t * t / 233 + t ** 3 / 2547; }
    if (y < 1986) { const t = y - 1975; return 45.45 + 1.067 * t - t * t / 260 - t ** 3 / 718; }
    if (y < 2005) {
      const t = y - 2000;
      return 63.86 + 0.3345 * t - 0.060374 * t * t + 0.0017275 * t ** 3 +
        0.000651814 * t ** 4 + 0.00002373599 * t ** 5;
    }
    if (y < 2050) { const t = y - 2000; return 62.92 + 0.32217 * t + 0.005589 * t * t; }
    const t = (y - 1820) / 100;
    return -20 + 32 * t * t - 0.5628 * (2150 - y);
  }

  function equinoxSolsticeJde(gregorianYear, quarter) {
    const y = (gregorianYear - 2000) / 1000;
    let jde0;
    switch (quarter) {
      case 0: jde0 = 2451623.80984 + 365242.37404 * y + 0.05169 * y ** 2 - 0.00411 * y ** 3 - 0.00057 * y ** 4; break;
      case 1: jde0 = 2451716.56767 + 365241.62603 * y + 0.00325 * y ** 2 + 0.00888 * y ** 3 - 0.00030 * y ** 4; break;
      case 2: jde0 = 2451810.21715 + 365242.01767 * y - 0.11575 * y ** 2 + 0.00337 * y ** 3 + 0.00078 * y ** 4; break;
      default: jde0 = 2451900.05952 + 365242.74049 * y - 0.06223 * y ** 2 - 0.00823 * y ** 3 + 0.00032 * y ** 4;
    }
    const t = (jde0 - 2451545.0) / 36525.0;
    const w = (35999.373 * t - 2.47) * Math.PI / 180;
    const dLambda = 1 + 0.0334 * Math.cos(w) + 0.0007 * Math.cos(2 * w);
    let s = 0;
    for (const term of TERMS) {
      s += term[0] * Math.cos((term[1] + term[2] * t) * Math.PI / 180);
    }
    return jde0 + (0.00001 * s) / dLambda;
  }

  function localDayAndFraction(gregorianYear, quarter) {
    const jde = equinoxSolsticeJde(gregorianYear, quarter);
    const jdUt = jde - deltaTSeconds(gregorianYear) / 86400;
    const jdLocal = jdUt + TEHRAN_OFFSET_HOURS / 24;
    const jdn = Math.floor(jdLocal + 0.5);
    return { jdn: jdn, frac: (jdLocal + 0.5) - jdn };
  }

  function momentUtcMillis(gregorianYear, quarter) {
    const jde = equinoxSolsticeJde(gregorianYear, quarter);
    const jdUt = jde - deltaTSeconds(gregorianYear) / 86400;
    return (jdUt - 2440587.5) * 86400000;
  }

  const nowruzCache = new Map();

  function nowruzEpochDay(persianYear) {
    if (nowruzCache.has(persianYear)) return nowruzCache.get(persianYear);
    const gYear = persianYear + 621;
    let value;
    if (gYear >= 1800 && gYear <= 2150) {
      const { jdn, frac } = localDayAndFraction(gYear, 0);
      value = (frac <= 0.5 ? jdn : jdn + 1) - JDN_1970;
    } else {
      value = arithmeticNowruz(persianYear);
    }
    nowruzCache.set(persianYear, value);
    return value;
  }

  function arithmeticNowruz(persianYear) {
    const epBase = persianYear >= 0 ? persianYear - 474 : persianYear - 473;
    const epYear = 474 + floorMod(epBase, 2820);
    const jdn = 1 + Math.floor((epYear * 682 - 110) / 2816) + (epYear - 1) * 365 +
      floorDiv(epBase, 2820) * 1029983 + (1948320 - 1);
    return jdn - JDN_1970;
  }

  function isLeapYear(y) { return nowruzEpochDay(y + 1) - nowruzEpochDay(y) === 366; }
  function yearLength(y) { return nowruzEpochDay(y + 1) - nowruzEpochDay(y); }
  function monthLength(y, m) {
    if (m >= 1 && m <= 6) return 31;
    if (m >= 7 && m <= 11) return 30;
    if (m === 12) return isLeapYear(y) ? 30 : 29;
    return 0;
  }
  function dayOfYear(y, m, d) { return m <= 7 ? (m - 1) * 31 + d : 186 + (m - 7) * 30 + d; }

  function toEpochDay(y, m, d) {
    const start = nowruzEpochDay(y);
    const before = m <= 7 ? (m - 1) * 31 : 186 + (m - 7) * 30;
    return start + before + d - 1;
  }

  function fromEpochDay(epochDay) {
    const g = epochDayToGregorian(epochDay);
    let year = g.year - 621;
    while (epochDay < nowruzEpochDay(year)) year--;
    while (epochDay >= nowruzEpochDay(year + 1)) year++;
    const doy = epochDay - nowruzEpochDay(year) + 1;
    let month, day;
    if (doy <= 186) {
      month = Math.floor((doy + 30) / 31);
      day = doy - (month - 1) * 31;
    } else {
      month = Math.floor((doy - 157) / 30) + 6;
      day = doy - (186 + (month - 7) * 30);
    }
    return { year: year, month: month, day: day };
  }

  function weekdayIndex(epochDay) { return floorMod(epochDay + 5, 7); }
  function weekdayName(epochDay) { return WEEKDAYS[weekdayIndex(epochDay)]; }
  function weekOfYear(y, m, d) {
    return Math.floor((dayOfYear(y, m, d) - 1 + weekdayIndex(nowruzEpochDay(y))) / 7) + 1;
  }
  function isoWeekdayIndex(epochDay) { return floorMod(epochDay + 3, 7); }
  function isoWeek(epochDay) {
    const thursday = epochDay - isoWeekdayIndex(epochDay) + 3;
    const g = epochDayToGregorian(thursday);
    const jan1 = gregorianToEpochDay(g.year, 1, 1);
    return Math.floor((thursday - jan1) / 7) + 1;
  }

  // ─── هجری قمری (روش حسابی) ───────────────────────────────────────────
  function hijriFromEpochDay(epochDay, offset) {
    const jdn = epochDay + JDN_1970 + (offset || 0);
    let l = jdn - 1948440 + 10632;
    const n = Math.floor((l - 1) / 10631);
    l = l - 10631 * n + 354;
    const j = Math.floor((10985 - l) / 5316) * Math.floor((50 * l) / 17719) +
      Math.floor(l / 5670) * Math.floor((43 * l) / 15238);
    l = l - Math.floor((30 - j) / 15) * Math.floor((17719 * j) / 50) -
      Math.floor(j / 16) * Math.floor((15238 * j) / 43) + 29;
    const m = Math.floor((24 * l) / 709);
    const d = l - Math.floor((709 * m) / 24);
    const y = 30 * n + j - 30;
    return { year: y, month: m, day: d };
  }

  function hijriToEpochDay(y, m, d) {
    const jdn = Math.floor((11 * y + 3) / 30) + 354 * y + 30 * m - Math.floor((m - 1) / 2) + d + 1948440 - 385;
    return jdn - JDN_1970;
  }

  function hijriMonthLength(y, m) {
    const start = hijriToEpochDay(y, m, 1);
    const next = m === 12 ? hijriToEpochDay(y + 1, 1, 1) : hijriToEpochDay(y, m + 1, 1);
    return next - start;
  }

  // ─── نمادها و چرخه‌ها ────────────────────────────────────────────────
  function chineseZodiac(gy) { return CHINESE_ANIMALS[floorMod(gy - 4, 12)]; }
  function chineseElement(gy) { return CHINESE_ELEMENTS[Math.floor(floorMod(gy - 4, 10) / 2)]; }
  function chineseCycle(gy) { return 'سال ' + chineseElement(gy) + ' ' + chineseZodiac(gy); }

  function moonPhase(millis) {
    const jd = millis / 86400000 + 2440587.5;
    const synodic = 29.530588853;
    const age = ((jd - 2451550.26) % synodic + synodic) % synodic;
    const illumination = Math.round((1 - Math.cos(2 * Math.PI * age / synodic)) / 2 * 100);
    let name = 'ماه نو';
    if (age >= 28.53 || age < 1) name = 'ماه نو';
    else if (age < 5.5) name = 'هلال افزاینده';
    else if (age < 9) name = 'ربع نخست';
    else if (age < 13.5) name = 'کوژِ افزاینده';
    else if (age < 16) name = 'ماه کامل';
    else if (age < 20.5) name = 'کوژِ کاهنده';
    else if (age < 24) name = 'ربع آخر';
    else name = 'هلال کاهنده';
    return { name: name, illumination: illumination, ageDays: age };
  }

  function seasonOf(month) {
    if (month <= 3) return 'بهار';
    if (month <= 6) return 'تابستان';
    if (month <= 9) return 'پاییز';
    return 'زمستان';
  }

  function nowruzMomentUtcMillis(persianYear) { return momentUtcMillis(persianYear + 621, 0); }

  function tomorrow(epochDay) { return epochDay + 1; }

  global.GahCalendar = {
    JDN_1970, MONTHS, MONTHS_AVESTAN, WEEKDAYS, WEEKDAYS_SHORT, GREGORIAN_MONTHS,
    HIJRI_MONTHS, ZODIAC, ZODIAC_SIGNS,
    gregorianToEpochDay, epochDayToGregorian, nowruzEpochDay, isLeapYear, yearLength,
    monthLength, dayOfYear, toEpochDay, fromEpochDay, weekdayIndex, weekdayName,
    weekOfYear, isoWeek, hijriFromEpochDay, hijriToEpochDay, hijriMonthLength,
    chineseCycle, moonPhase, seasonOf, nowruzMomentUtcMillis, momentUtcMillis,
    equinoxSolsticeJde, deltaTSeconds, tomorrow, floorMod
  };
})(typeof window !== 'undefined' ? window : globalThis);

if (typeof module !== 'undefined' && module.exports) {
  module.exports = (typeof window !== 'undefined' ? window : globalThis).GahCalendar;
}

/* =========================================================================
   گاه‌شماری ایرانی — لایهٔ بالای موتور نوروز
   دوازده ماه سی‌روزه، سی روزِ نام‌دار و پنجهٔ وهیک در پایان سال.
   ========================================================================= */
(function (global) {
  'use strict';
  const C = global.GahCalendar;
  if (!C) return;

  const MONTH_NAMES = ['فروردین', 'اردیبهشت', 'خرداد', 'تیر', 'امرداد', 'شهریور',
    'مهر', 'آبان', 'آذر', 'دی', 'بهمن', 'سپندارمذ'];
  const DAY_NAMES = ['اورمزد', 'بهمن', 'اردیبهشت', 'شهریور', 'سپندارمذ', 'خرداد', 'امرداد',
    'دی به آذر', 'آذر', 'آبان', 'خور', 'ماه', 'تیر', 'گوش', 'دی به مهر',
    'مهر', 'سروش', 'رشن', 'فروردین', 'بهرام', 'رام', 'باد', 'دی به دین',
    'دین', 'ارد', 'اشتاد', 'آسمان', 'زامیاد', 'مانتره سپند', 'انارام'];
  const DAY_MEANINGS = ['خداوند دانا', 'اندیشهٔ نیک', 'بهترین راستی', 'کشور و شهریاری', 'فروتنی و مهر',
    'کمال و رسایی', 'بی‌مرگی', 'آفریدگار، ماه آتش', 'آتش', 'آب‌ها', 'خورشید', 'ماه',
    'ستارهٔ باران', 'گوش‌دهندهٔ جهان', 'آفریدگار، ماه مهر', 'پیمان و مهر',
    'پیام‌آوری و شنوایی', 'دادگری', 'فروهرها', 'پیروزی', 'شادی و آرامش', 'باد',
    'آفریدگار، ماه دین', 'دین', 'خوشبختی', 'راستی', 'آسمان', 'زمین',
    'کلام مقدس', 'بی‌کران'];
  const GATHA_NAMES = ['اهونود', 'اشتود', 'سپنتمد', 'وهوخشتر', 'وهیشتواشت'];
  const LEAP_GATHA = 'وهیشتواشت دوم';

  const MONTH_DAYS = 30;
  const MONTHS = 12;
  const DAY_MS = 86400000;
  const TEHRAN_MS = 3.5 * 3600 * 1000;

  function yearStart(year) { return C.nowruzEpochDay(year); }
  function yearLength(year) { return C.yearLength(year); }
  function panjehLength(year) { return yearLength(year) - MONTHS * MONTH_DAYS; }
  function monthStart(year, month) { return yearStart(year) + (month - 1) * MONTH_DAYS; }
  function panjehStart(year) { return yearStart(year) + MONTHS * MONTH_DAYS; }
  function isLeap(year) { return yearLength(year) === 366; }

  function yearOf(epochDay) {
    let year = C.epochDayToGregorian(epochDay).year - 621;
    while (epochDay < yearStart(year)) year--;
    while (epochDay >= yearStart(year + 1)) year++;
    return year;
  }

  function fromEpochDay(epochDay) {
    const year = yearOf(epochDay);
    const index = epochDay - yearStart(year);
    if (index < MONTHS * MONTH_DAYS) {
      return { year: year, month: Math.floor(index / MONTH_DAYS) + 1, day: (index % MONTH_DAYS) + 1, panjehIndex: 0 };
    }
    return { year: year, month: 0, day: 0, panjehIndex: index - MONTHS * MONTH_DAYS + 1 };
  }

  function toEpochDay(year, month, day, panjehIndex) {
    if (panjehIndex) return panjehStart(year) + panjehIndex - 1;
    return yearStart(year) + (month - 1) * MONTH_DAYS + day - 1;
  }

  function monthName(month) { return MONTH_NAMES[Math.min(Math.max(month - 1, 0), 11)]; }
  function dayName(day) { return DAY_NAMES[Math.min(Math.max(day - 1, 0), 29)]; }
  function dayMeaning(day) { return DAY_MEANINGS[Math.min(Math.max(day - 1, 0), 29)]; }
  function gathaName(index) { return index >= 1 && index <= 5 ? GATHA_NAMES[index - 1] : LEAP_GATHA; }

  function nameOf(date) {
    return date.panjehIndex ? gathaName(date.panjehIndex) : dayName(date.day);
  }
  function meaningOf(date) {
    return date.panjehIndex ? 'روز گاتها' : dayMeaning(date.day);
  }
  function ritualLabel(date) {
    return date.panjehIndex
      ? 'روز ' + nameOf(date) + ' از پنجهٔ وهیک'
      : 'روز ' + nameOf(date) + ' از ' + monthName(date.month) + ' ماه';
  }

  function gahOfDay(hour) {
    if (hour <= 5 || hour >= 20 && hour <= 23) return 'اشاهین‌گاه';
    if (hour <= 11) return 'هاون‌گاه';
    if (hour <= 15) return 'رپیتون‌گاه';
    if (hour <= 19) return 'ازیرین‌گاه';
    return 'ایوی‌سرترون‌گاه';
  }

  function lastWednesdayBeforeNowruz(year) {
    let day = yearStart(year) - 1;
    while (C.weekdayIndex(day) !== 3) day--;
    return day;
  }

  function winterSolsticeDay(year) {
    const start = yearStart(year);
    const end = yearStart(year + 1);
    for (const gYear of [year + 621, year + 622]) {
      const millis = C.momentUtcMillis(gYear, 3);
      const day = Math.floor((millis + TEHRAN_MS) / DAY_MS);
      if (day >= start && day < end) return day;
    }
    return toEpochDay(year, 10, 10);
  }

  function dateInYear(festival, year) {
    const rule = festival.rule;
    switch (rule.type) {
      case 'monthDay': return toEpochDay(year, rule.month, rule.day);
      case 'panjeh': return panjehStart(year);
      case 'lastWednesday': return lastWednesdayBeforeNowruz(year);
      case 'winterSolstice': return winterSolsticeDay(year);
      default: return null;
    }
  }

  function festivalsOfYear(festivals, year) {
    return festivals
      .map(f => ({ festival: f, epochDay: dateInYear(f, year) }))
      .filter(item => item.epochDay !== null && item.epochDay !== undefined)
      .sort((a, b) => a.epochDay - b.epochDay);
  }

  function festivalsOn(festivals, epochDay) {
    const date = fromEpochDay(epochDay);
    const year = date.year;
    const out = [];
    festivals.forEach(f => {
      const rule = f.rule;
      if (rule.type === 'monthDay') {
        if (!date.panjehIndex && date.month === rule.month && date.day === rule.day) out.push(f);
      } else if (rule.type === 'panjeh') {
        if (date.panjehIndex === 1) out.push(f);
      } else if (rule.type === 'lastWednesday') {
        if (epochDay === lastWednesdayBeforeNowruz(year)) out.push(f);
      } else if (rule.type === 'winterSolstice') {
        if (epochDay === winterSolsticeDay(year)) out.push(f);
      }
    });
    return out;
  }

  global.Iranian = {
    MONTH_NAMES, DAY_NAMES, DAY_MEANINGS, GATHA_NAMES, MONTH_DAYS, MONTHS,
    yearStart, yearLength, panjehLength, monthStart, panjehStart, isLeap, yearOf,
    fromEpochDay, toEpochDay, monthName, dayName, dayMeaning, gathaName,
    nameOf, meaningOf, ritualLabel, gahOfDay, lastWednesdayBeforeNowruz,
    winterSolsticeDay, dateInYear, festivalsOfYear, festivalsOn
  };
})(typeof window !== 'undefined' ? window : globalThis);
