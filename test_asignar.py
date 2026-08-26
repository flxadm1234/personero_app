import requests, json, sys
sys.path.insert(0, r'f:\SISTEMA DE PERSONERO\electoral-personeros-app\backend')

B = 'http://localhost:8000'
r = requests.post(f'{B}/api/auth/login', data={'username':'admin','password':'Admin123'})
tk = r.json()['access_token']
h = {'Authorization': f'Bearer {tk}'}

print("=== TEST: asignar-mesa SOLO id_solicitud=2 (Lucia, sin Usuario registrado) ===")
b2 = dict(idMesa=1, id_mesa=1, idSolicitud=2, id_solicitud=2, idCentro=1, id_centro=1)
# NO ENVIO idusuario (solo envio idSolicitud/id_solicitud) -> debe funcionar SOLO con eso!
print("Body =", json.dumps(b2))
resp = requests.post(f'{B}/api/admin/asignaciones/personero-mesa/asignar-mesa', json=b2, headers=h)
print(f'STATUS {resp.status_code}')
p = resp.json()
print('  RESP =', json.dumps(p, indent=2))

print()
print('=== Consulta final: Mesa 1 en admin/mesas centroId=1 ===')
r2 = requests.get(f'{B}/api/admin/mesas', params={'centroId':1,'perPage':3}, headers=h)
for row in r2.json():
    if row['id_mesa']==1:
        print("  mesa1 num=%s estado=%s" % (row['num_mesa'], row['estado']))
        print("  personero =", json.dumps(row['personero'], indent=4, ensure_ascii=False))
