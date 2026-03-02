# Implementación de `GET /admin/folios` en FastAPI (guía puntual)

> Objetivo: agregar el endpoint que Android ya consume desde `ApiService.obtenerFoliosAdmin(...)` y devolver exactamente la estructura esperada por `CorteCajaResponse`.

---

## 1) Dónde insertarlo

En `backend/main.py`, agrégalo **después** de `@app.post("/admin/aprobar_prestamo")` y **antes** de `@app.get("/admin/estadisticas")`.

Referencia rápida de ubicación actual:
- `procesar_prestamo` inicia en `backend/main.py` cerca de la línea 265.
- `obtener_estadisticas` inicia en `backend/main.py` cerca de la línea 328.

---

## 2) Bloque exacto a agregar

```python
@app.get("/admin/folios")
def obtener_folios_admin(fecha: Optional[str] = Query(None)):
    """
    Respuesta esperada por Android (CorteCajaResponse):
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
              AND t.estado = 'ACTIVO'
            ORDER BY t.fecha_generacion DESC
            """,
            (fecha_filtro,),
        )

        movimientos = cursor.fetchall()

        for m in movimientos:
            if m.get("fecha_generacion") and hasattr(m["fecha_generacion"], "isoformat"):
                m["fecha_generacion"] = m["fecha_generacion"].isoformat()
            m["monto_pagado"] = float(m.get("monto_pagado") or 0)

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

## 3) Dependencias / imports

Este endpoint usa:
- `Optional` (typing)
- `Query`, `HTTPException` (fastapi)
- `datetime` (datetime)

En tu `main.py` actual ya existen estos imports, por lo que no necesitas agregar nada extra si no los removiste.

---

## 4) Contrato que alinea con Android

`ApiService.kt` consume:
- `@GET("admin/folios")`
- `Response<CorteCajaResponse>`

`CorteCajaResponse` espera:
- `status`
- `fecha`
- `total_pagos`
- `total_cobrado`
- `movimientos[]` con `id_ticket`, `folio`, `monto_pagado`, `fecha_generacion`, `metodo_pago`, `tipo`, `folio_prestamo`, `nombre`, `apellido_paterno`.

El bloque propuesto cumple ese contrato 1:1.

---

## 5) Verificación manual rápida (después de deploy)

```bash
curl -i "https://<tu-api-railway>/admin/folios"
curl -i "https://<tu-api-railway>/admin/folios?fecha=2026-03-02"
```

Esperado:
- HTTP `200`
- JSON con `status=success`
- `movimientos` (vacío o con datos), pero **sin 404**.

---

## 6) Nota importante de despliegue

Si en Android sigue apareciendo `404 Not Found` tras agregar el endpoint, el problema ya no es código: es que Railway está sirviendo una versión anterior. Debes redeployar el servicio correcto y validar la URL/base path activa.
