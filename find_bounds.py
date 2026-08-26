import re, sys
p = sys.argv[1]
xml = open(p, encoding="utf-8").read()
# find "Accesos Rápidos"
for m in re.finditer(r'text="([^"]*)".*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml):
    txt = m.group(1)
    if "Asignar" in txt or "Mesas" in txt or "Accesos" in txt or "Cobertura" in txt or "Solicitudes Pend" in txt:
        x1,y1,x2,y2 = int(m.group(2)), int(m.group(3)), int(m.group(4)), int(m.group(5))
        print(f"TEXT {txt!r} bounds [{x1},{y1}][{x2},{y2}] center {round((x1+x2)/2)},{round((y1+y2)/2)}")
