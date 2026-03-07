package com.moon.casaprestamo.presentation.admin.supervision

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moon.casaprestamo.data.models.PrestamoPendienteAdmin
import com.moon.casaprestamo.data.models.TicketDetalle
import com.moon.casaprestamo.ui.components.supervision.BarraSuperior
import com.moon.casaprestamo.ui.components.supervision.DetalleSolicitudModal
import com.moon.casaprestamo.ui.components.supervision.EstadoCuentaLoadingDialog
import com.moon.casaprestamo.ui.components.supervision.EstadoDeCuentaModal
import com.moon.casaprestamo.ui.components.supervision.Rojo
import com.moon.casaprestamo.ui.components.supervision.TabCartera
import com.moon.casaprestamo.ui.components.supervision.TabLibroFolios
import com.moon.casaprestamo.ui.components.supervision.TabSolicitudes
import com.moon.casaprestamo.ui.components.supervision.TicketPagoModal
import com.moon.casaprestamo.ui.components.supervision.Verde

// ═══════════════════════════════════════════════════════════
// ORQUESTADOR PRINCIPAL DE SUPERVISIÓN
// ═══════════════════════════════════════════════════════════

/**
 * Pantalla de Supervisión unificada para Admin y Empleado.
 *
 * @param esAdmin Si es `false`:
 *   - La tarjeta de RECAUDACIÓN muestra "Acceso restringido" y no es clicable.
 *   - La pestaña SOLICITUDES no aparece en la navegación.
 *   - Los diálogos de aprobación de préstamos quedan completamente bloqueados.
 */
@Composable
fun AdminSupervisionContent(
    uiState:               SupervisionUiState,
    idAprobador:           Int,
    esAdmin:               Boolean = true,          // ← parámetro de rol
    onSetTab:              (SupervisionTab) -> Unit,
    onSetFechas:           (String, String) -> Unit,
    onCargarFolios:        (String?) -> Unit,
    onAbrirEstadoCuenta:   (String) -> Unit,
    onCerrarEstadoCuenta:  () -> Unit,
    onAbrirTicketPago:     (TicketDetalle) -> Unit,
    onCerrarTicketPago:    () -> Unit,
    onAbrirSolicitud:      (PrestamoPendienteAdmin) -> Unit,
    onCerrarSolicitud:     () -> Unit,
    onAprobar:             (Int) -> Unit,
    onRechazar:            (Int) -> Unit,
    onLimpiarMensaje:      () -> Unit,
    modifier:              Modifier = Modifier
) {
    Column(
        modifier            = modifier.fillMaxSize().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Barra superior (KPIs + selector de fechas) ────────
        BarraSuperior(
            recaudacion         = if (uiState.tab == SupervisionTab.FOLIOS)
                uiState.recaudacionFolios
            else
                uiState.recaudacionCartera,
            recaudacionCargando = uiState.tab == SupervisionTab.FOLIOS && uiState.foliosLoading,
            solicitudesPendientes = uiState.solicitudesPendientes,
            tabActual           = uiState.tab,
            fechaDesde          = uiState.fechaDesde,
            fechaHasta          = uiState.fechaHasta,
            onLoad              = onSetFechas,
            // Recaudación visible para todos
            onClickRecaudacion  = { onSetTab(SupervisionTab.CARTERA) },
            // Solicitudes solo para Admin
            onClickSolicitudes  = if (esAdmin) {
                { onSetTab(SupervisionTab.SOLICITUDES) }
            } else {
                { /* acceso restringido */ }
            },
            onFechasChange      = onSetFechas,
            esAdmin             = esAdmin
        )

        // ── Contenido según pestaña activa ────────────────────
        // Empleados nunca ven SupervisionTab.SOLICITUDES
        val tabEfectivo = if (!esAdmin && uiState.tab == SupervisionTab.SOLICITUDES)
            SupervisionTab.CARTERA
        else
            uiState.tab

        when (tabEfectivo) {
            SupervisionTab.CARTERA -> TabCartera(
                cartera    = uiState.cartera,
                fechaDesde = uiState.fechaDesde,
                fechaHasta = uiState.fechaHasta,
                isLoading  = uiState.carteraLoading,
                onSwitch   = { onSetTab(SupervisionTab.FOLIOS) },
                onDetalle  = onAbrirEstadoCuenta
            )

            SupervisionTab.FOLIOS -> TabLibroFolios(
                folios         = uiState.folios,
                fechaDesde     = uiState.fechaDesde,
                fechaHasta     = uiState.fechaHasta,
                isLoading      = uiState.foliosLoading,
                onSwitch       = { onSetTab(SupervisionTab.CARTERA) },
                onCargarFolios = onCargarFolios,
                onClickTicket  = onAbrirTicketPago
            )

            SupervisionTab.SOLICITUDES -> {
                // Seguridad extra: si no es admin, jamás se llega aquí (tabEfectivo lo redirige),
                // pero si llegara, permiteAprobar = false bloquea los diálogos.
                TabSolicitudes(
                    solicitudes    = uiState.solicitudes,
                    permiteAprobar = esAdmin,
                    isLoading      = uiState.solicitudesLoading,
                    onVolver       = { onSetTab(SupervisionTab.CARTERA) },
                    onAbrirDetalle = if (esAdmin) onAbrirSolicitud else { _ -> }
                )
            }
        }

        // ── Toast de mensaje ──────────────────────────────────
        uiState.mensaje?.let { msg ->
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                onLimpiarMensaje()
            }
            Surface(
                color    = if (msg.contains("✅")) Verde.copy(alpha = 0.12f) else Rojo.copy(alpha = 0.12f),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    msg,
                    modifier   = Modifier.padding(12.dp),
                    color      = if (msg.contains("✅")) Verde else Rojo,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // ── Modales ───────────────────────────────────────────────

    if (uiState.estadoCuentaLoading) {
        EstadoCuentaLoadingDialog(onDismiss = onCerrarEstadoCuenta)
    }

    uiState.estadoCuenta?.let {
        EstadoDeCuentaModal(detalle = it, onDismiss = onCerrarEstadoCuenta)
    }

    // Ticket de pago (Libro de Folios)
    uiState.ticketPagoAbierto?.let { ticket ->
        TicketPagoModal(ticket = ticket, onDismiss = onCerrarTicketPago)
    }

    // Seguridad: diálogo de aprobación solo disponible para Admin
    if (esAdmin) {
        uiState.solicitudDetalle?.let { sol ->
            DetalleSolicitudModal(
                prestamo   = sol,
                onDismiss  = onCerrarSolicitud,
                onAprobar  = { onAprobar(sol.idPrestamo) },
                onRechazar = { onRechazar(sol.idPrestamo) }
            )
        }
    }
}