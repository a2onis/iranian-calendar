/* ==========================================================================
   گاهشمار پارسی — پیش‌نمایش وب
   همان داده‌ها و همان منطق نسخهٔ اندروید، در مرورگر.
   ========================================================================== */
(function () {
  'use strict';

  const C = window.GahCalendar;
  const D = window.GAH_DATA;

  const CATS = {
    NOWRUZ: { label: 'نوروز', emoji: '🌱' },
    FESTIVAL: { label: 'جشن و آیین', emoji: '🎊' },
    ZOROASTRIAN: { label: 'زرتشتی', emoji: '🔥' },
    ANCIENT: { label: 'ایران باستان', emoji: '🏛' },
    LITERATURE: { label: 'ادب و اندیشه', emoji: '📜' },
    CULTURE: { label: 'فرهنگ و هنر', emoji: '🎨' },
    NATURE: { label: 'طبیعت و کیهان', emoji: '☀' },
    MEMORIAL: { label: 'بزرگداشت', emoji: '🕯' }
  };

  const ARTICLE_CATS = [
    { id: 'NOWRUZ', label: 'نوروز و آیین‌ها', emoji: '🌱' },
    { id: 'ZOROASTRIAN', label: 'دین زرتشتی', emoji: '🔥' },
    { id: 'ANCIENT', label: 'ایران باستان', emoji: '🏛' },
    { id: 'CULTURE', label: 'فرهنگ و هنر', emoji: '🎨' },
    { id: 'CALENDAR', label: 'گاه‌شماری', emoji: '☀' }
  ];

  const ERAS = [
    ['MYTHIC', 'دوران اساطیری'],
    ['PREHISTORY', 'پیش از تاریخ'],
    ['ELAM', 'ایلام'],
    ['MEDIAN', 'ماد'],
    ['ACHAEMENID', 'هخامنشی'],
    ['PARTHIAN', 'اشکانی'],
    ['SASSANID', 'ساسانی'],
    ['POST', 'پس از اسلام']
  ];

  const DAY_MS = 86400000;
  const TEHRAN_MS = 3.5 * 3600 * 1000;

  const store = {
    get(key, fallback) {
      try {
        const raw = localStorage.getItem('gahshomar:' + key);
        return raw === null ? fallback : JSON.parse(raw);
      } catch (e) { return fallback; }
    },
    set(key, value) {
      try { localStorage.setItem('gahshomar:' + key, JSON.stringify(value)); } catch (e) { }
    }
  };

  const today = Math.floor((Date.now() + TEHRAN_MS) / DAY_MS);

  const state = {
    tab: 'calendar',
    mode: 'solar',
    yearView: false,
    selected: today,
    solar: null,
    greg: null,
    hijri: null,
    article: null,
    query: '',
    category: null,
    articleCat: 'NOWRUZ',
    bookmarksOnly: false,
    theme: store.get('theme', 'light'),
    digits: store.get('digits', true),
    weekStart: store.get('weekStart', 'saturday'),
    hijriOffset: store.get('hijriOffset', 0),
    showAstronomy: store.get('showAstronomy', true),
    showGregorianNumbers: store.get('showGregorianNumbers', true),
    bookmarks: new Set(store.get('bookmarks', [])),
    notes: store.get('notes', {})
  };

  (function initViews() {
    const p = C.fromEpochDay(state.selected);
    const g = C.epochDayToGregorian(state.selected);
    const h = C.hijriFromEpochDay(state.selected, state.hijriOffset);
    state.solar = { y: p.year, m: p.month };
    state.greg = { y: g.year, m: g.month };
    state.hijri = { y: h.year, m: h.month };
  })();

  /* ─── ابزارها ─────────────────────────────────────────────────────── */
  const digitsMap = ['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];
  function toFa(str) {
    return String(str).replace(/\d/g, d => digitsMap[+d]);
  }
  function fa(n) { return state.digits ? toFa(n) : String(n); }
  function monthName(m) { return C.MONTHS[m - 1]; }
  function gMonthName(m) { return C.GREGORIAN_MONTHS[m - 1]; }
  function hMonthName(m) { return C.HIJRI_MONTHS[m - 1]; }
  function weekday(epoch) { return C.WEEKDAYS[C.weekdayIndex(epoch)]; }
  function formatDate(p) { return `${fa(p.day)} ${monthName(p.month)} ${fa(p.year)}`; }
  function formatGreg(g) { return `${fa(g.day)} ${gMonthName(g.month)} ${fa(g.year)}`; }
  function formatHijri(h) { return `${fa(h.day)} ${hMonthName(h.month)} ${fa(h.year)}`; }
  function pad2(n) { return String(n).padStart(2, '0'); }
  function timeTehran(utcMillis) {
    const d = new Date(utcMillis + TEHRAN_MS);
    return `${fa(pad2(d.getUTCHours()))}:${fa(pad2(d.getUTCMinutes()))}`;
  }
  function relative(epoch) {
    const diff = epoch - today;
    if (diff === 0) return 'امروز';
    if (diff === 1) return 'فردا';
    if (diff === -1) return 'دیروز';
    if (diff === 2) return 'پس‌فردا';
    if (diff === -2) return 'پریروز';
    return '';
  }
  function daysUntilLabel(days) {
    if (days === 0) return 'امروز';
    if (days === 1) return '۱ روز دیگر';
    if (days < 0) return `${fa(Math.abs(days))} روز پیش`;
    if (days < 31) return `${fa(days)} روز دیگر`;
    if (days < 365) return `${fa(Math.floor(days / 30))} ماه و ${fa(days % 30)} روز دیگر`;
    return `${fa(Math.floor(days / 365))} سال و ${fa(Math.floor((days % 365) / 30))} ماه دیگر`;
  }
  function star(size, color) {
    const s = size || 14, col = color || 'currentColor';
    let pts = [];
    for (let i = 0; i < 16; i++) {
      const r = i % 2 === 0 ? 10 : 4.2;
      const a = -Math.PI / 2 + i * Math.PI / 8;
      pts.push((11 + r * Math.cos(a)).toFixed(2) + ',' + (11 + r * Math.sin(a)).toFixed(2));
    }
    return `<svg width="${s}" height="${s}" viewBox="0 0 22 22" fill="${col}" aria-hidden="true"><polygon points="${pts.join(' ')}"/></svg>`;
  }
  function icon(name, size) {
    const s = size || 20;
    const paths = {
      calendar: '<rect x="3" y="4.5" width="18" height="16" rx="3"/><path d="M3 9.5h18M8 2.5v4M16 2.5v4"/>',
      star: '<path d="M12 3.2l2.5 5.2 5.7.8-4.1 4 1 5.7L12 16.2l-5.1 2.7 1-5.7-4.1-4 5.7-.8z"/>',
      heart: '<path d="M12 20s-7.5-4.6-9.3-9A5 5 0 0 1 12 6.4 5 5 0 0 1 21.3 11c-1.8 4.4-9.3 9-9.3 9z"/>',
      column: '<path d="M5 21V8.5L12 3l7 5.5V21"/><path d="M9 21v-6h6v6"/>',
      flame: '<path d="M12 2c1.6 3 4.5 4.8 4.5 8.3A4.5 4.5 0 0 1 12 15a4.5 4.5 0 0 1-4.5-4.7C7.5 6.8 10.4 5 12 2z"/><path d="M12 22c3.9 0 6.5-2.4 6.5-5.7 0-1.2-.3-2.3-.9-3.3"/>',
      gear: '<circle cx="12" cy="12" r="3.2"/><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.9l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.9-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.9.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.9 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1A1.7 1.7 0 0 0 4.6 9a1.7 1.7 0 0 0-.3-1.9l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.9.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.9-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.9V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z"/>',
      swap: '<path d="M4 8h13l-3.5-3.5M20 16H7l3.5 3.5"/>',
      search: '<circle cx="11" cy="11" r="6.5"/><path d="M16 16l4.5 4.5"/>',
      close: '<path d="M6 6l12 12M18 6L6 18"/>',
      right: '<path d="M9 5l7 7-7 7"/>',
      left: '<path d="M15 5l-7 7 7 7"/>',
      share: '<path d="M4 12v7a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-7"/><path d="M12 16V3M8 7l4-4 4 4"/>',
      book: '<path d="M4 5.5A2.5 2.5 0 0 1 6.5 3H20v15H6.5A2.5 2.5 0 0 0 4 20.5z"/><path d="M8 7h8M8 11h6"/>'
    };
    return `<svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${paths[name] || ''}</svg>`;
  }

  /* ─── داده: مناسبت‌ها ────────────────────────────────────────────── */
  function momentsOf(gregYear) {
    return [0, 1, 2, 3].map(q => Math.floor((C.momentUtcMillis(gregYear, q) + TEHRAN_MS) / DAY_MS));
  }

  function chaharshanbeSuriOf(persianYear) {
    let day = C.nowruzEpochDay(persianYear) - 1;
    while (C.weekdayIndex(day) !== 3) day--;
    return day;
  }

  function eventsForDay(epoch) {
    const p = C.fromEpochDay(epoch);
    const g = C.epochDayToGregorian(epoch);
    const moments = momentsOf(g.year);
    const suri = chaharshanbeSuriOf(p.year + 1);
    return D.events.filter(ev => {
      const r = ev.rule;
      if (r.type === 'solar') return p.month === r.month && p.day === r.day;
      if (r.type === 'western') return g.month === r.month && g.day === r.day;
      if (r.type === 'chaharshanbe') return epoch === suri;
      if (r.type === 'moment') return state.showAstronomy && moments[r.quarter] === epoch;
      return false;
    });
  }

  function occurrenceInYear(ev, persianYear) {
    const yearStart = C.nowruzEpochDay(persianYear);
    const yearEnd = C.nowruzEpochDay(persianYear + 1);
    const r = ev.rule;
    if (r.type === 'solar') {
      if (r.day > C.monthLength(persianYear, r.month)) return null;
      return C.toEpochDay(persianYear, r.month, r.day);
    }
    if (r.type === 'western') {
      const g1 = C.epochDayToGregorian(yearStart).year;
      const g2 = C.epochDayToGregorian(yearEnd - 1).year;
      for (let gy = g1; gy <= g2; gy++) {
        const e = C.gregorianToEpochDay(gy, r.month, r.day);
        if (e >= yearStart && e < yearEnd) return e;
      }
      return null;
    }
    if (r.type === 'chaharshanbe') return chaharshanbeSuriOf(persianYear + 1);
    if (r.type === 'moment') {
      const g1 = C.epochDayToGregorian(yearStart).year;
      const g2 = C.epochDayToGregorian(yearEnd - 1).year;
      for (let gy = g1; gy <= g2; gy++) {
        const e = momentsOf(gy)[r.quarter];
        if (e >= yearStart && e < yearEnd) return e;
      }
      return null;
    }
    return null;
  }

  function yearOccurrences(persianYear) {
    return D.events
      .map(ev => ({ event: ev, epoch: occurrenceInYear(ev, persianYear) }))
      .filter(item => item.epoch !== null)
      .sort((a, b) => a.epoch - b.epoch);
  }

  function upcoming(epoch, count) {
    const list = [];
    for (let d = epoch; d < epoch + 400 && list.length < count; d++) {
      for (const ev of eventsForDay(d)) {
        list.push({ event: ev, epoch: d });
        if (list.length >= count) break;
      }
    }
    return list;
  }

  /* ─── رندر: سربرگ ────────────────────────────────────────────────── */
  function heroHtml() {
    const epoch = state.selected;
    const p = C.fromEpochDay(epoch);
    const g = C.epochDayToGregorian(epoch);
    const h = C.hijriFromEpochDay(epoch, state.hijriOffset);
    const rel = relative(epoch);
    const nowruzDays = C.nowruzEpochDay(p.year + 1) - today;
    return `
      <header class="hero">
        <div class="hero__arch"></div>
        <div class="hero__arch hero__arch--2"></div>
        <div class="hero__top">
          <div class="hero__brand">
            <h1 class="hero__title">گاهشمار پارسی</h1>
            <p class="hero__sub">تقویم ایران، آیین‌ها و فرهنگ کهن</p>
          </div>
          <div class="hero__actions">
            <button class="iconbtn" data-action="converter" aria-label="تبدیل تاریخ">${icon('swap', 19)}</button>
            <button class="iconbtn" data-action="book" aria-label="دربارهٔ برنامه">${icon('book', 19)}</button>
            <button class="iconbtn" data-action="settings" aria-label="تنظیمات">${icon('gear', 19)}</button>
          </div>
        </div>
        <div class="hero__date">
          <div>
            <div class="hero__weekday">${weekday(epoch)} ${fa(p.day)}</div>
            <div class="hero__month">${monthName(p.month)} ${fa(p.year)}</div>
          </div>
          <div class="hero__badge">
            ${star(24, '#E7BE6A')}
            <div>${C.ZODIAC[p.month - 1]} • ${C.seasonOf(p.month)}</div>
          </div>
        </div>
        <div class="hero__pills">
          <span class="pill-outline">${formatGreg(g)}</span>
          <span class="pill-outline pill-outline--teal">${formatHijri(h)}</span>
          ${rel ? `<span class="pill-outline">${rel}</span>` : ''}
          ${nowruzDays > 0 && nowruzDays < 60 ? `<span class="pill-outline">${daysUntilLabel(nowruzDays)} به نوروز</span>` : ''}
        </div>
      </header>`;
  }

  /* ─── رندر: تقویم ────────────────────────────────────────────────── */
  function gridData() {
    let start, length, title, sub;
    if (state.mode === 'solar') {
      const { y, m } = state.solar;
      start = C.toEpochDay(y, m, 1);
      length = C.monthLength(y, m);
      title = `${monthName(m)} ${fa(y)}`;
      sub = C.isLeapYear(y) ? 'سال کبیسه' : 'سال عادی';
    } else if (state.mode === 'gregorian') {
      const { y, m } = state.greg;
      start = C.gregorianToEpochDay(y, m, 1);
      const isLeap = (y % 4 === 0 && y % 100 !== 0) || y % 400 === 0;
      length = m === 2 ? (isLeap ? 29 : 28) : [4, 6, 9, 11].includes(m) ? 30 : 31;
      title = `${gMonthName(m)} ${fa(y)}`;
      sub = 'گاه‌شماری میلادی';
    } else {
      const { y, m } = state.hijri;
      start = C.hijriToEpochDay(y, m, 1);
      length = C.hijriMonthLength(y, m);
      title = `${hMonthName(m)} ${fa(y)}`;
      sub = 'گاه‌شماری هجری قمری';
    }
    const order = state.weekStart === 'saturday' ? [0, 1, 2, 3, 4, 5, 6] : [6, 0, 1, 2, 3, 4, 5];
    const leading = ((C.weekdayIndex(start) - order[0]) + 7) % 7;
    const gridStart = start - leading;
    return { start, length, title, sub, order, gridStart };
  }

  function calendarCellHtml(epoch, inMonth) {
    const p = C.fromEpochDay(epoch);
    const g = C.epochDayToGregorian(epoch);
    const h = C.hijriFromEpochDay(epoch, state.hijriOffset);
    const events = eventsForDay(epoch);
    const isHoliday = events.some(e => e.observance !== 'NONE');
    const friday = C.weekdayIndex(epoch) === 6;
    let primary, secondary;
    if (state.mode === 'solar') {
      primary = p.day;
      secondary = state.showGregorianNumbers ? g.day : null;
    } else if (state.mode === 'gregorian') {
      primary = g.day;
      secondary = p.day;
    } else {
      primary = h.day;
      secondary = p.day;
    }
    const cls = ['cell'];
    if (!inMonth) cls.push('cell--muted');
    if (epoch === state.selected) cls.push('cell--selected');
    if (epoch === today) cls.push('cell--today');
    if (friday) cls.push('cell--friday');
    if (isHoliday) cls.push('cell--holiday');
    const dots = events.slice(0, 3).map(e =>
      `<span class="cell__dot ${e.observance !== 'NONE' ? 'cell__dot--red' : ''}"></span>`).join('');
    return `<button class="${cls.join(' ')}" data-day="${epoch}" aria-label="${formatDate(p)}">
      <span class="cell__num">${fa(primary)}</span>
      <span class="cell__sub">${secondary === null ? '' : fa(secondary)}</span>
      <span class="cell__dots">${dots}</span>
    </button>`;
  }

  function calendarScreen() {
    const grid = gridData();
    const cells = [];
    for (let i = 0; i < 42; i++) {
      const epoch = grid.gridStart + i;
      cells.push(calendarCellHtml(epoch, epoch >= grid.start && epoch < grid.start + grid.length));
    }
    const monthChips = C.MONTHS.map((name, idx) =>
      `<button class="chip ${state.mode === 'solar' && state.solar.m === idx + 1 && !state.yearView ? 'chip--active' : ''}"
        data-action="set-month" data-month="${idx + 1}">${name}</button>`).join('');

    const modeLabels = [['solar', 'خورشیدی'], ['gregorian', 'میلادی'], ['hijri', 'قمری']];
    const segmented = modeLabels.map(([id, label]) =>
      `<button data-action="set-mode" data-mode="${id}" aria-selected="${state.mode === id}">${label}</button>`).join('');

    const viewToggle = `<div class="viewtoggle">
        <button data-action="view-month" class="${!state.yearView ? 'on' : ''}">ماه</button>
        <button data-action="view-year" class="${state.yearView ? 'on' : ''}">سال</button>
      </div>`;

    const headTitle = state.yearView ? (state.mode === 'solar' ? `سال ${fa(state.solar.y)}` :
      state.mode === 'gregorian' ? `سال ${fa(state.greg.y)}` : `سال ${fa(state.hijri.y)}`) : grid.title;
    const sub = state.yearView ? 'نمای سالانه — برای دیدن روزنگار روی روز بزنید' : grid.sub;

    return `
      ${heroHtml()}
      <div class="monthbar">
        <button class="iconbtn" data-action="prev-month" aria-label="ماه بعد">${icon('right', 20)}</button>
        <div class="monthbar__title">${headTitle}
          <div class="muted" style="font-size:10.5px;text-align:center">${sub}</div>
        </div>
        <button class="iconbtn" data-action="next-month" aria-label="ماه پیش">${icon('left', 20)}</button>
        <button class="chip" data-action="today">امروز</button>
        ${viewToggle}
      </div>
      <div class="segmented" role="tablist">${segmented}</div>
      ${state.mode === 'solar' && !state.yearView ? `<div class="monthstrip">${monthChips}</div>` : ''}
      ${state.yearView ? yearGridHtml() : `
        <div class="weekhead">${grid.order.map(i => `<span>${i === 6 ? 'آدینه' : C.WEEKDAYS_SHORT[i]}</span>`).join('')}</div>
        <div class="grid">${cells.join('')}</div>
        ${dayCardHtml(state.selected)}
        ${weekStripHtml(state.selected)}
        ${upcomingHtml()}
        ${quoteHtml()}
        <p class="muted center" style="padding:14px 30px 24px">
          مبنای محاسبه: قاعدهٔ نجومی مرکز تقویم ایران — اعتدال بهاری به وقت تهران.<br>
          همهٔ داده‌ها در خود برنامه و بدون اینترنت است.
        </p>`}`;
  }

  /* نمای سالانه: دوازده ماه کوچک در یک نگاه */
  function yearGridHtml() {
    const order = state.weekStart === 'saturday' ? [0, 1, 2, 3, 4, 5, 6] : [6, 0, 1, 2, 3, 4, 5];
    const blocks = [];
    for (let m = 1; m <= 12; m++) {
      let start, length, title;
      if (state.mode === 'solar') {
        const y = state.solar.y;
        start = C.toEpochDay(y, m, 1);
        length = C.monthLength(y, m);
        title = monthName(m);
      } else if (state.mode === 'gregorian') {
        const y = state.greg.y;
        start = C.gregorianToEpochDay(y, m, 1);
        const isLeap = (y % 4 === 0 && y % 100 !== 0) || y % 400 === 0;
        length = m === 2 ? (isLeap ? 29 : 28) : [4, 6, 9, 11].includes(m) ? 30 : 31;
        title = gMonthName(m);
      } else {
        const y = state.hijri.y;
        start = C.hijriToEpochDay(y, m, 1);
        length = C.hijriMonthLength(y, m);
        title = hMonthName(m);
      }
      const lead = ((C.weekdayIndex(start) - order[0]) + 7) % 7;
      const cells = [];
      for (let pad = 0; pad < lead; pad++) cells.push('<span class="mini__cell mini__cell--blank"></span>');
      for (let d = 0; d < length; d++) {
        const epoch = start + d;
        const p = C.fromEpochDay(epoch);
        const evs = eventsForDay(epoch);
        const holiday = evs.some(e => e.observance === 'PUBLIC');
        const cls = ['mini__cell'];
        if (holiday) cls.push('mini__cell--holiday');
        if (epoch === today) cls.push('mini__cell--today');
        if (epoch === state.selected) cls.push('mini__cell--sel');
        cells.push(`<span class="${cls.join(' ')}" data-action="open-day" data-day="${epoch}" title="${escapeAttr(formatDate(p))}">${fa(d + 1)}</span>`);
      }
      blocks.push(`<div class="mini">
        <div class="mini__title">${title}</div>
        <div class="mini__wd">${order.map(i => `<span>${i === 6 ? 'آ' : C.WEEKDAYS_SHORT[i]}</span>`).join('')}</div>
        <div class="mini__grid">${cells.join('')}</div>
      </div>`);
    }
    return `<div class="yeargrid">${blocks.join('')}</div>
      <p class="muted center" style="padding:6px 30px 20px">
        نقطه‌های سرخ، روزهای تعطیل رسمی و آیین‌های کهن‌اند. نمای سالانه برای یک نگاه کلی به سال است.
      </p>`;
  }

  /* نوار هفته: هفت روزِ هفتهٔ جاری با مناسبت‌ها */
  function weekStripHtml(epoch) {
    const firstIdx = state.weekStart === 'saturday' ? 0 : 6;
    const weekStart = epoch - ((C.weekdayIndex(epoch) - firstIdx + 7) % 7);
    const days = [];
    for (let i = 0; i < 7; i++) {
      const e = weekStart + i;
      const p = C.fromEpochDay(e);
      const evs = eventsForDay(e);
      const cls = ['wk__day'];
      if (e === today) cls.push('wk__day--today');
      if (e === state.selected) cls.push('wk__day--sel');
      if (evs.some(x => x.observance === 'PUBLIC')) cls.push('wk__day--holiday');
      days.push(`<button class="${cls.join(' ')}" data-action="pick-day" data-day="${e}">
        <span class="wk__wd">${C.WEEKDAYS_SHORT[C.weekdayIndex(e)]}</span>
        <span class="wk__num">${fa(p.day)}</span>
        <span class="wk__dot ${evs.length ? '' : 'wk__dot--none'}"></span>
      </button>`);
    }
    const evs = eventsForDay(epoch);
    return `
      <div class="section-title">${star(12)} <span>هفتهٔ جاری</span></div>
      <div class="card weekstrip">${days.join('')}</div>
      ${evs.length ? '' : `<p class="muted" style="margin:-4px 18px 0">این هفته مناسبت ویژه‌ای ندارد؛ اما هر روزش یک برگ از گاهشمار ایران است.</p>`}`;
  }

  function dayCardHtml(epoch) {
    const p = C.fromEpochDay(epoch);
    const g = C.epochDayToGregorian(epoch);
    const h = C.hijriFromEpochDay(epoch, state.hijriOffset);
    const events = eventsForDay(epoch);
    const rel = relative(epoch);
    const zDay = D.dayNames[p.day - 1];
    const zMean = D.dayMeanings[p.day - 1];
    const eventsHtml = events.length
      ? events.map(ev => eventRowHtml(ev, epoch)).join('')
      : '<p class="muted mt6">مناسبت ویژه‌ای برای این روز ثبت نشده است.</p>';
    const note = state.notes[epoch] || '';
    return `
      <div class="card">
        <div class="dayline">
          <div class="seal"><span class="seal__day">${fa(p.day)}</span><span class="seal__month">${monthName(p.month)}</span></div>
          <div class="daymeta">
            <div class="daymeta__title">${weekday(epoch)} ${rel ? `<span class="badge">${rel}</span>` : ''}</div>
            <div class="daymeta__sub">
              ${formatDate(p)}<br>
              ${formatGreg(g)} • ${formatHijri(h)}
            </div>
          </div>
        </div>
        <div class="rows">
          <div class="row"><span class="row__label">روز زرتشتی</span><span class="row__value">روز ${zDay} از ماه ${C.MONTHS_AVESTAN[p.month - 1]} — ${zMean}</span></div>
          <div class="row"><span class="row__label">برج و فصل</span><span class="row__value">${C.ZODIAC[p.month - 1]} • ${C.seasonOf(p.month)}</span></div>
          <div class="row"><span class="row__label">روز و هفته</span><span class="row__value">روز ${fa(C.dayOfYear(p.year, p.month, p.day))} از سال • هفتهٔ ${fa(C.weekOfYear(p.year, p.month, p.day))} • هفتهٔ ISO ${fa(C.isoWeek(epoch))}</span></div>
          <div class="row"><span class="row__label">ماه قمری</span><span class="row__value">${formatHijri(h)} • ${fa(C.hijriMonthLength(h.year, h.month))} روزه</span></div>
          <div class="row"><span class="row__label">نماد چینی</span><span class="row__value">${C.chineseCycle(g.year)}</span></div>
          ${(p.month === 1 && p.day === 1) ? `<div class="row"><span class="row__label">تحویل سال</span><span class="row__value" style="color:var(--pomegranate)">${timeTehran(C.nowruzMomentUtcMillis(p.year))} به وقت تهران</span></div>` : ''}
        </div>
        <div class="hr"></div>
        ${eventsHtml}
        ${note ? `<div class="mt6 muted">📝 یادداشت من: ${escapeHtml(note)}</div>` : ''}
        <div class="actions">
          <button class="btn btn--primary" data-action="open-day" data-day="${epoch}">روزنگار کامل</button>
          <button class="btn" data-action="share-day" data-day="${epoch}">${icon('share', 15)} اشتراک‌گذاری</button>
        </div>
      </div>`;
  }

  function eventRowHtml(ev, epoch, withDate) {
    const cat = CATS[ev.category] || { label: ev.category, emoji: '•' };
    const holiday = ev.observance !== 'NONE';
    const tags = [`<span class="badge badge--gold">${cat.label}</span>`];
    if (holiday) tags.push(`<span class="badge badge--red">${ev.observance === 'PUBLIC' ? 'تعطیل رسمی' : 'آیین کهن'}</span>`);
    if (withDate && epoch) {
      const p = C.fromEpochDay(epoch);
      tags.unshift(`<span class="badge">${fa(p.day)} ${monthName(p.month)}</span>`);
    }
    return `<div class="eventrow" data-action="open-day" data-day="${epoch}">
      <span class="eventrow__bullet ${holiday ? 'eventrow__bullet--red' : ''}"></span>
      <div class="eventrow__body">
        <div class="eventrow__title ${holiday ? 'eventrow__title--holiday' : ''}">${cat.emoji} ${escapeHtml(ev.title)}</div>
        <div class="eventrow__sum">${escapeHtml(ev.summary)}</div>
        <div class="eventrow__tags">${tags.join('')}</div>
      </div>
    </div>`;
  }

  function upcomingHtml() {
    const list = upcoming(today + 1, 5);
    return `
      <div class="section-title">${star(12)} <span>مناسبت‌های پیشِ رو</span></div>
      <div class="card">
        ${list.map(item => {
      const p = C.fromEpochDay(item.epoch);
      return `<div class="upcoming" data-action="open-day" data-day="${item.epoch}">
            <div class="upcoming__date">
              <div class="upcoming__day">${fa(p.day)}</div>
              <div class="upcoming__month">${monthName(p.month)}</div>
            </div>
            <div style="flex:1 1 auto;min-width:0">
              <div class="upcoming__title">${escapeHtml(item.event.title)}</div>
              <div class="upcoming__left">${daysUntilLabel(item.epoch - today)}</div>
            </div>
          </div>`;
    }).join('')}
      </div>`;
  }

  function quoteHtml() {
    const q = D.quotes[C.floorMod(today, D.quotes.length)];
    return `
      <div class="section-title">${star(12)} <span>سخن روز</span></div>
      <div class="card card--dark">
        ${star(18, '#E7BE6A')}
        <div class="quote">${escapeHtml(q.text)}</div>
        <div class="quote__source">— ${escapeHtml(q.source)}</div>
      </div>`;
  }

  /* ─── رندر: مناسبت‌ها ────────────────────────────────────────────── */
  function eventsScreen() {
    const year = C.fromEpochDay(state.selected).year;
    const occurrences = yearOccurrences(year);
    const q = normalize(state.query);
    const filtered = occurrences.filter(item => {
      if (state.category && item.event.category !== state.category) return false;
      if (state.bookmarksOnly && !state.bookmarks.has(item.event.id)) return false;
      if (!q) return true;
      const text = normalize(item.event.title + ' ' + item.event.summary + ' ' + item.event.detail + ' ' + (CATS[item.event.category] || {}).label);
      return text.indexOf(q) !== -1;
    });
    if (q) {
      filtered.sort((a, b) => {
        const at = normalize(a.event.title).indexOf(q) !== -1 ? 0 : 1;
        const bt = normalize(b.event.title).indexOf(q) !== -1 ? 0 : 1;
        return at - bt || a.epoch - b.epoch;
      });
    }
    const months = [];
    for (let m = 1; m <= 12; m++) {
      const items = filtered.filter(item => C.fromEpochDay(item.epoch).month === m);
      if (items.length) months.push({ m, items });
    }
    const chips = [`<button class="chip ${!state.category && !state.bookmarksOnly ? 'chip--active' : ''}" data-action="filter" data-cat="">همه</button>`,
      `<button class="chip ${state.bookmarksOnly ? 'chip--active' : ''}" data-action="filter-bookmarks">★ نشان‌شده‌ها</button>`]
      .concat(Object.keys(CATS).map(key =>
        `<button class="chip ${state.category === key ? 'chip--active' : ''}" data-action="filter" data-cat="${key}">${CATS[key].emoji} ${CATS[key].label}</button>`))
      .join('');

    return `
      <div class="hero" style="border-radius:0 0 26px 26px;padding-bottom:16px">
        <div class="hero__arch"></div>
        <div class="hero__top">
          <div class="hero__brand">
            <h1 class="hero__title">مناسبت‌های ایران</h1>
            <p class="hero__sub">جشن‌ها، آیین‌های کهن و بزرگداشت‌های سال ${fa(year)}</p>
          </div>
          <div class="hero__actions">${star(22, '#E7BE6A')}</div>
        </div>
      </div>
      <div class="searchbox">
        ${icon('search', 17)}
        <input id="event-search" placeholder="جست‌وجو در مناسبت‌ها…" value="${escapeAttr(state.query)}" />
      </div>
      <div class="chiprow">${chips}</div>
      ${months.map(group => `
        <div class="monthhead"><span>${monthName(group.m)} • ${fa(group.items.length)} مناسبت</span><span class="line"></span></div>
        <div class="card listcard">
          ${group.items.map(item => eventRowHtml(item.event, item.epoch, true)).join('')}
        </div>`).join('')}
      ${months.length === 0 ? '<p class="muted center" style="padding:40px">مناسبتی با این ویژگی یافت نشد.</p>' : ''}
      <div style="height:20px"></div>`;
  }

  /* ─── رندر: فرهنگ و مقاله ───────────────────────────────────────── */
  function cultureScreen() {
    const list = D.articles.filter(a => a.category === state.articleCat);
    const segmented = ARTICLE_CATS.map(cat =>
      `<button data-action="set-articlecat" data-cat="${cat.id}" aria-selected="${state.articleCat === cat.id}">${cat.label}</button>`).join('');
    return `
      <div class="hero" style="border-radius:0 0 26px 26px;padding-bottom:16px">
        <div class="hero__arch"></div>
        <div class="hero__top">
          <div class="hero__brand">
            <h1 class="hero__title">فرهنگ و آیین</h1>
            <p class="hero__sub">آیین‌ها، هنرها و دانستنی‌های ایران‌زمین</p>
          </div>
          <div class="hero__actions">${star(22, '#E7BE6A')}</div>
        </div>
      </div>
      <div class="segmented" style="flex-wrap:nowrap;overflow-x:auto">${segmented}</div>
      ${list.map(a => `
        <div class="card artcard" data-action="open-article" data-id="${a.id}">
          <div class="artcard__icon">${(ARTICLE_CATS.find(c => c.id === a.category) || {}).emoji || '📖'}</div>
          <div style="flex:1 1 auto;min-width:0">
            <div class="artcard__title">${escapeHtml(a.title)}</div>
            <div class="artcard__sub">${escapeHtml(a.subtitle)}</div>
          </div>
          ${star(10)}
        </div>`).join('')}
      <div class="card card--dark mt10">
        <div class="eventrow__title" style="color:#E7BE6A">ایران، سرزمین آیین‌ها</div>
        <p class="muted mt6" style="color:rgba(246,238,225,.85)">
          از نوروز و مهرگان تا باغ ایرانی و فرش و موسیقی، همهٔ آنچه هویت ایرانی را می‌سازد،
          در این بخش گرد آمده است. همهٔ نوشتارها در خود برنامه ذخیره‌اند و بدون اینترنت خوانده می‌شوند.
        </p>
      </div>
      <div style="height:20px"></div>`;
  }

  function articleScreen() {
    const a = D.articles.find(x => x.id === state.article);
    if (!a) return '<p class="muted center" style="padding:40px">مقاله یافت نشد.</p>';
    const cat = ARTICLE_CATS.find(c => c.id === a.category) || { label: '', emoji: '📖' };
    const bullets = (a.bullets || []).map(b =>
      `<div class="bullet-row">${star(9)}<div>
        <div class="bullet-row__term">${escapeHtml(b[0])}</div>
        <div class="bullet-row__mean">${escapeHtml(b[1])}</div>
      </div></div>`).join('');
    return `
      <div class="article-hero">
        <button class="iconbtn" data-action="back" style="margin-inline-start:-8px">${icon('right', 20)}</button>
        <span class="badge" style="background:rgba(67,198,183,.2);color:#A9EFE7">${cat.emoji} ${cat.label}</span>
        <h1>${escapeHtml(a.title)}</h1>
        <p>${escapeHtml(a.subtitle)}</p>
      </div>
      <div class="article-body">
        ${a.paragraphs.map(p => `<p>${escapeHtml(p)}</p>`).join('')}
        ${bullets ? `<div class="section-title" style="margin-inline:0">${star(12)} <span>در یک نگاه</span></div><div class="bullets">${bullets}</div>` : ''}
        ${a.quote ? `<div class="card card--dark">${star(16, '#E7BE6A')}<div class="quote">${escapeHtml(a.quote)}</div><div class="quote__source">— ${escapeHtml(a.quoteSource || '')}</div></div>` : ''}
        <p class="muted center mt14">گاهشمار پارسی • فرهنگ و آیین ایران</p>
      </div>`;
  }

  /* ─── رندر: ایران باستان ─────────────────────────────────────────── */
  function ancientScreen() {
    const groups = ERAS.map(([id, label]) => ({ id, label, items: D.timeline.filter(t => t.era === id) }))
      .filter(g => g.items.length);
    const ancientArticles = D.articles.filter(a => a.category === 'ANCIENT');
    return `
      <div class="hero" style="border-radius:0 0 26px 26px;padding-bottom:16px">
        <div class="hero__arch"></div>
        <div class="hero__top">
          <div class="hero__brand">
            <h1 class="hero__title">ایران باستان</h1>
            <p class="hero__sub">سه هزار سال تمدن، از شهر سوخته تا تیسفون</p>
          </div>
          <div class="hero__actions">${star(22, '#E7BE6A')}</div>
        </div>
      </div>
      <div class="card card--dark">
        <div class="eventrow__title" style="color:#E7BE6A">تمدن ایران، یکی از کهن‌ترین تمدن‌های پیوستهٔ جهان</div>
        <p class="muted mt6" style="color:rgba(246,238,225,.85)">
          از شهرهای پیش از تاریخ در سیستان و کاشان تا شاهنشاهی‌های ماد، هخامنشی، اشکانی و ساسانی،
          ایران زمینِ اندیشه، داد، معماری، هنر و دانش بوده است. این بخش، خط زمانی ایران را از اسطوره تا تاریخ روایت می‌کند.
        </p>
      </div>
      ${groups.map(g => `
        <div class="erahead"><span class="erahead__tag">${g.label}</span><span class="erahead__line"></span></div>
        ${g.items.map(item => `
          <div class="card">
            <div class="tl">
              <div class="tl__year">${escapeHtml(item.year)}</div>
              <div>
                <div class="tl__title">${escapeHtml(item.title)}</div>
                <div class="tl__text">${escapeHtml(item.text)}</div>
              </div>
            </div>
          </div>`).join('')}`).join('')}
      <div class="section-title">${star(12)} <span>بیشتر بخوانید</span></div>
      ${ancientArticles.map(a => `
        <div class="card artcard" data-action="open-article" data-id="${a.id}">
          <div class="artcard__icon">🏛</div>
          <div style="flex:1 1 auto;min-width:0">
            <div class="artcard__title">${escapeHtml(a.title)}</div>
            <div class="artcard__sub">${escapeHtml(a.subtitle)}</div>
          </div>
        </div>`).join('')}
      <p class="muted center" style="padding:18px">«توانا بود هر که دانا بود» — فردوسی</p>`;
  }

  /* ─── رندر: دین زرتشتی ───────────────────────────────────────────── */
  function zoroScreen() {
    const year = C.fromEpochDay(state.selected).year;
    const articles = D.articles.filter(a => a.category === 'ZOROASTRIAN');
    const amesha = (D.articles.find(a => a.id === 'ahura-mazda') || {}).bullets || [];
    const dayCells = D.dayNames.map((name, idx) => `
      <div>
        <div class="day3__name">${fa(idx + 1)}. ${name}</div>
        <div class="day3__mean">${D.dayMeanings[idx]}</div>
      </div>`).join('');
    return `
      <div class="hero" style="border-radius:0 0 26px 26px;padding-bottom:16px">
        <div class="hero__arch"></div>
        <div class="hero__top">
          <div class="hero__brand">
            <h1 class="hero__title">دین زرتشتی</h1>
            <p class="hero__sub">آیین کهن ایران؛ از گاهان زرتشت تا آتشگاه‌های امروز</p>
          </div>
          <div class="hero__actions">${icon('flame', 22)}</div>
        </div>
      </div>
      <div class="card card--dark">
        <div class="eventrow__title" style="color:#E7BE6A;font-size:16px">اندیشهٔ نیک، گفتار نیک، کردار نیک</div>
        <p class="muted mt6" style="color:rgba(246,238,225,.85)">
          زرتشتی‌گری، دین ایران باستان، بر یگانگی اورمزد، آزادی گزینش انسان و پاسداشت
          آب، آتش، خاک و گیاه استوار است. آیین‌ها و جشن‌های آن، هنوز در تقویم و فرهنگ ایرانی زنده‌اند.
        </p>
      </div>
      <div class="section-title">${star(12)} <span>امشاسپندان و فروزه‌های اورمزد</span></div>
      <div class="card">
        ${amesha.map(b => `<div class="bullet-row">${star(8)}<div>
          <div class="bullet-row__term">${escapeHtml(b[0])}</div>
          <div class="bullet-row__mean">${escapeHtml(b[1])}</div></div></div>`).join('')}
      </div>
      <div class="section-title">${star(12)} <span>سی روز ماه به نام ایزدان</span></div>
      <div class="card"><div class="daygrid3">${dayCells}</div></div>
      <div class="section-title">${star(12)} <span>جشن‌های ماهانه (روز و ماه هم‌نام)</span></div>
      <div class="card">
        ${D.festivals.map(f => `
          <div class="eventitem" data-action="open-day" data-day="${C.toEpochDay(year, f.month, Math.min(f.day, C.monthLength(year, f.month)))}">
            <div class="eventitem__date"><div class="eventitem__day">${fa(f.day)}</div><div class="eventitem__wd">${monthName(f.month)}</div></div>
            <div class="eventitem__body">
              <div class="eventitem__title">${escapeHtml(f.name)}</div>
              <div class="eventitem__sum">${escapeHtml(f.meaning)}</div>
            </div>
          </div>`).join('')}
      </div>
      <div class="section-title">${star(12)} <span>شش گاهنبار سال</span></div>
      <div class="card">
        ${D.gahambars.map(g => `<div class="row">
          <span class="badge badge--gold" style="flex:0 0 auto">${escapeHtml(g.name)}</span>
          <span class="row__value">${escapeHtml(g.seasonTime)} — ${escapeHtml(g.creation)}</span>
        </div>`).join('')}
      </div>
      <div class="section-title">${star(12)} <span>پنج گاه روز</span></div>
      <div class="card">
        ${D.gahs.map(g => `<div class="row"><span class="row__value">${escapeHtml(g)}</span></div>`).join('')}
      </div>
      <div class="section-title">${star(12)} <span>نوشتارها</span></div>
      ${articles.map(a => `
        <div class="card artcard" data-action="open-article" data-id="${a.id}">
          <div class="artcard__icon">🔥</div>
          <div style="flex:1 1 auto;min-width:0">
            <div class="artcard__title">${escapeHtml(a.title)}</div>
            <div class="artcard__sub">${escapeHtml(a.subtitle)}</div>
          </div>
        </div>`).join('')}
      <div class="card card--flat">
        <div class="eventrow__title">زرتشتیان امروز</div>
        <p class="muted mt6">
          امروز حدود صد تا دویست هزار زرتشتی در جهان زندگی می‌کنند؛ در ایران بیشتر در یزد،
          کرمان و تهران. آتشگاه یزد و آتشگاه کرمان از مهم‌ترین نیایشگاه‌های آنان‌اند و آیین‌های
          نوروز، مهرگان، سده و گاهنبارها همچنان برپا می‌شود.
        </p>
      </div>
      <div style="height:20px"></div>`;
  }

  /* ─── برگهٔ روز ───────────────────────────────────────────────────── */
  function daySheet(epoch) {
    const p = C.fromEpochDay(epoch);
    const g = C.epochDayToGregorian(epoch);
    const h = C.hijriFromEpochDay(epoch, state.hijriOffset);
    const events = eventsForDay(epoch);
    const moon = C.moonPhase(epoch * DAY_MS);
    const moments = momentsOf(g.year);
    const momentNames = ['اعتدال بهاری', 'انقلاب تابستانی', 'اعتدال پاییزی', 'انقلاب زمستانی'];
    const note = state.notes[epoch] || '';
    const nowruzTime = timeTehran(C.nowruzMomentUtcMillis(p.year));
    const body = `
      <div style="padding:0 14px">
        <div class="card" style="margin:8px 0">
          <div class="dayline">
            <div class="seal"><span class="seal__day">${fa(p.day)}</span><span class="seal__month">${monthName(p.month)}</span></div>
            <div class="daymeta">
              <div class="daymeta__title">${weekday(epoch)} ${relative(epoch) ? `<span class="badge">${relative(epoch)}</span>` : ''}</div>
              <div class="daymeta__sub">${formatDate(p)}<br>${formatGreg(g)} • ${formatHijri(h)}</div>
            </div>
          </div>
        </div>
        <div class="card" style="margin:8px 0">
          <div class="row"><span class="row__label">خورشیدی</span><span class="row__value">${formatDate(p)} • ${weekday(epoch)}</span></div>
          <div class="row"><span class="row__label">میلادی</span><span class="row__value">${formatGreg(g)} • ${g.year}-${pad2(g.month)}-${pad2(g.day)}</span></div>
          <div class="row"><span class="row__label">هجری قمری</span><span class="row__value">${formatHijri(h)}</span></div>
          <div class="row"><span class="row__label">زرتشتی</span><span class="row__value">روز ${D.dayNames[p.day - 1]} — ${D.dayMeanings[p.day - 1]}</span></div>
          <div class="row"><span class="row__label">ماه اوستایی</span><span class="row__value">${C.MONTHS_AVESTAN[p.month - 1]}</span></div>
          <div class="row"><span class="row__label">برج و فصل</span><span class="row__value">${C.ZODIAC[p.month - 1]} • ${C.seasonOf(p.month)}</span></div>
          <div class="hr"></div>
          <div class="row"><span class="row__label">روز سال</span><span class="row__value">${fa(C.dayOfYear(p.year, p.month, p.day))} از ${fa(C.yearLength(p.year))}</span></div>
          <div class="row"><span class="row__label">هفتهٔ سال</span><span class="row__value">${fa(C.weekOfYear(p.year, p.month, p.day))}</span></div>
          <div class="row"><span class="row__label">هفتهٔ ISO</span><span class="row__value">${fa(C.isoWeek(epoch))}</span></div>
          <div class="row"><span class="row__label">روز جولیَن</span><span class="row__value">${fa(epoch + C.JDN_1970)}</span></div>
          <div class="row"><span class="row__label">نماد چینی</span><span class="row__value">${C.chineseCycle(g.year)}</span></div>
          <div class="row"><span class="row__label">تحویل سال</span><span class="row__value">${nowruzTime} به وقت تهران</span></div>
        </div>
        <div class="section-title" style="margin-inline:0">${star(12)} <span>چرخهٔ کیهانی</span></div>
        <div class="card" style="margin:0 0 8px">
          <div class="row"><span class="row__label">ماه</span><span class="row__value">${moon.name} • ${fa(moon.illumination)}٪ روشنایی</span></div>
          ${momentNames.map((name, q) => {
      const mEpoch = moments[q];
      return `<div class="row"><span class="row__label">${name}</span><span class="row__value">${timeTehran(C.momentUtcMillis(g.year, q))} • ${formatDate(C.fromEpochDay(mEpoch))}</span></div>`;
    }).join('')}
          <p class="muted mt6">لحظه‌ها به وقت تهران و بر پایهٔ روش نجومی مئوس محاسبه شده‌اند (دقت چند دقیقه).</p>
        </div>
        <div class="section-title" style="margin-inline:0">${star(12)} <span>مناسبت‌های این روز</span></div>
        <div class="card" style="margin:0 0 8px">
          ${events.length ? events.map(ev => `
            <div class="eventrow">
              <span class="eventrow__bullet ${ev.observance !== 'NONE' ? 'eventrow__bullet--red' : ''}"></span>
              <div class="eventrow__body">
                <div class="eventrow__title ${ev.observance !== 'NONE' ? 'eventrow__title--holiday' : ''}">${(CATS[ev.category] || {}).emoji || ''} ${escapeHtml(ev.title)}</div>
                <div class="eventrow__sum">${escapeHtml(ev.summary)}</div>
                ${ev.detail ? `<div class="eventrow__sum" style="margin-top:6px;color:var(--ink)">${escapeHtml(ev.detail)}</div>` : ''}
                <div class="eventrow__tags">
                  <span class="badge badge--gold">${(CATS[ev.category] || {}).label || ''}</span>
                  ${ev.observance !== 'NONE' ? `<span class="badge badge--red">${ev.observance === 'PUBLIC' ? 'تعطیل رسمی' : 'آیین کهن'}</span>` : ''}
                </div>
                ${ev.articleId ? `<button class="btn mt6" data-action="open-article" data-id="${ev.articleId}">بیشتر بخوانید</button>` : ''}
              </div>
            </div>`).join('') : '<p class="muted">برای این روز مناسبتی ثبت نشده است.</p>'}
        </div>
        <div class="section-title" style="margin-inline:0">${star(12)} <span>یادداشت من</span></div>
        <div class="card notecard" style="margin:0 0 8px">
          <textarea id="note-${epoch}" placeholder="یادداشتی برای این روز بنویسید…">${escapeHtml(note)}</textarea>
          <div class="actions">
            <button class="btn btn--primary" data-action="save-note" data-day="${epoch}">ذخیره</button>
            <button class="btn" data-action="share-day" data-day="${epoch}">${icon('share', 15)} اشتراک‌گذاری</button>
          </div>
        </div>
        <div class="actions" style="justify-content:space-between;padding-bottom:10px">
          <button class="btn" data-action="day-step" data-step="-1" data-day="${epoch}">${icon('right', 15)} روز پیش</button>
          <button class="btn" data-action="day-step" data-step="1" data-day="${epoch}">روز بعد ${icon('left', 15)}</button>
        </div>
      </div>`;
    return { title: `روزنگار — ${formatDate(p)}`, body };
  }

  /* ─── برگهٔ تنظیمات و تبدیل ──────────────────────────────────────── */
  function settingsSheet() {
    const themeBtn = (id, label) =>
      `<button class="chip ${state.theme === id ? 'chip--active' : ''}" data-action="set-theme" data-theme-id="${id}">${label}</button>`;
    const body = `
      <div style="padding:0 14px">
        <div class="section-title" style="margin-inline:0">${star(12)} <span>پوسته</span></div>
        <div style="display:flex;gap:6px;flex-wrap:wrap">
          ${themeBtn('light', 'روشن')}${themeBtn('dark', 'تیره')}${themeBtn('system', 'هماهنگ با سامانه')}
        </div>

        <div class="section-title" style="margin-inline:0">${star(12)} <span>گاه‌شماری</span></div>
        <div class="card" style="margin:0">
          <div class="switchrow">
            <div class="switchrow__text">رقم‌های فارسی<div class="switchrow__sub">نمایش ۱۲۳ به‌جای 123</div></div>
            <button class="switch" role="switch" aria-checked="${state.digits}" data-action="toggle-digits"></button>
          </div>
          <div class="switchrow">
            <div class="switchrow__text">عدد میلادی زیر روز<div class="switchrow__sub">در خانه‌های تقویم خورشیدی</div></div>
            <button class="switch" role="switch" aria-checked="${state.showGregorianNumbers}" data-action="toggle-greg"></button>
          </div>
          <div class="switchrow">
            <div class="switchrow__text">مناسبت‌های نجومی<div class="switchrow__sub">اعتدالین، انقلابین و نوروز نجومی</div></div>
            <button class="switch" role="switch" aria-checked="${state.showAstronomy}" data-action="toggle-astro"></button>
          </div>
          <div class="switchrow">
            <div class="switchrow__text">آغاز هفته</div>
            <div class="stepper">
              <button data-action="toggle-week">${state.weekStart === 'saturday' ? 'شنبه' : 'یکشنبه'}</button>
            </div>
          </div>
          <div class="switchrow">
            <div class="switchrow__text">تصحیح هجری قمری<div class="switchrow__sub">تقویم قمری حسابی است و ممکن است با رؤیت هلال اختلاف داشته باشد</div></div>
            <div class="stepper">
              <button data-action="hijri-minus">−</button><span>${fa(state.hijriOffset)}</span><button data-action="hijri-plus">+</button>
            </div>
          </div>
        </div>

        <div class="section-title" style="margin-inline:0">${star(12)} <span>ابزارها</span></div>
        <div class="card" style="margin:0">
          <button class="btn btn--primary" data-action="converter">${icon('swap', 15)} تبدیل تاریخ</button>
          <p class="muted mt10">
            این پیش‌نمایش وب همهٔ محاسبات را در مرورگر و بدون اینترنت انجام می‌دهد؛
            نسخهٔ اندروید نیز هیچ دسترسی اینترنتی ندارد.
          </p>
        </div>
        <div style="height:16px"></div>
      </div>`;
    return { title: 'تنظیمات', body };
  }

  function converterSheet() {
    const p = C.fromEpochDay(state.selected);
    const g = C.epochDayToGregorian(state.selected);
    const h = C.hijriFromEpochDay(state.selected, state.hijriOffset);
    const body = `
      <div style="padding:0 14px">
        <div class="card" style="margin:0 0 8px">
          <div class="field-row">
            <div class="field"><label>سال خورشیدی</label><input id="cv-sy" inputmode="numeric" value="${p.year}"></div>
            <div class="field"><label>ماه</label><input id="cv-sm" inputmode="numeric" value="${p.month}"></div>
            <div class="field"><label>روز</label><input id="cv-sd" inputmode="numeric" value="${p.day}"></div>
          </div>
          <div class="actions">
            <button class="btn btn--primary" data-action="convert">تبدیل</button>
            <button class="btn" data-action="convert-today">امروز</button>
          </div>
        </div>
        <div id="convert-result">
          ${convertResultHtml(state.selected)}
        </div>
      </div>`;
    return { title: 'تبدیل تاریخ', body };
  }

  function convertResultHtml(epoch) {
    const p = C.fromEpochDay(epoch);
    const g = C.epochDayToGregorian(epoch);
    const h = C.hijriFromEpochDay(epoch, state.hijriOffset);
    return `<div class="card" style="margin:0">
      <div class="row"><span class="row__label">خورشیدی</span><span class="row__value">${formatDate(p)} • ${weekday(epoch)}</span></div>
      <div class="row"><span class="row__label">میلادی</span><span class="row__value">${formatGreg(g)}</span></div>
      <div class="row"><span class="row__label">هجری قمری</span><span class="row__value">${formatHijri(h)}</span></div>
      <div class="hr"></div>
      <div class="row"><span class="row__label">روز سال</span><span class="row__value">${fa(C.dayOfYear(p.year, p.month, p.day))}</span></div>
      <div class="row"><span class="row__label">هفتهٔ سال</span><span class="row__value">${fa(C.weekOfYear(p.year, p.month, p.day))} • ISO ${fa(C.isoWeek(epoch))}</span></div>
      <div class="row"><span class="row__label">روز زرتشتی</span><span class="row__value">${D.dayNames[p.day - 1]}</span></div>
      <div class="row"><span class="row__label">برج</span><span class="row__value">${C.ZODIAC[p.month - 1]}</span></div>
      <div class="row"><span class="row__label">کبیسه</span><span class="row__value">${C.isLeapYear(p.year) ? 'سال کبیسه است' : 'سال کبیسه نیست'}</span></div>
      <div class="row"><span class="row__label">نماد چینی</span><span class="row__value">${C.chineseCycle(g.year)}</span></div>
    </div>`;
  }

  function aboutSheet() {
    const body = `
      <div style="padding:0 14px">
        <div class="card card--dark" style="margin:0 0 8px">
          ${star(24, '#E7BE6A')}
          <div class="eventrow__title" style="color:#F6EEE1;font-size:20px;font-family:'Lalezar','Vazirmatn',sans-serif">گاهشمار پارسی</div>
          <p class="muted" style="color:rgba(246,238,225,.85)">
            برنامه‌ای برای تقویم خورشیدی ایران، مناسبت‌های کهن، آیین‌های زرتشتی و فرهنگ ۲۵۰۰ سالهٔ ایران.
          </p>
        </div>
        <div class="card" style="margin:0 0 8px">
          <div class="eventrow__title">ویژگی‌ها</div>
          ${[
        'تقویم خورشیدی با محاسبهٔ نجومی رسمی ایران',
        'گاه‌شماری میلادی و هجری قمری و چینی در یک نما',
        'پنجاه مناسبت ایرانی، زرتشتی و ادبی',
        'نوشتارهای فرهنگی، آیینی، باستانی و دینی',
        'خط زمان ایران باستان و پیوند به مقاله‌ها',
        'تبدیل تاریخ، یادداشت روزانه و تنظیمات کامل',
        'کاملاً آفلاین و بی‌نیاز از مجوز اینترنت'
      ].map(t => `<div class="bullet-row">${star(7)}<div class="bullet-row__term" style="font-weight:400">${t}</div></div>`).join('')}
        </div>
        <div class="card" style="margin:0 0 8px">
          <div class="eventrow__title">روش محاسبه</div>
          <p class="muted mt6">
            آغاز سال خورشیدی، نزدیک‌ترین روز به لحظهٔ اعتدال بهاری است که نیمروزش به وقت تهران
            به آن لحظه نزدیک باشد. لحظهٔ اعتدال با الگوریتم نجومی مئوس و تصحیح ΔT محاسبه می‌شود.
            گاه‌شماری قمری به روش حسابی است.
          </p>
        </div>
        <div class="card" style="margin:0 0 8px">
          <div class="eventrow__title">سپاس‌ها</div>
          <p class="muted mt6">
            قلم «وزیرمتن» ساختهٔ صابر راستی‌کردار و قلم «لاله‌زار» ساختهٔ Borna Izadpanah، هر دو با پروانهٔ OFL.
            این صفحه پیش‌نمایش وب برنامهٔ اندروید است که با Kotlin و Jetpack Compose نوشته شده است.
          </p>
        </div>
      </div>`;
    return { title: 'دربارهٔ برنامه', body };
  }

  /* ─── ابزارهای امنیتی متن ────────────────────────────────────────── */
  function escapeHtml(text) {
    return String(text == null ? '' : text)
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
  }
  function escapeAttr(text) { return escapeHtml(text); }
  function normalize(text) {
    return String(text || '')
      .replace(/ي/g, 'ی').replace(/ك/g, 'ک')
      .replace(/\u200c/g, '').replace(/\s+/g, '')
      .toLowerCase();
  }

  /* ─── برگه‌ها ────────────────────────────────────────────────────── */
  const layer = document.getElementById('sheet-layer');
  let sheetOpen = false;

  function openSheet(title, bodyHtml) {
    layer.innerHTML = `
      <div class="sheet-backdrop" data-action="close-sheet"></div>
      <div class="sheet" role="dialog" aria-modal="true" aria-label="${escapeAttr(title)}">
        <div class="sheet__handle"></div>
        <div class="sheet__head">
          <div class="sheet__title">${escapeHtml(title)}</div>
          <button class="iconbtn" data-action="close-sheet" style="color:var(--ink-soft)">${icon('close', 18)}</button>
        </div>
        <div class="sheet__body">${bodyHtml}</div>
      </div>`;
    requestAnimationFrame(() => layer.classList.add('open'));
    sheetOpen = true;
  }

  function closeSheet() {
    layer.classList.remove('open');
    sheetOpen = false;
    setTimeout(() => { if (!sheetOpen) layer.innerHTML = ''; }, 320);
  }

  function toast(message) {
    let el = document.querySelector('.toast');
    if (!el) {
      el = document.createElement('div');
      el.className = 'toast';
      document.body.appendChild(el);
    }
    el.textContent = message;
    el.classList.add('show');
    clearTimeout(el._timer);
    el._timer = setTimeout(() => el.classList.remove('show'), 1900);
  }

  /* ─── رندر اصلی ──────────────────────────────────────────────────── */
  const app = document.getElementById('app');
  const tabbar = document.getElementById('tabbar');

  const TABS = [
    { id: 'calendar', label: 'تقویم', icon: 'calendar' },
    { id: 'events', label: 'مناسبت‌ها', icon: 'star' },
    { id: 'culture', label: 'فرهنگ', icon: 'heart' },
    { id: 'ancient', label: 'باستان', icon: 'column' },
    { id: 'zoro', label: 'زرتشتی', icon: 'flame' }
  ];

  function renderTabs() {
    tabbar.innerHTML = TABS.map(tab => `
      <button data-tab="${tab.id}" ${state.tab === tab.id ? 'aria-current="page"' : ''}>
        ${icon(tab.icon, 21)}
        <span>${tab.label}</span>
        <span class="tabdot"></span>
      </button>`).join('');
  }

  function render(keepScroll) {
    const scrollTop = app.scrollTop;
    let html = '';
    if (state.article) {
      html = articleScreen();
    } else {
      switch (state.tab) {
        case 'calendar': html = calendarScreen(); break;
        case 'events': html = eventsScreen(); break;
        case 'culture': html = cultureScreen(); break;
        case 'ancient': html = ancientScreen(); break;
        case 'zoro': html = zoroScreen(); break;
      }
    }
    app.innerHTML = html;
    renderTabs();
    if (state.article) {
      app.scrollTop = 0;
    } else if (keepScroll) {
      app.scrollTop = scrollTop;
    }
    const search = document.getElementById('event-search');
    if (search) {
      search.addEventListener('input', ev => {
        state.query = ev.target.value;
        const pos = ev.target.selectionStart;
        render();
        const box = document.getElementById('event-search');
        if (box) { box.focus(); box.setSelectionRange(pos, pos); }
      });
    }
  }

  /* ─── رویدادها ───────────────────────────────────────────────────── */
  document.addEventListener('click', event => {
    const target = event.target.closest('[data-action], [data-tab], [data-day]');
    if (!target) return;

    if (target.dataset.tab) {
      state.tab = target.dataset.tab;
      state.article = null;
      render();
      app.scrollTop = 0;
      return;
    }

    const action = target.dataset.action;
    const dayAttr = target.dataset.day;

    // کلیک روی خانهٔ تقویم ≈ باز کردن روزنگار
    if (!action && dayAttr) {
      openDay(+dayAttr);
      return;
    }

    switch (action) {
      case 'set-mode':
        state.mode = target.dataset.mode;
        syncViews(state.selected);
        render();
        break;
      case 'prev-month':
      case 'next-month': {
        const delta = action === 'next-month' ? 1 : -1;
        if (state.yearView) {
          if (state.mode === 'solar') state.solar = { y: state.solar.y + delta, m: state.solar.m };
          else if (state.mode === 'gregorian') state.greg = { y: state.greg.y + delta, m: state.greg.m };
          else state.hijri = { y: state.hijri.y + delta, m: state.hijri.m };
          render(true);
          break;
        }
        if (state.mode === 'solar') {
          const idx = state.solar.y * 12 + (state.solar.m - 1) + delta;
          state.solar = { y: Math.floor(idx / 12), m: idx % 12 + 1 };
        } else if (state.mode === 'gregorian') {
          const idx = state.greg.y * 12 + (state.greg.m - 1) + delta;
          state.greg = { y: Math.floor(idx / 12), m: idx % 12 + 1 };
        } else {
          const idx = state.hijri.y * 12 + (state.hijri.m - 1) + delta;
          state.hijri = { y: Math.floor(idx / 12), m: idx % 12 + 1 };
        }
        render();
        break;
      }
      case 'view-month':
        state.yearView = false;
        render();
        break;
      case 'view-year':
        state.yearView = true;
        syncViews(state.selected);
        render();
        break;
      case 'pick-day': {
        state.selected = +dayAttr;
        syncViews(+dayAttr);
        render(true);
        break;
      }
      case 'set-month':
        state.solar = { y: state.solar.y, m: +target.dataset.month };
        render();
        break;
      case 'today':
        state.selected = today;
        syncViews(today);
        render();
        break;
      case 'open-day':
        openDay(+dayAttr);
        break;
      case 'day-step':
        openDay(+dayAttr + (+target.dataset.step));
        break;
      case 'save-note': {
        const epoch = +dayAttr;
        const box = document.getElementById('note-' + epoch);
        if (box) {
          state.notes[epoch] = box.value;
          store.set('notes', state.notes);
          toast('یادداشت ذخیره شد');
          render(true);
        }
        break;
      }
      case 'share-day': {
        const epoch = +dayAttr;
        const p = C.fromEpochDay(epoch);
        const g = C.epochDayToGregorian(epoch);
        const h = C.hijriFromEpochDay(epoch, state.hijriOffset);
        const lines = [`${formatDate(p)}، ${weekday(epoch)}`,
        `میلادی: ${formatGreg(g)}`, `قمری: ${formatHijri(h)}`];
        const evs = eventsForDay(epoch);
        if (evs.length) {
          lines.push('', 'مناسبت‌ها:');
          evs.forEach(ev => lines.push('• ' + ev.title));
        }
        lines.push('', 'گاهشمار پارسی');
        const text = lines.join('\n');
        if (navigator.share) {
          navigator.share({ text }).catch(() => { });
        } else if (navigator.clipboard) {
          navigator.clipboard.writeText(text).then(() => toast('در حافظه کپی شد'));
        }
        break;
      }
      case 'filter':
        state.category = target.dataset.cat || null;
        state.bookmarksOnly = false;
        render();
        break;
      case 'filter-bookmarks':
        state.bookmarksOnly = !state.bookmarksOnly;
        state.category = null;
        render();
        break;
      case 'set-articlecat':
        state.articleCat = target.dataset.cat;
        render();
        break;
      case 'open-article':
        state.article = target.dataset.id;
        state.tab = 'culture';
        closeSheet();
        render();
        app.scrollTop = 0;
        break;
      case 'back':
        state.article = null;
        render();
        break;
      case 'settings': {
        const sheet = settingsSheet();
        openSheet(sheet.title, sheet.body);
        break;
      }
      case 'converter': {
        const sheet = converterSheet();
        openSheet(sheet.title, sheet.body);
        break;
      }
      case 'book': {
        const sheet = aboutSheet();
        openSheet(sheet.title, sheet.body);
        break;
      }
      case 'close-sheet':
        closeSheet();
        break;
      case 'set-theme':
        setTheme(target.dataset.themeId);
        {
          const sheet = settingsSheet();
          openSheet(sheet.title, sheet.body);
          render(true);
        }
        break;
      case 'toggle-digits':
        state.digits = !state.digits;
        store.set('digits', state.digits);
        {
          const sheet = settingsSheet();
          openSheet(sheet.title, sheet.body);
          render(true);
        }
        break;
      case 'toggle-greg':
        state.showGregorianNumbers = !state.showGregorianNumbers;
        store.set('showGregorianNumbers', state.showGregorianNumbers);
        render(true);
        {
          const sheet = settingsSheet();
          openSheet(sheet.title, sheet.body);
        }
        break;
      case 'toggle-astro':
        state.showAstronomy = !state.showAstronomy;
        store.set('showAstronomy', state.showAstronomy);
        render(true);
        {
          const sheet = settingsSheet();
          openSheet(sheet.title, sheet.body);
        }
        break;
      case 'toggle-week':
        state.weekStart = state.weekStart === 'saturday' ? 'sunday' : 'saturday';
        store.set('weekStart', state.weekStart);
        render(true);
        {
          const sheet = settingsSheet();
          openSheet(sheet.title, sheet.body);
        }
        break;
      case 'hijri-minus':
      case 'hijri-plus': {
        const delta = action === 'hijri-plus' ? 1 : -1;
        state.hijriOffset = Math.max(-3, Math.min(3, state.hijriOffset + delta));
        store.set('hijriOffset', state.hijriOffset);
        syncViews(state.selected);
        render(true);
        {
          const sheet = settingsSheet();
          openSheet(sheet.title, sheet.body);
        }
        break;
      }
      case 'convert': {
        const sy = parseInt((document.getElementById('cv-sy') || {}).value, 10);
        const sm = parseInt((document.getElementById('cv-sm') || {}).value, 10);
        const sd = parseInt((document.getElementById('cv-sd') || {}).value, 10);
        const box = document.getElementById('convert-result');
        if (!sy || !sm || !sd || sm < 1 || sm > 12 || sd < 1 || sd > C.monthLength(sy, sm)) {
          if (box) box.innerHTML = '<div class="card" style="margin:0"><p class="muted" style="color:var(--pomegranate)">تاریخ واردشده معتبر نیست. سال، ماه و روز را بررسی کنید.</p></div>';
          break;
        }
        const epoch = C.toEpochDay(sy, sm, sd);
        if (box) box.innerHTML = convertResultHtml(epoch);
        break;
      }
      case 'convert-today': {
        const p = C.fromEpochDay(today);
        const g = C.epochDayToGregorian(today);
        const boxes = [['cv-sy', p.year], ['cv-sm', p.month], ['cv-sd', p.day], ['cv-gy', g.year], ['cv-gm', g.month], ['cv-gd', g.day]];
        boxes.forEach(([id, value]) => {
          const el = document.getElementById(id);
          if (el) el.value = value;
        });
        const box = document.getElementById('convert-result');
        if (box) box.innerHTML = convertResultHtml(today);
        break;
      }
      default:
        break;
    }
  });

  function openDay(epoch) {
    state.selected = epoch;
    syncViews(epoch);
    render(true);
    const sheet = daySheet(epoch);
    openSheet(sheet.title, sheet.body);
  }

  function syncViews(epoch) {
    const p = C.fromEpochDay(epoch);
    const g = C.epochDayToGregorian(epoch);
    const h = C.hijriFromEpochDay(epoch, state.hijriOffset);
    state.solar = { y: p.year, m: p.month };
    state.greg = { y: g.year, m: g.month };
    state.hijri = { y: h.year, m: h.month };
  }

  function setTheme(mode) {
    state.theme = mode;
    store.set('theme', mode);
    applyTheme();
  }

  function applyTheme() {
    let resolved = state.theme;
    if (resolved === 'system') {
      resolved = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }
    document.documentElement.setAttribute('data-theme', resolved);
    const meta = document.querySelector('meta[name="theme-color"]');
    if (meta) meta.setAttribute('content', resolved === 'dark' ? '#0D1424' : '#152A54');
  }

  // ساعت نوار وضعیت
  function tick() {
    const el = document.getElementById('status-time');
    if (!el) return;
    const now = new Date();
    el.textContent = fa(`${pad2(now.getHours())}:${pad2(now.getMinutes())}`);
  }

  /* ─── راه‌اندازی ─────────────────────────────────────────────────── */
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && sheetOpen) closeSheet();
    if (!sheetOpen && !state.article) {
      if (event.key === 'ArrowRight') { state.selected = Math.max(state.selected - 1, today - 4000); syncViews(state.selected); render(true); }
      if (event.key === 'ArrowLeft') { state.selected = state.selected + 1; syncViews(state.selected); render(true); }
    }
  });

  if (window.matchMedia) {
    const mq = window.matchMedia('(prefers-color-scheme: dark)');
    const onChange = () => { if (state.theme === 'system') applyTheme(); };
    if (mq.addEventListener) mq.addEventListener('change', onChange);
    else if (mq.addListener) mq.addListener(onChange);
  }

  applyTheme();
  tick();
  setInterval(tick, 20000);
  render();
})();
