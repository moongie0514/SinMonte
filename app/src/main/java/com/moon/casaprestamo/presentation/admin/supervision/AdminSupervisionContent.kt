package com.moon.casaprestamo.presentation.admin.supervision

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.moon.casaprestamo.data.models.PrestamoPendienteAdmin
import com.moon.casaprestamo.presentation.admin.supervision.components.*

@Composable
fun AdminSupervisionContent(
    uiState: SupervisionUiState,
    idAprobador: Int,
    esAdmin: Boolean = true,
    onSetTab: (SupervisionTab) -> Unit,
    onSetFechas: (String, String) -> Unit,
    onCargarFolios: (String?) -> Unit,
    onAbrirEstadoCuenta: (String) -> Unit,
    onCerrarEstadoCuenta: () -> Unit,
    onAbrirSolicitud: (PrestamoPendienteAdmin) -> Unit,
    onCerrarSolicitud: () -> Unit,
    onAprobar: (Int) -> Unit,
    onRechazar: (Int) -> Unit,
    onLimpiarMensaje: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabEfectiva = if (!esAdmin && uiState.tab == SupervisionTab.SOLICITUDES) SupervisionTab.CARTERA else uiState.tab

    LaunchedEffect(esAdmin, uiState.tab) {
        if (!esAdmin && uiState.tab == SupervisionTab.SOLICITUDES) onSetTab(SupervisionTab.CARTERA)
    }

    Column(
        modifier            = modifier.fillMaxSize().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Barra superior (KPIs + selector de fechas) ────────
        BarraSuperior(
            recaudacion = if (tabEfectiva == SupervisionTab.FOLIOS) uiState.recaudacionFolios else uiState.recaudacionCartera,
            recaudacionLabel = if (tabEfectiva == SupervisionTab.FOLIOS) "FOLIOS" else "CARTERA",
            recaudacionCargando = tabEfectiva == SupervisionTab.FOLIOS && uiState.foliosLoading,
            solicitudesPendientes = uiState.solicitudesPendientes,
            tabActual = tabEfectiva,
            fechaDesde = uiState.fechaDesde,
            fechaHasta = uiState.fechaHasta,
            esAdmin = esAdmin,
            onLoad = onSetFechas,
            onClickRecaudacion = { if (esAdmin) onSetTab(SupervisionTab.CARTERA) },
            onClickSolicitudes = { if (esAdmin) onSetTab(SupervisionTab.SOLICITUDES) },
            onFechasChange = onSetFechas,
        )

        when (tabEfectiva) {
            SupervisionTab.CARTERA -> TabCartera(
                cartera = uiState.cartera,
                isLoading = uiState.carteraLoading,
                fechaDesde = uiState.fechaDesde,
                fechaHasta = uiState.fechaHasta,
                onSwitch = { onSetTab(SupervisionTab.FOLIOS) },
                onDetalle = onAbrirEstadoCuenta
            )

            SupervisionTab.FOLIOS -> TabLibroFolios(
                movimientos = uiState.folios,
                isLoading = uiState.foliosLoading,
                fechaDesde = uiState.fechaDesde,
                fechaHasta = uiState.fechaHasta,
                onSwitch = { onSetTab(SupervisionTab.CARTERA) },
                onCargarFolios = onCargarFolios,
                onClickFolio = onAbrirEstadoCuenta
            )

            SupervisionTab.SOLICITUDES -> if (esAdmin) {
                TabSolicitudes(
                    solicitudes = uiState.solicitudes,
                    isLoading = uiState.solicitudesLoading,
                    permiteAprobar = true,
                    onVolver = { onSetTab(SupervisionTab.CARTERA) },
                    onAbrirDetalle = onAbrirSolicitud
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
                color = if (msg.contains("✅")) Verde.copy(0.12f) else Rojo.copy(0.12f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    msg,
                    modifier = Modifier.padding(12.dp),
                    color = if (msg.contains("✅")) Verde else Rojo
                )
            }
        }
    }

    if (uiState.estadoCuentaLoading) {
        androidx.compose.ui.window.Dialog(onDismissRequest = onCerrarEstadoCuenta) {
            Box(Modifier.size(80.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Rojo) }
        }
    }

    uiState.estadoCuenta?.let { EstadoDeCuentaModal(detalle = it, onDismiss = onCerrarEstadoCuenta) }

    if (esAdmin) {
        uiState.solicitudDetalle?.let { sol ->
            DetalleSolicitudModal(
                prestamo = sol,
                permiteAprobar = true,
                onDismiss = onCerrarSolicitud,
                onAprobar = { onAprobar(sol.idPrestamo) },
                onRechazar = { onRechazar(sol.idPrestamo) }
            )
        }
    }
}
