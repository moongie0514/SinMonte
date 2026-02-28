# FastAPI actualizado (backend-first) — contrato base para Android

> Objetivo: que **FastAPI sea la fuente única de verdad** y Android solo se alinee después.

Este documento propone un contrato estable para los endpoints críticos (Admin/Empleado + Auth + Cliente clave), compatible con los problemas que detectaste en pruebas.

---

## 1) Reglas de contrato (obligatorias)

1. Todas las respuestas JSON deben incluir:
   - `status`: `"success" | "error"`
   - `message` opcional en éxito
   - `detail` en error (HTTP 4xx/5xx)
2. Fechas en ISO-8601.
3. Números monetarios como `number` (no string).
4. No cambiar nombres de campos sin versionar API.

---

## 2) Endpoints críticos y shape canónico

## 2.1 Auth

### `POST /registrar_cliente`
**Response 200**
```json
{
  "status": "success",
  "message": "Registro exitoso. Ya puedes iniciar sesión.",
  "requiere_verificacion": false,
  "id_cliente": 123
}
```

> Recomendación crítica: enviar correo en background task para evitar timeout.

### `POST /solicitar_codigo?email=...`
**Response 200**
```json
{
  "status": "success",
  "message": "Código enviado a usuario@mail.com"
}
```

> Igual: no bloquear la respuesta por SMTP.

---

## 2.2 Cliente

### `GET /cliente/{id_cliente}/prestamos`
**Response 200 (lista directa, estable)**
```json
[
  {
    "id_prestamo": 34,
    "folio": "MSP-34",
    "monto_total": 12000.0,
    "saldo_pendiente": 9500.0,
    "tasa_interes": 0.05,
    "plazo_meses": 12,
    "estado": "PENDIENTE",
    "fecha_creacion": "2026-02-25T21:10:00",
    "fecha_aprobacion": null,
    "pagos_realizados": 0,
    "total_pagos": 0
  }
]
```

### `GET /cliente/{id_cliente}/prestamos/{id_prestamo}/pagos`
**Response 200 (lista directa)**
```json
[
  {
    "id_pago": 1,
    "numero_pago": 1,
    "fecha_vencimiento": "2026-03-25",
    "monto": 1000.0,
    "estado": "pendiente",
    "fecha_pago": null
  }
]
```

### `POST /cliente/solicitar_credito`
**Response 200 mínima estable**
```json
{
  "status": "success",
  "message": "Solicitud enviada. Un empleado la revisará pronto."
}
```

---

## 2.3 Admin

### `GET /admin/estadisticas`
**Response 200**
```json
{
  "status": "success",
  "clientes": 40,
  "capital_activo": 250000.0,
  "recuperado": 150000.0,
  "morosos": 3,
  "pendientes": 5
}
```

### `POST /admin/aprobar_prestamo`
**Request**
```json
{
  "id_prestamo": 34,
  "accion": "aprobar",
  "id_empleado": 8
}
```
**Response 200**
```json
{ "status": "success", "message": "Préstamo aprobado. Se generaron 12 pagos." }
```

### `GET /configuracion_sistema`
**Response 200**
```json
{
  "status": "success",
  "configuracion": [
    { "id_config": 1, "clave": "tasa_interes", "valor": "0.05" },
    { "id_config": 2, "clave": "plazo_maximo", "valor": "48" },
    { "id_config": 3, "clave": "monto_minimo", "valor": "1000" },
    { "id_config": 4, "clave": "monto_maximo", "valor": "50000" }
  ]
}
```

### `PUT /configuracion_sistema/{id_config}`
**Request**
```json
{ "valor": "0.06" }
```
**Response 200**
```json
{ "status": "success", "message": "Configuración actualizada" }
```

---

## 2.4 Empleado

### `GET /empleado/pagos_pendientes`
**Response 200 (lista directa)**
```json
[
  {
    "id_pago": 91,
    "id_prestamo": 34,
    "numero_pago": 2,
    "fecha_vencimiento": "2026-03-15",
    "monto": 1540.22,
    "estado": "pendiente",
    "folio": "MSP-34",
    "nombre": "Juan",
    "apellido_paterno": "Pérez",
    "telefono": "8112345678"
  }
]
```

### `POST /empleado/registrar_pago`
**Request**
```json
{ "id_pago": 91, "id_empleado": 8 }
```
**Response 200**
```json
{
  "status": "success",
  "message": "Pago #2 registrado exitosamente",
  "monto": 1540.22,
  "id_prestamo": 34
}
```

### `GET /empleado/corte_caja?id_empleado=8&fecha=2026-02-27`
**Response 200**
```json
{
  "status": "success",
  "fecha": "2026-02-27",
  "total_pagos": 18,
  "total_cobrado": 25780.9,
  "movimientos": [
    {
      "id_ticket": 120,
      "folio": "T-91-1709057",
      "monto_pagado": 1540.22,
      "fecha_generacion": "2026-02-27T10:15:00",
      "metodo_pago": "EFECTIVO",
      "tipo": "PAGO",
      "folio_prestamo": "MSP-34",
      "nombre": "Juan",
      "apellido_paterno": "Pérez"
    }
  ]
}
```

### `GET /tickets/{folio}`
**Response 200**
```json
{
  "status": "success",
  "ticket": {
    "id_prestamo": 34,
    "folio": "MSP-34",
    "monto_total": 12000.0,
    "saldo_pendiente": 9500.0,
    "tasa_interes": 0.05,
    "plazo_meses": 12,
    "estado": "ACTIVO",
    "fecha_creacion": "2026-01-10T09:00:00",
    "fecha_aprobacion": "2026-01-11T10:00:00",
    "nombre": "Juan",
    "apellido_paterno": "Pérez",
    "apellido_materno": "García",
    "curp": "PEGJ...",
    "telefono": "8112345678",
    "email": "juan@mail.com",
    "pagos_realizados": 2,
    "total_pagos": 12,
    "pagos": []
  }
}
```

---

## 3) Implementación recomendada para evitar timeouts SMTP

Patrón sugerido:

```python
from fastapi import BackgroundTasks

@app.post("/registrar_cliente")
def registrar_cliente(request: RegistroClienteRequest, bg: BackgroundTasks):
    # 1) insertar usuario + commit
    # 2) responder rápido
    bg.add_task(email_bienvenida, request.email, request.nombre)
    return {"status": "success", "message": "Registro exitoso. Ya puedes iniciar sesión.", "requiere_verificacion": False, "id_cliente": id_cliente}
```

Y lo mismo para `solicitar_codigo` y `reenviar_codigo_verificacion`.

---

## 4) Criterios de “done” backend

- [ ] Los endpoints arriba devuelven exactamente estos campos/tipos.
- [ ] SMTP ya no bloquea respuesta HTTP.
- [ ] OpenAPI generado refleja contrato final.
- [ ] Smoke tests de contrato pasan (ver documento complementario).

