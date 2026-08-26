import requests
base = "http://localhost:8001"
r = requests.post(base + "/api/auth/login", data={"username": "admin", "password": "Admin123"})
print("LOGIN:", r.status_code, r.text[:120])
tok = r.json().get("access_token")
hdr = {"Authorization": f"Bearer {tok}"}
payload = {
  "idMesa": 2,
  "id_mesa": 2,
  "idSolicitud": 3,
  "id_solicitud": 3,
  "idUsuario": None,
  "id_usuario": None,
  "idusuario": None,
  "idCentro": 1,
  "id_centro": 1,
  "observaciones": None
}
r2 = requests.post(base + "/api/admin/asignaciones/personero-mesa/asignar-mesa", json=payload, headers=hdr)
print("ASIGNAR:", r2.status_code, r2.text[:1500])
