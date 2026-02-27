# Validación de contratos: FastAPI actualizado vs Data Classes Android

Fecha: revisión posterior al FastAPI "API FINAL - MONTE SIN PIEDAD".

## Resultado general

Se actualizaron los Data Classes y ViewModels Android para alinearse con los nombres de campos del FastAPI nuevo, especialmente en:

- `admin/estadisticas`
- `empleado/pagos_pendientes`
- payloads de aprobación de préstamo y pagos

## Matriz rápida de coincidencia

| Endpoint | FastAPI (campos clave) | Android (modelo) | Estado |
|---|---|---|---|
| `GET /admin/estadisticas` | `total_clientes`, `prestamos_activos`, `capital_otorgado`, `saldo_pendiente`, `monto_recuperado` | `EstadisticasResponse` | ✅ Alineado |
| `POST /admin/aprobar_prestamo` | `id_prestamo`, `accion`, `id_empleado` | `AprobarPrestamoRequest` | ✅ Alineado |
| `GET /empleado/pagos_pendientes` | `id_pago`, `id_prestamo`, `numero_pago`, `fecha_vencimiento`, `monto`, `estado`, `monto_total`, `nombre_cliente`, `nombre`, `apellido_paterno`, `telefono` | `PagoPendiente` | ✅ Alineado |
| `POST /empleado/registrar_pago` | `status`, `message`, `monto`, `id_prestamo` | `RegistrarPagoResponse` | ✅ Alineado |

## Nota de robustez

`PagoPendiente` se dejó con campos opcionales de respaldo para tolerar cambios menores (`nombre_cliente`, `nombre`, `apellido_paterno`) sin romper UI.

## Cambios de UI derivados

`EmpleadoCobranzaContent` ahora consume `nombreClienteUi` (derivado) en lugar de depender de un único campo obligatorio.

`ReportesViewModel` ahora mapea estadísticas desde los nombres canónicos del backend nuevo (`total_clientes`, etc.).

