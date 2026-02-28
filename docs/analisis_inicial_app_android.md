# Diagnóstico técnico de fallos reportados (Android + FastAPI actual)

Este documento se centra en **los errores reales que reportaste en pruebas** y su causa más probable con base en el código Android actual y la versión de FastAPI que compartiste.

---

## 1) Registro se queda cargando y termina en timeout (pero sí se guarda en Railway)

## Síntoma
- Al presionar **Crear mi cuenta**, la app tarda mucho y luego muestra timeout.
- Aun así, el registro sí quedó persistido en DB.

## Causa probable (alta)
### Bloqueo por envío de correo dentro de la misma petición HTTP
En `POST /registrar_cliente`, el backend hace `commit` del usuario y luego intenta enviar email de bienvenida por SMTP antes de terminar la respuesta HTTP. Si Gmail responde lento o falla, la app puede llegar al timeout del cliente (30s) aunque el registro ya quedó guardado.  

En Android, Retrofit tiene timeout de 30s en connect/read/write. Si el backend tarda más por SMTP, el cliente lanza excepción de red/timeout.

## Evidencia de código
- Android timeout de 30s. (`RetrofitClient.kt`)
- Registro en Android trata cualquier excepción como `Error de red: ...`. (`RegistroViewModel.kt`)
- Backend: primero inserta y `commit`, luego ejecuta `email_bienvenida(...)` antes de retornar. (FastAPI compartido por ti)

## Qué validar en logs
- Latencia de `/registrar_cliente` > 30s.
- En Railway: inserción exitosa + error o lentitud de SMTP en la misma solicitud.

---

## 2) Recuperar contraseña: “Enviar código” también timeout; código aparece en DB pero no llega correo

## Síntoma
- `Enviar código` tarda, da timeout.
- Código de recuperación sí aparece en BD.
- Correo no llega.

## Causas probables
1. **Misma raíz de timeout SMTP**: endpoint genera código y luego intenta mandar correo en la misma petición.
2. **Credenciales de Gmail App Password inválidas o bloqueadas**:
   - En el código, `GMAIL_PASS` por defecto trae espacios y luego se aplica `replace(" ", "")`.
   - Si la contraseña en variable de entorno no coincide con un App Password válido o Gmail rechaza inicio de sesión, el correo no se envía.
3. **Rollback inconsistente**:
   - En `/solicitar_codigo`, haces `UPDATE ...` + `commit` antes del envío.
   - Si enviar correo falla, llamas `db.rollback()`, pero ese rollback ya no revierte el commit previo.
   - Resultado: código queda guardado aunque correo no salga.

## Evidencia de código
- Android: `ForgotPasswordViewModel` interpreta timeout/excepción como “Sin conexión...”.
- Backend: `POST /solicitar_codigo` actualiza código y hace `db.commit()` antes del envío SMTP.

---

## 3) Como admin no se visualizan solicitudes/préstamos; todo en 0

Aquí hay **dos incompatibilidades de contrato** críticas entre Android y esta versión de FastAPI.

### 3.1 Estadísticas admin: nombres JSON no coinciden
Android espera:
- `total_clientes`, `prestamos_activos`, `capital_otorgado`, `saldo_pendiente`, `monto_recuperado`

FastAPI devuelve:
- `clientes`, `capital_activo`, `recuperado`, `morosos`, `pendientes`

Cuando Gson no encuentra los campos esperados, se quedan en valores por defecto (0 / 0.0).

### 3.2 Solicitudes de préstamos: request para aprobar/rechazar no coincide
Android envía `AprobarPrestamoRequest` con:
- `id_prestamo`, `aprobado: Boolean`

FastAPI espera:
- `id_prestamo`, `accion: "aprobar"|"rechazar"`, `id_empleado`

Esto rompe el flujo de aprobación/rechazo desde admin.

### 3.3 Supervision no usa endpoint dedicado de pendientes
Tu backend sí tiene `/admin/prestamos_pendientes`, pero Android arma pendientes iterando clientes + `/cliente/{id}/prestamos` y filtrando `estado == "PENDIENTE"`. Ese enfoque depende de que todos los contratos intermedios estén perfectos; con respuestas inconsistentes, termina vacío.

---

## 4) Cliente solicita préstamo y sale “Config: tasa=0.0 min=0.0 max=0.0” + “cuota $0.00, 0 mes”

Este fallo se explica por **desajuste de forma de respuesta** en configuración y de respuesta en solicitud.

## Causa 1: respuesta de `/configuracion_sistema` no coincide
Android espera un objeto directo:
```json
{ "id": 1, "tasa_interes": 5.0, ... }
```

Tu FastAPI devuelve:
```json
{ "status": "success", "configuracion": [ ... ] }
```

Como el modelo Android no coincide, la deserialización puede fallar o mapear campos en cero/null, de ahí logs `0.0`.

## Causa 2: respuesta de `/cliente/solicitar_credito` incompleta para Android
Android espera en `SolicitudCreditoResponse`:
- `id_prestamo`, `cuota_mensual`, `plazo_meses`, `monto`

FastAPI actual retorna solo:
- `status`, `message`

Por eso se imprime éxito pero los campos usados para mensaje quedan en valores default (0.00, 0 meses).

---

## 5) Al iniciar sesión como empleado la app se cierra

La causa más probable es **mismatch fuerte en modelos de cobranza** al entrar al dashboard de empleado.

## Causa técnica probable
`EmpleadoCobranzaViewModel` llama de inmediato `/empleado/pagos_pendientes` en `init`.  
Android modela cada item con campos como:
- `monto_total`, `saldo_pendiente`, `nombre_cliente`

Pero tu FastAPI devuelve:
- `folio`, `nombre`, `apellido_paterno`, etc. (sin varios de los campos esperados).

Ese mismatch puede disparar errores de parseo y comportamiento inestable justo al abrir el módulo de empleado.

Adicionalmente, `registrar_pago` también tiene contrato distinto:
- Android envía `metodo_pago`, espera `folio`, `id_ticket`, `monto_pagado`, `nuevo_saldo`
- FastAPI recibe solo `id_pago`, `id_empleado` y devuelve `monto`, `id_prestamo`

---

## 6) Matriz de incompatibilidades confirmadas (alta prioridad)

1. `GET /configuracion_sistema` → forma JSON incompatible (objeto esperado vs wrapper con lista).
2. `PUT /configuracion_sistema/{id}` → Android envía 4 campos numéricos; backend espera solo `valor: str`.
3. `GET /admin/estadisticas` → nombres de campos incompatibles.
4. `POST /admin/aprobar_prestamo` → body incompatible (`aprobado` vs `accion`).
5. `GET /empleado/pagos_pendientes` → shape de items incompatible.
6. `POST /empleado/registrar_pago` → request/response incompatibles.
7. `GET /empleado/corte_caja` → Android espera `total_tickets/total_general/tickets`; backend devuelve `total_pagos/total_cobrado/movimientos`.
8. `GET /tickets/{folio}` → Android espera `TicketCompleto` plano; backend devuelve `{status, ticket:{...}}`.
9. `GET /cliente/{id}/prestamos` y `.../pagos` en tu versión alterna devuelven lista directa en varias rutas legacy; Android espera wrapper con `status` y arreglo nombrado.

---

## 7) Acciones recomendadas para estabilizar pruebas (orden sugerido)

## Paso A — Congelar contrato de API (primero)
- Definir un OpenAPI único como “source of truth”.
- Alinear backend a los DTO Android actuales **o** actualizar Android a DTO reales del backend, pero no ambos “a medias”.

## Paso B — Corregir flujos críticos de timeout correo
- Mover envío de email a tarea asíncrona (`BackgroundTasks`/cola) y responder HTTP inmediatamente tras persistencia.
- Registrar estado de envío para reintentos.
- Revisar credenciales Gmail App Password reales en Railway (`GMAIL_USER`, `GMAIL_PASS`) y reputación SPF/DKIM.

## Paso C — Reparar módulo empleado/admin
- Alinear DTO de `pagos_pendientes`, `registrar_pago`, `corte_caja`, `estadisticas`, `aprobar_prestamo`.
- Usar endpoint directo `/admin/prestamos_pendientes` en Android en lugar de agregación manual por cliente.

## Paso D — Smoke tests automáticos mínimos (antes de UI)
- Ejecutar prueba de contrato para 10 endpoints críticos con payload fijo.
- Solo después correr pruebas de navegación/UI en app.

---

## 8) Conclusión

Los fallos que reportaste **sí son consistentes** con el código actual: el problema principal no parece ser “un bug aislado”, sino una combinación de:

1. **Timeouts por SMTP bloqueante en endpoints de registro/recuperación**, y
2. **Desalineación de contratos JSON entre Android y FastAPI** en módulos admin/empleado/configuración.

Con esas dos líneas corregidas primero, la estabilidad del sistema debería mejorar de forma importante antes de seguir con pruebas más profundas de navegación e interfaces.
