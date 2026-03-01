package com.moon.casaprestamo.presentation.admin.supervision

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.moon.casaprestamo.data.models.PrestamoPendienteAdmin
import com.moon.casaprestamo.ui.components.common.*
import java.text.SimpleDateFormat
import java.util.*

internal val Rojo     = Color(0xFFA6032F)
internal val Oscuro   = Color(0xFF0F172A)
internal val Verde    = Color(0xFF10B981)
internal val Amarillo = Color(0xFFF59E0B)

// Convierte "yyyy-MM-dd" a millis (medianoche UTC) para comparar
internal fun fechaStringAMillis(fecha: String): Long? {
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha)?.time
    } catch (e: Exception) { null }
}

// Convierte "dd/MM/yyyy" (formato display) a "yyyy-MM-dd" (formato API)
internal fun displayAApiDate(display: String): String {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val api = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        api.format(sdf.parse(display)!!)
    } catch (e: Exception) { display }
}

internal fun milisAFecha(milis: Long?): String {
    if (milis == null) return ""
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(milis))
}

// ═════════════════════════════════════════════════════════════
// CONTENT PRINCIPAL
// ═════════════════════════════════════════════════════════════

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
        modifier = modifier.fillMaxSize().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BarraSuperior(
            recaudacion           = if (uiState.tab == SupervisionTab.FOLIOS) uiState.recaudacionFolios else uiState.recaudacionCartera,
            recaudacionCargando   = uiState.tab == SupervisionTab.FOLIOS && uiState.foliosLoading,
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
        Dialog(onDismissRequest = onCerrarEstadoCuenta) {
            Box(Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Rojo)
            }
        }
    }
    uiState.estadoCuenta?.let {
        EstadoDeCuentaModal(detalle = it, onDismiss = onCerrarEstadoCuenta)
    }
    uiState.solicitudDetalle?.let { sol ->
        DetalleSolicitudModal(
            prestamo   = sol,
            onDismiss  = onCerrarSolicitud,
            onAprobar  = { onAprobar(sol.idPrestamo) },
            onRechazar = { onRechazar(sol.idPrestamo) }
        )
    }
}



// ─── Micro-helpers ───────────────────────────────────────────

@Composable
internal fun TH(text: String, modifier: Modifier) =
    Text(text, modifier = modifier, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.5.sp, color = MaterialTheme.colorScheme.outline)

@Composable
internal fun CalH(text: String, modifier: Modifier) =
    Text(text, modifier = modifier, fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8))

@Composable
internal fun CondItem(label: String, value: String, modifier: Modifier) =
    Column(modifier) {
        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black)
    }

@Composable
internal fun LoadBox() =
    Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) { CircularProgressIndicator(color = Rojo) }

@Composable
internal fun EmptyBox(msg: String) =
    Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) { Text(msg, color = MaterialTheme.colorScheme.outline) }
