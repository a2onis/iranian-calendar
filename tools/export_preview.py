#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
استخراج محتوای برنامهٔ اندروید (Kotlin) و ساخت فایل داده برای پیش‌نمایش وب.
این ابزار متن کاتلین را «می‌خواند» تا داده‌های پیش‌نمایش، همان داده‌های واقعی برنامه باشند.
"""
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA = os.path.join(ROOT, "app/src/main/java/ir/gahshomar/parsi")
OUT = os.path.join(ROOT, "preview/data.js")


def read(name):
    with open(os.path.join(JAVA, name), encoding="utf-8") as f:
        return f.read()


def find_calls(src, name, require_newline=False):
    """یافتن همهٔ فراخوانی‌های name(...) با پرانتزهای متوازن."""
    results = []
    pattern = r'(?<![\w.])' + re.escape(name) + r'\s*\('
    for m in re.finditer(pattern, src):
        start = m.end() - 1
        depth = 0
        i = start
        in_str = False
        esc = False
        while i < len(src):
            ch = src[i]
            if in_str:
                if esc:
                    esc = False
                elif ch == '\\':
                    esc = True
                elif ch == '"':
                    in_str = False
            else:
                if ch == '"':
                    in_str = True
                elif ch == '(':
                    depth += 1
                elif ch == ')':
                    depth -= 1
                    if depth == 0:
                        break
            i += 1
        results.append(src[start + 1:i])
    return results


def split_top(text, sep=','):
    parts = []
    depth = 0
    in_str = False
    esc = False
    current = []
    i = 0
    while i < len(text):
        ch = text[i]
        if in_str:
            current.append(ch)
            if esc:
                esc = False
            elif ch == '\\':
                esc = True
            elif ch == '"':
                in_str = False
        else:
            if ch == '"':
                in_str = True
                current.append(ch)
            elif ch in '([{':
                depth += 1
                current.append(ch)
            elif ch in ')]}':
                depth -= 1
                current.append(ch)
            elif ch == sep and depth == 0:
                parts.append(''.join(current))
                current = []
            else:
                current.append(ch)
        i += 1
    if ''.join(current).strip():
        parts.append(''.join(current))
    return [p.strip() for p in parts if p.strip()]


def split_concat(text):
    parts = []
    depth = 0
    in_str = False
    esc = False
    current = []
    for ch in text:
        if in_str:
            current.append(ch)
            if esc:
                esc = False
            elif ch == '\\':
                esc = True
            elif ch == '"':
                in_str = False
        else:
            if ch == '"':
                in_str = True
                current.append(ch)
            elif ch in '([{':
                depth += 1
                current.append(ch)
            elif ch in ')]}':
                depth -= 1
                current.append(ch)
            elif ch == '+' and depth == 0:
                parts.append(''.join(current))
                current = []
            else:
                current.append(ch)
    parts.append(''.join(current))
    return parts


def unescape(s):
    return (s.replace('\\n', '\n').replace('\\t', '\t')
            .replace('\\"', '"').replace('\\\\', '\\'))


def eval_string(expr):
    expr = expr.strip()
    if expr == 'null':
        return None
    parts = split_concat(expr)
    out = []
    for p in parts:
        p = p.strip()
        if len(p) >= 2 and p.startswith('"') and p.endswith('"'):
            out.append(unescape(p[1:-1]))
        else:
            return None
    return ''.join(out)


def eval_value(expr):
    expr = expr.strip()
    s = eval_string(expr)
    if s is not None:
        return s
    if re.fullmatch(r'-?\d+', expr):
        return int(expr)
    if re.fullmatch(r'-?\d+L', expr):
        return int(expr[:-1])
    if expr.startswith('listOf(') and expr.endswith(')'):
        inner = expr[len('listOf('):-1]
        items = []
        for part in split_top(inner):
            if ' to ' in part:
                left, right = part.split(' to ', 1)
                items.append([eval_value(left), eval_value(right)])
            else:
                items.append(eval_value(part))
        return items
    if re.fullmatch(r'[A-Za-z_][\w.]*', expr):
        if '.' in expr:
            return expr.split('.')[-1]
        return expr
    m = re.fullmatch(r'DateRule\.(\w+)\((.*)\)', expr, re.S)
    if m:
        kind, args = m.group(1), split_top(m.group(2))
        return {'type': kind, 'args': [eval_value(a) for a in args]}
    return None


def parse_args(argtext, positional_names):
    """تبدیل متن آرگومان‌ها به دیکشنری (پشتیبانی از آرگومان‌های نام‌دار)."""
    result = {}
    position = 0
    for part in split_top(argtext):
        m = re.match(r'^([A-Za-z_]\w*)\s*=\s*(.*)$', part, re.S)
        if m:
            result[m.group(1)] = eval_value(m.group(2))
        else:
            if position < len(positional_names):
                result[positional_names[position]] = eval_value(part)
            position += 1
    return result


def parse_events():
    src = read('data/EventData.kt')
    events = []
    for name, mapping in (
        ('solar', ['id', 'title', 'month', 'day', 'category', 'observance', 'summary', 'detail', 'articleId']),
        ('western', ['id', 'title', 'month', 'day', 'category', 'summary', 'detail']),
        ('moment', ['id', 'title', 'quarter', 'summary', 'detail']),
        ('Event', ['id', 'title', 'rule', 'category', 'observance', 'summary', 'detail', 'articleId']),
    ):
        for argtext in find_calls(src, name):
            args = parse_args(argtext, mapping)
            if 'rule' in args and isinstance(args.get('rule'), dict) and args['rule'].get('type') == 'Solar':
                rule = {'type': 'solar', 'month': args['rule']['args'][0], 'day': args['rule']['args'][1]}
            elif 'rule' in args and isinstance(args.get('rule'), dict) and args['rule'].get('type') == 'Western':
                rule = {'type': 'western', 'month': args['rule']['args'][0], 'day': args['rule']['args'][1]}
            elif 'rule' in args and isinstance(args.get('rule'), dict) and args['rule'].get('type') == 'SolarMomentRule':
                rule = {'type': 'moment', 'quarter': args['rule']['args'][0]}
            elif 'rule' in args and args.get('rule') == 'ChaharshanbeSuri':
                rule = {'type': 'chaharshanbe'}
            elif 'month' in args and 'day' in args:
                rule = {'type': 'solar' if name == 'solar' else 'western',
                        'month': args['month'], 'day': args['day']}
            elif 'quarter' in args:
                rule = {'type': 'moment', 'quarter': args['quarter']}
            else:
                continue
            if not args.get('id'):
                continue
            # رد کردن بقایای تجزیه (مثل امضای توابع یا سرآیند کلاس)
            if not re.fullmatch(r'[a-z0-9-]{2,}', str(args.get('id'))):
                continue
            if args.get('title') in (None, '', 'title'):
                continue
            if name == 'moment' and args.get('month') is None:
                args['category'] = 'NATURE'
            events.append({
                'id': args.get('id'),
                'title': args.get('title'),
                'rule': rule,
                'category': args.get('category'),
                'observance': args.get('observance') or 'NONE',
                'summary': args.get('summary') or '',
                'detail': args.get('detail') or '',
                'articleId': args.get('articleId'),
            })
    # حذف تکرارها بر پایهٔ شناسه
    seen = {}
    for event in events:
        seen.setdefault(event['id'], event)
    return list(seen.values())


def parse_articles():
    files = ['data/ArticlesNowruz.kt', 'data/ArticlesZoroastrian.kt',
             'data/ArticlesAncient.kt', 'data/ArticlesCulture.kt']
    articles = []
    for file in files:
        src = read(file)
        for argtext in find_calls(src, 'Article'):
            args = parse_args(argtext, [])
            if not args.get('id'):
                continue
            articles.append({
                'id': args.get('id'),
                'title': args.get('title') or '',
                'subtitle': args.get('subtitle') or '',
                'category': args.get('category') or 'CULTURE',
                'paragraphs': [p for p in (args.get('paragraphs') or []) if isinstance(p, str)],
                'bullets': args.get('bullets') or [],
                'quote': args.get('quote'),
                'quoteSource': args.get('quoteSource'),
            })
    return articles


def parse_timeline():
    src = read('data/Timeline.kt')
    entries = []
    for argtext in find_calls(src, 'TimelineEntry'):
        args = parse_args(argtext, ['year', 'title', 'text', 'era'])
        if args.get('year'):
            entries.append({
                'year': args.get('year'),
                'title': args.get('title') or '',
                'text': args.get('text') or '',
                'era': args.get('era') or 'MYTHIC',
            })
    return entries


def parse_quotes():
    src = read('data/Quotes.kt')
    quotes = []
    for argtext in find_calls(src, 'Quote'):
        args = parse_args(argtext, ['text', 'source', 'category'])
        if args.get('text'):
            quotes.append(args)
    return quotes


def parse_arrays():
    source = read('core/PersianCalendar.kt')
    other = read('core/OtherCalendars.kt')

    def array_from(text, name):
        m = re.search(r'val ' + name + r'\s*=\s*arrayOf\((.*?)\)\n', text, re.S)
        if not m:
            m = re.search(r'val ' + name + r'\s*=\s*arrayOf\(([\s\S]*?)\)\s*\n', text)
        if not m:
            return []
        return [eval_string(x) for x in split_top(m.group(1))]

    def list_strings(text, name):
        m = re.search(r'val ' + name + r'\s*=\s*listOf\(([\s\S]*?)\)\s*\n\n', text)
        if not m:
            return []
        return [eval_string(x) for x in split_top(m.group(1))]

    zoro_section = other[other.find('object ZoroastrianCalendar'):]
    data = {
        'months': array_from(source, 'MONTH_NAMES'),
        'avestanMonths': array_from(source, 'MONTH_NAMES_AVESTAN'),
        'weekdays': array_from(source, 'WEEKDAY_NAMES'),
        'weekdayShort': array_from(source, 'WEEKDAY_NAMES_SHORT'),
        'zodiac': array_from(source, 'ZODIAC_NAMES'),
        'zodiacSigns': array_from(source, 'ZODIAC_SIGNS'),
        'dayNames': array_from(other, 'DAY_NAMES'),
        'dayMeanings': array_from(other, 'DAY_MEANINGS'),
        'zoroastrianMonths': array_from(zoro_section, 'MONTH_NAMES'),
        'gahs': array_from(other, 'GAHS'),
        'gregorianMonths': array_from(read('core/PersianText.kt'), 'GREGORIAN_MONTHS'),
        'hijriMonths': array_from(other[other.find('object HijriCalendar'):other.find('object ZoroastrianCalendar')], 'MONTH_NAMES'),
    }
    festivals = []
    for argtext in find_calls(other, 'FestivalNameDay'):
        args = parse_args(argtext, ['name', 'month', 'day', 'meaning'])
        if args.get('name'):
            festivals.append(args)
    gahambars = []
    for argtext in find_calls(other, 'Gahambar'):
        args = parse_args(argtext, ['name', 'seasonTime', 'creation'])
        if args.get('name'):
            gahambars.append(args)
    data['festivals'] = festivals
    data['gahambars'] = gahambars
    return data


def main():
    events = parse_events()
    articles = parse_articles()
    timeline = parse_timeline()
    quotes = parse_quotes()
    extra = parse_arrays()

    data = {
        'events': events,
        'articles': articles,
        'timeline': timeline,
        'quotes': quotes,
    }
    data.update(extra)

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, 'w', encoding='utf-8') as f:
        f.write('// این فایل به‌طور خودکار از منابع کاتلین برنامه ساخته می‌شود.\n')
        f.write('// tools/export_preview.py\n')
        f.write('window.GAH_DATA = ')
        json.dump(data, f, ensure_ascii=False, indent=1)
        f.write(';\n')

    print('events:', len(events))
    print('articles:', len(articles))
    print('timeline:', len(timeline))
    print('quotes:', len(quotes))
    missing = [k for k, v in data.items() if isinstance(v, list) and not v]
    print('empty:', missing)
    print('written to', OUT)


if __name__ == '__main__':
    sys.exit(main())
