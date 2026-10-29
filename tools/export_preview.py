#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
استخراج دادهٔ گاه‌شمار ایرانی از کد کاتلین و ساخت فایل دادهٔ پیش‌نمایش وب.
این ابزار متن کاتلین را «می‌خواند» تا دادهٔ پیش‌نمایش، همان دادهٔ واقعی برنامه باشد.
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


def find_calls(src, name):
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
            elif ch == sep and depth == 0:
                parts.append(''.join(current))
                current = []
            else:
                current.append(ch)
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
    out = []
    for p in split_concat(expr):
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
    if expr.startswith('arrayOf(') and expr.endswith(')'):
        return [eval_value(x) for x in split_top(expr[len('arrayOf('):-1])]
    if expr.startswith('listOf(') and expr.endswith(')'):
        return [eval_value(x) for x in split_top(expr[len('listOf('):-1])]
    m = re.fullmatch(r'([A-Za-z_][\w.]*)\(([\s\S]*)\)', expr)
    if m:
        return {'call': m.group(1), 'args': [eval_value(a) for a in split_top(m.group(2))]}
    if re.fullmatch(r'[A-Za-z_][\w.]*', expr):
        return expr.split('.')[-1]
    return None


def parse_args(argtext, positional_names):
    result = {}
    position = 0
    for part in split_top(argtext):
        m = re.match(r'^([A-Za-z_]\w*)\s*=\s*([\s\S]*)$', part)
        if m:
            result[m.group(1)] = eval_value(m.group(2))
        else:
            if position < len(positional_names):
                result[positional_names[position]] = eval_value(part)
            position += 1
    return result


def array_from(text, name):
    """خواندن آرایه‌ای از رشته‌ها مثل val NAME = arrayOf(...)"""
    m = re.search(r'val ' + name + r'\s*=\s*arrayOf\(([\s\S]*?)\)\s*\n', text)
    if not m:
        return []
    return [eval_string(x) for x in split_top(m.group(1))]


def parse_festivals():
    """۲۶ جشن و آیین از data/Festivals.kt"""
    src = read('data/Festivals.kt')
    festivals = []
    for argtext in find_calls(src, 'Festival'):
        args = parse_args(argtext, ['id', 'name', 'rule', 'kind', 'summary', 'detail'])
        festival_id = args.get('id')
        if not festival_id or not re.fullmatch(r'[a-z0-9-]{2,}', str(festival_id)):
            continue
        rule = args.get('rule')
        if isinstance(rule, dict):
            call = rule.get('call', '')
            rule_args = rule.get('args') or []
            if call.endswith('MonthDay'):
                parsed_rule = {'type': 'monthDay', 'month': rule_args[0], 'day': rule_args[1]}
            elif call.endswith('Panjeh'):
                parsed_rule = {'type': 'panjeh'}
            elif call.endswith('LastWednesday'):
                parsed_rule = {'type': 'lastWednesday'}
            elif call.endswith('WinterSolstice'):
                parsed_rule = {'type': 'winterSolstice'}
            else:
                continue
        elif isinstance(rule, str):
            if rule.endswith('Panjeh'):
                parsed_rule = {'type': 'panjeh'}
            elif rule.endswith('LastWednesday'):
                parsed_rule = {'type': 'lastWednesday'}
            elif rule.endswith('WinterSolstice'):
                parsed_rule = {'type': 'winterSolstice'}
            else:
                continue
        else:
            continue
        festivals.append({
            'id': festival_id,
            'name': args.get('name') or '',
            'rule': parsed_rule,
            'kind': args.get('kind') or 'JASHN',
            'summary': args.get('summary') or '',
            'detail': args.get('detail') or '',
        })
    seen = set()
    unique = []
    for festival in festivals:
        if festival['id'] in seen:
            continue
        seen.add(festival['id'])
        unique.append(festival)
    return unique


def parse_calendar_data():
    source = read('core/IranianCalendar.kt')
    weekdays = read('core/PersianCalendar.kt')
    return {
        'months': array_from(source, 'MONTH_NAMES'),
        'dayNames': array_from(source, 'DAY_NAMES'),
        'dayMeanings': array_from(source, 'DAY_MEANINGS'),
        'gathas': array_from(source, 'GATHA_NAMES'),
        'signs': array_from(source, 'MONTH_SIGNS'),
        'weekdays': array_from(weekdays, 'WEEKDAY_NAMES'),
        'weekdayShort': array_from(weekdays, 'WEEKDAY_NAMES_SHORT'),
    }


def main():
    festivals = parse_festivals()
    data = parse_calendar_data()
    data['festivals'] = festivals

    if not data['months'] or not data['dayNames'] or not festivals:
        print('خطا: داده‌ای استخراج نشد.')
        return 1

    kinds = {}
    for festival in festivals:
        kinds[festival['kind']] = kinds.get(festival['kind'], 0) + 1

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, 'w', encoding='utf-8') as f:
        f.write('// این فایل به‌طور خودکار از منابع کاتلین برنامه ساخته می‌شود.\n')
        f.write('// tools/export_preview.py\n')
        f.write('window.GAH_DATA = ')
        json.dump(data, f, ensure_ascii=False, indent=1)
        f.write(';\n')

    print('festivals:', len(festivals), kinds)
    print('months:', len(data['months']), 'days:', len(data['dayNames']))
    print('written to', OUT)
    return 0


if __name__ == '__main__':
    sys.exit(main())
