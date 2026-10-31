#!/usr/bin/env node
/**
 * نگهبان سبک کاتلین: توازن آکولاد و پرانتز، رشته‌های ناتمام و پرانتزهای اضافی.
 * استفاده: node tools/ktcheck.cjs app/src/main app/src/test
 */
const fs = require('fs'), path = require('path');
const files = [];
function walk(dir) {
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) walk(p);
    else if (e.name.endsWith('.kt')) files.push(p);
  }
}
for (const a of process.argv.slice(2)) walk(a);
const problems = [];
for (const f of files) {
  const src = fs.readFileSync(f, 'utf8');
  let i = 0, line = 1, inStr = false, inLine = false, inBlock = false;
  const depth = { '{': 0, '(': 0, '[': 0 };
  while (i < src.length) {
    const c = src[i], n = src[i + 1];
    if (c === '\n') { line++; if (inLine) inLine = false; i++; continue; }
    if (inLine) { i++; continue; }
    if (inBlock) { if (c === '*' && n === '/') { inBlock = false; i += 2; continue; } i++; continue; }
    if (inStr) {
      if (c === '\\') { i += 2; continue; }
      if (c === '"') inStr = false;
      i++; continue;
    }
    if (c === '/' && n === '/') { inLine = true; i += 2; continue; }
    if (c === '/' && n === '*') { inBlock = true; i += 2; continue; }
    if (c === '"') { inStr = true; i++; continue; }
    if (c === "'") { i++; while (i < src.length && src[i] !== "'") { if (src[i] === '\\') i++; i++; } i++; continue; }
    const open = { '}': '{', ')': '(', ']': '[' }[c];
    if ('{(['.includes(c)) depth[c]++;
    if (open) {
      depth[open]--;
      if (depth[open] < 0) { problems.push(`${f}:${line} extra ${c}`); break; }
    }
    i++;
  }
  if (depth['{'] || depth['('] || depth['[']) problems.push(`${f} unbalanced {${depth['{']} (${depth['(']} [${depth['[']}`);
  if (inStr || inBlock) problems.push(`${f} unterminated literal`);
}
console.log('\nfiles:', files.length, ' problems:', problems.length);
problems.forEach(p => console.log(' -', p));
process.exit(problems.length ? 1 : 0);
