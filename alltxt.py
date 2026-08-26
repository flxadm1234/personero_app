import re, sys
p = sys.argv[1]
xml = open(p, encoding="utf-8").read()
seen = set()
for m in re.finditer(r'text="([^"]+)"', xml):
    t = m.group(1).strip()
    if t and t not in seen:
        seen.add(t); print(repr(t)[:120])
