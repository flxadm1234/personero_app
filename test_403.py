import requests
base = "http://127.0.0.1:8000"
r = requests.post(base + "/api/auth/login", data={"username": "admin", "password": "Admin123"})
print("LOGIN STATUS:", r.status_code)
print("LOGIN BODY:", r.text[:500])
tok = r.json().get("access_token")
hdr = {"Authorization": f"Bearer {tok}"}
r2 = requests.get(base + "/api/admin/mesas?centroId=1&perPage=500", headers=hdr)
print("\nLISTAR MESAS ADMIN centroId=1:", r2.status_code)
print(r2.text[:800] if r2.status_code != 200 else f"OK, {len(r2.json())} mesas")
r3 = requests.get(base + "/api/centros-votacion", headers=hdr)
print("\nLISTAR CENTROS:", r3.status_code)
print(r3.text[:300] if r3.status_code != 200 else f"OK, {len(r3.json())} centros")
