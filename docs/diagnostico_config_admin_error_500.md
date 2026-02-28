# Diagnóstico técnico — Error `500` en módulo **Admin > Configuración**

## Objetivo
Identificar, **antes de cambiar código Android**, la causa más probable del error `500` que aparece al abrir la pantalla de configuración.

---

## 1) Evidencia del contrato Android actual

### 1.1 Endpoint que consume Android
Android llama directamente a:
- `GET /configuracion_sistema`
- `PUT /configuracion_sistema/{id_config}`

Referencia: `ApiService`.【F:app/src/main/java/com/moon/casaprestamo/data/network/ApiService.kt†L103-L112】

### 1.2 Forma JSON que espera Android al cargar configuración
Android espera un payload tipo:

```json
{
  "status": "success",
  "configuracion": [
    {
      "id_config": 1,
      "tasa_interes": 0.05,
      "plazo_maximo": 48,
      "monto_minimo": 1000,
      "monto_maximo": 50000,
      "fecha_actualizacion": "..."
    }
  ]
}
```

Esto se ve en `ConfiguracionResponse` + `ConfiguracionItem`.【F:app/src/main/java/com/moon/casaprestamo/data/models/Models.kt†L248-L260】

### 1.3 Lógica del ViewModel al entrar a la pantalla
- Hace `obtenerConfiguracion()`.
- Si HTTP no es exitoso, muestra literalmente `Error: <code>`.

Referencia: `ConfigAdminViewModel`.【F:app/src/main/java/com/moon/casaprestamo/presentation/admin/configuracion/ConfiguracionViewModel.kt†L30-L70】

**Conclusión:** el mensaje `Error: 500` que ves en UI confirma que el backend está devolviendo 500 en el GET, no que Compose esté fallando localmente.

---

## 2) Hallazgo crítico en el backend del repo (estado actual)

En el `backend/main.py` de este repo:
- existe un `ConfiguracionRequest` antiguo con campo `valor: str`.【F:backend/main.py†L94-L95】
- **no existen** endpoints `@app.get("/configuracion_sistema")` ni `@app.put("/configuracion_sistema/{id_config}")` en este archivo.

Referencia de que el archivo termina sin dichos endpoints (últimos endpoints son estadísticas y pagos pendientes).【F:backend/main.py†L328-L414】

### Implicación
El backend del repo y el backend que dices tener desplegado/actualizado **no están sincronizados**. Eso por sí solo explica comportamientos inconsistentes entre pruebas.

---

## 3) Causas probables del `500` en TU FastAPI actualizado

Tomando tu FastAPI actualizado (el que compartiste en el chat), el `GET /configuracion_sistema` hace:
- `SELECT * FROM configuracion_sistema`
- casteos con `float()`/`int()` sobre columnas `VARCHAR`

Cuando hay `500` aquí, lo más frecuente es:

### Causa A — Datos no numéricos en columnas VARCHAR
Si en BD hay valores como:
- `"5%"`, `"$1,000"`, `"1,000"`, `"N/A"`, `""`

entonces `float(...)` o `int(...)` lanzan excepción y FastAPI responde 500.

### Causa B — Nombre real de columna distinto al esperado
En tu descripción de esquema aparece `Id` (mayúscula/otro nombre) y en el código se usa `id_config` para update.
Si la columna no existe con ese nombre exacto, puede fallar (sobre todo en PUT).

### Causa C — Filas vacías o nulls no contemplados
Si una columna viene `NULL` y la conversión no está defendida correctamente en todas las ramas, también termina en 500.

---

## 4) Verificación rápida (backend) para confirmar causa en minutos

> Ejecutar contra la misma BD que usa Railway/producción.

### 4.1 Confirmar estructura real
```sql
SHOW COLUMNS FROM configuracion_sistema;
```

### 4.2 Ver datos crudos actuales
```sql
SELECT * FROM configuracion_sistema;
```

### 4.3 Detectar valores no parseables
```sql
SELECT id_config, tasa_interes, monto_minimo, monto_maximo, plazo_maximo
FROM configuracion_sistema
WHERE tasa_interes IS NULL
   OR monto_minimo IS NULL
   OR monto_maximo IS NULL
   OR plazo_maximo IS NULL
   OR tasa_interes REGEXP '[^0-9.]'
   OR monto_minimo REGEXP '[^0-9.]'
   OR monto_maximo REGEXP '[^0-9.]'
   OR plazo_maximo REGEXP '[^0-9]';
```

### 4.4 Probar endpoint en vivo
```bash
curl -i https://<tu-api>/configuracion_sistema
```

Si devuelve 500, revisar `detail` y logs del contenedor.

---

## 5) Solución recomendada (backend-first)

1. **Normalizar datos** en `configuracion_sistema` para que sean estrictamente numéricos.
2. En FastAPI, agregar parseo defensivo (`safe_float`, `safe_int`) para evitar 500 por suciedad histórica.
3. Alinear nombre de PK (`id_config` vs `Id`) entre SQL y código.
4. Versionar contrato: mantener **solo una** forma de configuración (row-based) y no mezclar con esquema viejo `clave/valor`.

---

## 6) Estado Android respecto a este problema

El cliente Android ya está alineado para esquema row-based en:
- modelo de respuesta de configuración.【F:app/src/main/java/com/moon/casaprestamo/data/models/Models.kt†L248-L260】
- consumo en ViewModel de configuración.【F:app/src/main/java/com/moon/casaprestamo/presentation/admin/configuracion/ConfiguracionViewModel.kt†L36-L53】

Por tanto, **el 500 actual se debe resolver primero en FastAPI/BD**.

---

## 7) Próximo paso sugerido

Una vez que confirmes y corrijas backend/BD (puntos 4 y 5), avanzamos con la implementación completa por rol empezando por Admin Configuración (UI/validaciones/preview), con pruebas end-to-end.
