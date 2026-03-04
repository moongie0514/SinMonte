# Implementación de `GET /admin/folios` (alineada a columnas reales de Railway)

> Objetivo: crear el endpoint en FastAPI usando **solo columnas existentes** en tus tablas de Railway y manteniendo el contrato que Android consume.

---

## 1) Verificación previa (muy importante)

Con el esquema que compartiste, para este endpoint sí existen estas columnas clave:

- `tickets_pagos`: `id_ticket`, `folio`, `id_pago`, `metodo_pago`, `monto_pagado`, `fecha_generacion`, `estado`, `tipo`.
- `pagos`: `id_pago`, `id_prestamo`.
- `prestamos`: `id_prestamo`.
- `usuarios`: `id_usuario`, `nombre`, `apellido_paterno`.

✅ Con esas columnas, el endpoint **sí se puede implementar sin inventar campos**.

⚠️ Ajuste importante respecto al MD anterior:
- en tu tabla `tickets_pagos.estado` los valores son `'activo' | 'cancelado'` (minúsculas), por lo que filtrar con `t.estado = 'ACTIVO'` puede fallar.
- recomendación robusta: `LOWER(t.estado) = 'activo'`.

---

## 2) Dónde insertarlo

En `backend/main.py`, inserta el endpoint entre:
- `@app.post("/admin/aprobar_prestamo")`
- `@app.get("/admin/estadisticas")`

---

## 3) Bloque exacto a agregar (versión recomendada)

```python
@app.get("/admin/folios")
def obtener_folios_admin(fecha: Optional[str] = Query(None)):
    """
    Respuesta compatible con Android (CorteCajaResponse):
    {
      "status": "success",
      "fecha": "YYYY-MM-DD",
      "total_pagos": int,
      "total_cobrado": float,
      "movimientos": [
        {
          "id_ticket": int,
          "folio": str,
          "monto_pagado": float,
          "fecha_generacion": "ISO-8601",
          "metodo_pago": str,
          "tipo": str,
          "folio_prestamo": "MSP-{id}",
          "nombre": str,
          "apellido_paterno": str
        }
      ]
    }
    """
    db = conectar()
    cursor = db.cursor(dictionary=True)
    try:
        fecha_filtro = fecha if fecha else datetime.now().strftime("%Y-%m-%d")

        cursor.execute(
            """
            SELECT t.id_ticket,
                   t.folio,
                   t.monto_pagado,
                   t.fecha_generacion,
                   t.metodo_pago,
                   t.tipo,
                   CONCAT('MSP-', p.id_prestamo) AS folio_prestamo,
                   u.nombre,
                   u.apellido_paterno
            FROM tickets_pagos t
            JOIN pagos g ON t.id_pago = g.id_pago
            JOIN prestamos p ON g.id_prestamo = p.id_prestamo
            JOIN usuarios u ON p.id_cliente = u.id_usuario
            WHERE DATE(t.fecha_generacion) = %s
              AND LOWER(t.estado) = 'activo'
            ORDER BY t.fecha_generacion DESC
            """,
            (fecha_filtro,),
        )

        movimientos = cursor.fetchall()

        for m in movimientos:
            if m.get("fecha_generacion") and hasattr(m["fecha_generacion"], "isoformat"):
                m["fecha_generacion"] = m["fecha_generacion"].isoformat()
            m["monto_pagado"] = float(m.get("monto_pagado") or 0)
            m["metodo_pago"] = (m.get("metodo_pago") or "").upper()
            m["tipo"] = (m.get("tipo") or "pago").upper()

        total = float(sum(m["monto_pagado"] for m in movimientos))

        return {
            "status": "success",
            "fecha": fecha_filtro,
            "total_pagos": len(movimientos),
            "total_cobrado": total,
            "movimientos": movimientos,
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        cursor.close()
        db.close()
```

---

## 4) Por qué este SQL sí es seguro con tu BD

- No usa `cancelado_por`, `motivo_cancelacion`, `fecha_cancelacion` ni otras columnas opcionales.
- No usa columnas inexistentes en `prestamos`/`pagos` para este caso.
- Toma `monto_pagado` desde `tickets_pagos` (correcto para libro de folios).
- Filtra por `estado` real de `tickets_pagos` (`activo`).

---

## 5) Imports requeridos (si faltan)

```python
from typing import Optional
from fastapi import Query, HTTPException
from datetime import datetime
```

---

## 6) Validación post-deploy

```bash
curl -i "https://<tu-api-railway>/admin/folios"
curl -i "https://<tu-api-railway>/admin/folios?fecha=2026-03-02"
```

Esperado:
- HTTP `200`
- `status = success`
- `movimientos` (vacío o con datos), pero ya **sin 404**.

---

## 7) Nota de operación

Si sigue apareciendo 404 tras agregar el endpoint, es despliegue desfasado en Railway (no código).
Verifica que el servicio activo apunte al commit donde agregaste `/admin/folios`.
