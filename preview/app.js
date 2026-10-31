/* ==========================================================================
   گاه‌شمار ایرانی — پیش‌نمایش وب
   یک صفحه: تاریخ برگزیده، دوازده ماه، شبکهٔ روزها و جشن‌های همان روز.
   ========================================================================== */
(function () {
  'use strict';

  const C = window.GahCalendar;
  const I = window.Iranian;
  const D = window.GAH_DATA;

  const DAY_MS = 86400000;
  const TEHRAN_MS = 3.5 * 3600 * 1000;
  const TODAY = Math.floor((Date.now() + TEHRAN_MS) / DAY_MS);

  const SHORT_WEEK = D.weekdayShort;
  const WEEK = D.weekdays;
  const KIND_LABEL = { JASHN: 'جشن', AYIN: 'آیین', GAHAMBAR: 'گاهنبار', TAJGOZARI: 'تاج‌گذاری', ASATIR: 'اسطوره', YADBUD: 'یادبود' };
  const KIND_CLASS = { JASHN: 'jashn', AYIN: 'ayin', GAHAMBAR: 'gahambar', TAJGOZARI: 'tajgozari', ASATIR: 'asatir', YADBUD: 'yadbud' };

  const store = {
    get(key, fallback) {
      try {
        const raw = localStorage.getItem('giran:' + key);
        return raw === null ? fallback : JSON.parse(raw);
      } catch (e) { return fallback; }
    },
    set(key, value) {
      try { localStorage.setItem('giran:' + key, JSON.stringify(value)); } catch (e) { }
    }
  };

  const state = {
    selected: TODAY,
    theme: store.get('theme', 'dark'),
    digits: store.get('digits', true)
  };

  /* ─── ابزارها ─────────────────────────────────────────────────────── */
  const FA = ['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];
  const toFa = (text) => String(text).replace(/\d/g, (d) => FA[+d]);
  const fa = (value) => (state.digits ? toFa(value) : String(value));
  const pad2 = (n) => String(n).padStart(2, '0');

  const date = () => I.fromEpochDay(state.selected);
  const weekdayName = (epoch) => WEEK[C.weekdayIndex(epoch)];

  function icons(name, size) {
    const s = size || 18;
    const paths = {
      settings: '<circle cx="12" cy="12" r="3.2"/><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.9l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.9-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.9.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.9 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1A1.7 1.7 0 0 0 4.6 9a1.7 1.7 0 0 0-.3-1.9l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.9.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.9-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.9V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z"/>',
      close: '<path d="M6 6l12 12M18 6L6 18"/>',
      share: '<path d="M4 12v7a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-7"/><path d="M12 16V3M8 7l4-4 4 4"/>',
      right: '<path d="M9 5l7 7-7 7"/>',
      left: '<path d="M15 5l-7 7 7 7"/>'
    };
    return `<svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${paths[name] || ''}</svg>`;
  }

  function star(size, color) {
    const pts = [];
    for (let i = 0; i < 16; i++) {
      const r = i % 2 === 0 ? 10 : 4.2;
      const a = -Math.PI / 2 + i * Math.PI / 8;
      pts.push((11 + r * Math.cos(a)).toFixed(2) + ',' + (11 + r * Math.sin(a)).toFixed(2));
    }
    return `<svg width="${size || 13}" height="${size || 13}" viewBox="0 0 22 22" fill="${color || 'currentColor'}" aria-hidden="true"><polygon points="${pts.join(' ')}"/></svg>`;
  }

  function escapeHtml(text) {
    return String(text == null ? '' : text)
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
  }

  function daysAway(days) {
    if (days === 0) return 'امروز';
    if (days === 1) return 'فردا';
    if (days === -1) return 'دیروز';
    if (days === 2) return 'پس‌فردا';
    return days > 0 ? `${fa(days)} روز دیگر` : `${fa(-days)} روز پیش`;
  }

  function longDate(d) {
    return `${fa(d.day)} ${I.monthName(d.month)} ${fa(I.pahlaviYear(d.year))}`;
  }

  /* ─── داده‌های محاسبه‌شده ─────────────────────────────────────────── */
  function festivalDaysOf(year, month) {
    const set = new Set();
    I.festivalsOfYear(D.festivals, year).forEach((item) => {
      const d = I.fromEpochDay(item.epochDay);
      if ((d.panjehIndex && month === 12) || (!d.panjehIndex && d.month === month)) {
        set.add(item.epochDay);
      }
    });
    return set;
  }

  function cellsOf(year, month) {
    const first = I.monthStart(year, month);
    const leading = C.weekdayIndex(first);
    const panjeh = month === 12 ? I.panjehLength(year) : 0;
    const regular = I.monthLength(year, month) - panjeh;
    const cells = [];
    for (let i = 0; i < leading; i++) cells.push(null);
    for (let day = 1; day <= regular; day++) cells.push(day);
    for (let p = 1; p <= panjeh; p++) cells.push(-p);
    return cells;
  }

  function cellEpoch(year, month, cell) {
    return cell > 0 ? I.toEpochDay(year, month, cell) : I.panjehStart(year) + (-cell - 1);
  }

  /* ─── رندر ───────────────────────────────────────────────────────── */
  const app = document.getElementById('app');

  function render(keepScroll) {
    const scrollTop = app.scrollTop;
    const d = date();
    const year = d.year;
    const month = d.panjehIndex ? 12 : d.month;
    const festivals = I.festivalsOn(D.festivals, state.selected);
    const festivalDays = festivalDaysOf(year, month);
    const cells = cellsOf(year, month);

    const cellHtml = cells.map((cell) => {
      if (cell === null) return '<button class="cell cell--empty" disabled></button>';
      const epoch = cellEpoch(year, month, cell);
      const isSelected = epoch === state.selected;
      const isToday = epoch === TODAY;
      const friday = C.weekdayIndex(epoch) === 6;
      const classes = ['cell'];
      if (isSelected) classes.push('cell--selected');
      if (isToday) classes.push('cell--today');
      if (friday) classes.push('cell--friday');
      if (cell < 0) classes.push('cell--panjeh');
      const label = fa(cell > 0 ? cell : -cell);
      const dot = festivalDays.has(epoch) ? '<span class="cell__dot"></span>' : '<span class="cell__dot" style="opacity:0"></span>';
      return `<button class="${classes.join(' ')}" data-day="${epoch}" title="${escapeHtml(longDate(I.fromEpochDay(epoch)))}">${label}${dot}</button>`;
    }).join('');

    const monthPills = I.MONTH_NAMES.map((name, index) =>
      `<button class="monthpill ${index + 1 === month ? 'monthpill--active' : ''}" data-month="${index + 1}">${name}</button>`
    ).join('');

    const festivalsHtml = festivals.length
      ? festivals.map((f) => `
        <div class="festcard" data-festival="${f.id}">
          <div class="festcard__top">
            <span class="badge badge--${KIND_CLASS[f.kind] || 'jashn'}">${KIND_LABEL[f.kind] || 'جشن'}</span>
            <span class="festcard__away">${daysAway(state.selected - TODAY)}</span>
          </div>
          <div class="festcard__name">${escapeHtml(f.name)}</div>
          <div class="festcard__sum">${escapeHtml(f.summary)}</div>
        </div>`).join('')
      : `<div class="empty">
           این روز جشن ویژه‌ای در گاه‌شمار ایرانی ندارد.
           <span>اما هر روز نامی دارد و نشانی: روز ${I.nameOf(d)} از ${d.panjehIndex ? 'پنجهٔ وهیک' : I.monthName(d.month) + ' ماه'}.</span>
         </div>`;

    const upcoming = I.festivalsOfYear(D.festivals, year)
      .concat(I.festivalsOfYear(D.festivals, year + 1))
      .filter((item) => item.epochDay > state.selected)
      .slice(0, 3);

    app.innerHTML = `
      <div class="topbar">
        ${star(15, 'var(--gold)')}
        <div class="topbar__title">گاه‌شمار ایرانی
          <small>${longDate(d)}</small>
        </div>
        ${state.selected !== TODAY ? '<button class="topbar__today" data-action="today">امروز</button>' : ''}
        <button class="iconbtn" data-action="settings" aria-label="تنظیمات">${icons('settings', 19)}</button>
      </div>

      <div class="hero">
        <div class="hero__row">
          <div class="hero__day">${fa(d.day)}</div>
          <div>
            <div class="hero__month">${I.monthName(d.month)} ${fa(I.pahlaviYear(d.year))}</div>
            <div class="hero__meta">${weekdayName(state.selected)}
              ${state.selected === TODAY ? '<span class="hero__dot"></span>' : ''}
            </div>
          </div>
          <div class="hero__nav">
            <button class="iconbtn" data-action="prev-day" aria-label="روز پیش">${icons('right', 20)}</button>
            <button class="iconbtn" data-action="next-day" aria-label="روز بعد">${icons('left', 20)}</button>
          </div>
        </div>
        ${d.panjehIndex ? '<div class="hero__badge">پنجهٔ وهیک</div>' : ''}
        <div class="hero__ritual">${I.ritualLabel(d)}</div>
        <div class="hero__meaning">${I.meaningOf(d)}</div>
      </div>

      <div class="yearbar">
        <button class="iconbtn" data-action="prev-year" aria-label="سال پیش">${icons('right', 19)}</button>
        <div class="yearbar__label">سال ${fa(I.pahlaviYear(year))}</div>
        <button class="iconbtn" data-action="next-year" aria-label="سال پس">${icons('left', 19)}</button>
      </div>

      <div class="months">${monthPills}</div>

      <div class="weekdays">${SHORT_WEEK.map((label) => `<span>${label}</span>`).join('')}</div>
      <div class="grid">${cellHtml}</div>

      <div class="hint">برای دیدن جشن‌ها و آیین‌های هر روز، روی آن روز بزنید.</div>

      <div class="section">${star(12)} <span>جشن و آیین این روز</span></div>
      <div class="festivals">${festivalsHtml}</div>

      ${upcoming.length ? `<div class="section">${star(11)} <span>جشن‌های پیشِ رو</span></div>
      <div class="upcoming">
        ${upcoming.map((item) => {
      const ud = I.fromEpochDay(item.epochDay);
      return `<div class="uprow" data-day="${item.epochDay}">
            <div class="uprow__box">${fa(ud.day)}</div>
            <div>
              <div class="uprow__name">${escapeHtml(item.festival.name)}</div>
              <div class="uprow__meta">${I.monthName(ud.month)}${ud.panjehIndex ? ' • پنجهٔ وهیک' : ''} • ${weekdayName(item.epochDay)}</div>
            </div>
            <div class="uprow__away">${daysAway(item.epochDay - TODAY)}</div>
          </div>`;
    }).join('')}
      </div>` : ''}

      <p class="footer">ماه‌ها با درازای تقویم ایران، سی روزِ نام‌دار و پنجهٔ وهیک؛ سال از اعتدال بهاری به وقت تهران آغاز می‌شود.</p>`;

    if (keepScroll) app.scrollTop = scrollTop;
    scrollActiveMonthIntoView();
  }

  /** ماهِ جاری را در ابتدای نوار ماه‌ها می‌آورد (راست‌به‌چپ هم درست کار می‌کند) */
  function scrollActiveMonthIntoView() {
    const active = app.querySelector('.monthpill--active');
    if (!active) return;
    const strip = active.closest('.months') || app.querySelector('.months');
    if (!strip) return;
    if (typeof active.scrollIntoView === 'function') {
      active.scrollIntoView({ inline: 'center', block: 'nearest', behavior: 'smooth' });
    }
  }

  /* ─── برگه‌ها ────────────────────────────────────────────────────── */
  const layer = document.getElementById('sheet-layer');
  const toastEl = document.getElementById('toast');
  let sheetOpen = false;

  function openSheet(title, body) {
    layer.innerHTML = `
      <div class="sheet-backdrop" data-action="close"></div>
      <div class="sheet" role="dialog" aria-modal="true" aria-label="${escapeHtml(title)}">
        <div class="sheet__handle"></div>
        <div class="sheet__head">
          <div class="sheet__title">${escapeHtml(title)}</div>
          <button class="iconbtn" data-action="close" aria-label="بستن">${icons('close', 18)}</button>
        </div>
        <div class="sheet__body">${body}</div>
      </div>`;
    sheetOpen = true;
    requestAnimationFrame(() => layer.classList.add('open'));
  }

  function closeSheet() {
    layer.classList.remove('open');
    sheetOpen = false;
    setTimeout(() => { if (!sheetOpen) layer.innerHTML = ''; }, 320);
  }

  function toast(message) {
    toastEl.textContent = message;
    toastEl.classList.add('show');
    clearTimeout(toastEl._t);
    toastEl._t = setTimeout(() => toastEl.classList.remove('show'), 1800);
  }

  function festivalSheet(id) {
    const festival = D.festivals.find((f) => f.id === id);
    if (!festival) return;
    const year = date().year;
    const epoch = I.dateInYear(festival, year);
    const when = epoch === null ? '' : `
      <div class="sheet__when">
        در سال ${fa(I.pahlaviYear(year))}: ${escapeHtml(longDate(I.fromEpochDay(epoch)))}، ${weekdayName(epoch)}
        ${epoch === state.selected ? ' (همین روز)' : ''}
      </div>`;
    openSheet(festival.name, `
      <span class="badge badge--${KIND_CLASS[festival.kind] || 'jashn'}">${KIND_LABEL[festival.kind] || 'جشن'}</span>
      <div class="sheet__name">${escapeHtml(festival.name)}</div>
      <div class="sheet__sum">${escapeHtml(festival.summary)}</div>
      <div class="sheet__text">${escapeHtml(festival.detail)}</div>
      ${when}
    `);
  }

  function settingsSheet() {
    const themeChip = (id, label) =>
      `<button class="chip ${state.theme === id ? 'chip--active' : ''}" data-theme-id="${id}">${label}</button>`;
    openSheet('تنظیمات', `
      <div class="sheet__label">پوسته</div>
      <div class="chips">
        ${themeChip('dark', 'تیره')}${themeChip('light', 'روشن')}${themeChip('system', 'هماهنگ با سامانه')}
      </div>

      <div class="sheet__label">نمایش</div>
      <div class="switchrow">
        <div class="switchrow__text">رقم‌های فارسی<small>نمایش ۱۲۳ به‌جای 123</small></div>
        <button class="switch" role="switch" aria-checked="${state.digits}" data-action="toggle-digits"></button>
      </div>

      <div class="sheet__label">گاه‌شمار</div>
      <div class="about">
        گاه‌شمار ایرانی همان گاه‌شماری است که تاریخ رسمی ایران را نگه می‌دارد و ساختار کهن
        ایران‌زمین را بر آن می‌نشاند: دوازده ماه با نام‌های اوستایی، سی روزِ نام‌دار از اورمزد
        تا انارام (روز سی‌ویکم «اَندَرگاه»)، و پنج روز گاتها (پنجهٔ وهیک) از ۲۵ سپندارمذ.
        سال در لحظهٔ اعتدال بهاری به وقت تهران آغاز می‌شود.
        <small>همهٔ داده‌ها در خود برنامه‌اند؛ برنامه هیچ دسترسی اینترنتی ندارد.</small>
      </div>
    `);
  }

  /* ─── رویدادها ───────────────────────────────────────────────────── */
  function select(epoch) {
    state.selected = epoch;
    render();
  }

  document.addEventListener('click', (event) => {
    const target = event.target.closest('[data-action], [data-day], [data-month], [data-festival], [data-theme-id]');
    if (!target) return;
    const action = target.dataset.action;

    if (target.dataset.themeId) {
      state.theme = target.dataset.themeId;
      store.set('theme', state.theme);
      applyTheme();
      settingsSheet();
      render(true);
      return;
    }
    if (target.dataset.festival) { festivalSheet(target.dataset.festival); return; }
    if (target.dataset.month) {
      const d = date();
      state.selected = I.toEpochDay(d.year, +target.dataset.month, 1);
      render();
      return;
    }
    if (target.dataset.day) { select(+target.dataset.day); return; }

    switch (action) {
      case 'today': select(TODAY); break;
      case 'prev-day': select(state.selected - 1); break;
      case 'next-day': select(state.selected + 1); break;
      case 'prev-year': shiftYear(-1); break;
      case 'next-year': shiftYear(1); break;
      case 'settings': settingsSheet(); break;
      case 'toggle-digits':
        state.digits = !state.digits;
        store.set('digits', state.digits);
        settingsSheet();
        render(true);
        break;
      case 'close': closeSheet(); break;
      default: break;
    }
  });

  function shiftYear(delta) {
    const d = date();
    const year = d.year + delta;
    state.selected = d.panjehIndex
      ? I.panjehStart(year) + Math.min(d.panjehIndex, I.panjehLength(year)) - 1
      : I.toEpochDay(year, d.month, d.day);
    render();
  }

  document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && sheetOpen) closeSheet();
    if (sheetOpen) return;
    if (event.key === 'ArrowRight') select(state.selected - 1);
    if (event.key === 'ArrowLeft') select(state.selected + 1);
    if (event.key === 'ArrowUp') shiftYear(-1);
    if (event.key === 'ArrowDown') shiftYear(1);
  });

  /* ─── پوسته ──────────────────────────────────────────────────────── */
  function applyTheme() {
    let theme = state.theme;
    if (theme === 'system') {
      theme = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }
    document.documentElement.setAttribute('data-theme', theme);
    const meta = document.querySelector('meta[name="theme-color"]');
    if (meta) meta.setAttribute('content', theme === 'dark' ? '#0B1226' : '#F6EEDC');
  }

  if (window.matchMedia) {
    const mq = window.matchMedia('(prefers-color-scheme: dark)');
    const onChange = () => { if (state.theme === 'system') applyTheme(); };
    if (mq.addEventListener) mq.addEventListener('change', onChange);
    else if (mq.addListener) mq.addListener(onChange);
  }

  applyTheme();
  render();
})();
