# Recomendación práctica inmediata (backend-first)

Este plan está pensado para ejecutar **hoy mismo** y dejar backend + Android sin ambigüedades.

---

## 0) Orden de trabajo

1. Congelar contrato FastAPI (este sprint).
2. Probar contrato con smoke tests (curl).
3. Ajustar Android únicamente donde falle contra contrato.
4. Repetir smoke + validación manual por rol.

---

## 1) Checklist backend crítico (implementación)

## 1.1 Tiempo de respuesta
- [ ] `POST /registrar_cliente` responde < 2s (sin esperar SMTP).
- [ ] `POST /solicitar_codigo` responde < 2s.
- [ ] `POST /reenviar_codigo_verificacion` responde < 2s.

## 1.2 Contrato Admin/Empleado
- [ ] `/admin/estadisticas` devuelve `clientes/capital_activo/recuperado/morosos/pendientes`.
- [ ] `/admin/aprobar_prestamo` recibe `accion`.
- [ ] `/configuracion_sistema` GET devuelve `status + configuracion[]`.
- [ ] `/configuracion_sistema/{id}` PUT recibe `{valor}`.
- [ ] `/empleado/pagos_pendientes` devuelve lista con `nombre` y `apellido_paterno`.
- [ ] `/empleado/registrar_pago` recibe `{id_pago,id_empleado}` y devuelve `{status,message,monto,id_prestamo}`.
- [ ] `/empleado/corte_caja` devuelve `{status,fecha,total_pagos,total_cobrado,movimientos[]}`.
- [ ] `/tickets/{folio}` devuelve `{status,ticket:{...}}`.

---

## 2) Smoke tests (curl) — copiar y pegar

> Define base URL primero:

```bash
export API_BASE="https://TU-DOMINIO-RAILWAY"
```

## 2.1 Health
```bash
curl -sS "$API_BASE/" | jq
```

## 2.2 Configuración
```bash
curl -sS "$API_BASE/configuracion_sistema" | jq
curl -sS -X PUT "$API_BASE/configuracion_sistema/1" \
  -H "Content-Type: application/json" \
  -d '{"valor":"0.05"}' | jq
```

## 2.3 Estadísticas admin
```bash
curl -sS "$API_BASE/admin/estadisticas" | jq
```

## 2.4 Aprobación/rechazo préstamo
```bash
curl -sS -X POST "$API_BASE/admin/aprobar_prestamo" \
  -H "Content-Type: application/json" \
  -d '{"id_prestamo":34,"accion":"aprobar","id_empleado":8}' | jq
```

## 2.5 Empleado
```bash
curl -sS "$API_BASE/empleado/pagos_pendientes" | jq

curl -sS -X POST "$API_BASE/empleado/registrar_pago" \
  -H "Content-Type: application/json" \
  -d '{"id_pago":91,"id_empleado":8}' | jq

curl -sS "$API_BASE/empleado/corte_caja?id_empleado=8&fecha=2026-02-27" | jq
```

## 2.6 Ticket
```bash
curl -sS "$API_BASE/tickets/MSP-34" | jq
```

## 2.7 Auth performance (tiempo)
```bash
time curl -sS -X POST "$API_BASE/registrar_cliente" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Test","apellido_paterno":"Uno","email":"test1@example.com","password":"123456"}' | jq

time curl -sS -X POST "$API_BASE/solicitar_codigo?email=test1@example.com" | jq
```

---

## 3) Matriz de aceptación rápida

Marca PASS/FAIL:

| Caso | Esperado | Resultado |
|---|---|---|
| Registro responde rápido | < 2s |  |
| Solicitar código responde rápido | < 2s |  |
| Estadísticas con datos reales | campos no nulos/0 según BD |  |
| Empleado entra sin crash | pantalla cobranza carga |  |
| Pagos pendientes parsean | lista visible |  |
| Registrar pago funciona | mensaje éxito |  |
| Solicitar crédito ya no muestra 0.00/0 | texto correcto |  |

---

## 4) Después de backend estable

1. Volver a Android y ajustar solo los puntos que fallen contra contrato real.
2. Generar `openapi.json` final y versionarlo (v1.0.0).
3. Añadir regresión mínima (smoke script en CI o manual semanal).

