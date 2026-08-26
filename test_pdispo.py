import requests, json
base = "http://127.0.0.1:8000/api"
lg = requests.post(base+"/auth/login", data={"username":"admin","password":"Admin123"}, timeout=20)
print("login", lg.status_code, lg.text[:300])
tok = lg.json()["access_token"]
h = {"Authorization": f"Bearer {tok}"}
for path in [
    "/personeros-disponibles",
    "/personeros-disponibles?tipo=MESA",
    "/personeros-disponibles?tipo=CENTRO",
    "/personeros-disponibles?provincia=MAYNAS&distrito=IQUITOS",
    "/personeros-disponibles?provincia=MAYNAS&distrito=IQUITOS&centroId=1",
    "/personero-centro/personeros-disponibles",
]:
    r = requests.get(base+path, headers=h, timeout=20)
    print(f"\n{path}: {r.status_code} len={len(r.text)}")
    if r.status_code == 200:
        d = r.json()
        print(f"  count={len(d) if isinstance(d,list) else 'notlist'}")
        if isinstance(d,list) and d:
            print("  first 2:", json.dumps(d[:2], ensure_ascii=False, indent=2)[:500])
    else:
        print("  body:", r.text[:800])
