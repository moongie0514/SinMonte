package com.moon.casaprestamo.presentation.admin.supervision

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.moon.casaprestamo.data.models.PrestamoPendienteAdmin
import com.moon.casaprestamo.data.models.TicketDetalle
import com.moon.casaprestamo.data.models.TicketPrestamoDetalle
import com.moon.casaprestamo.presentation.admin.supervision.components.BarraSuperior
import com.moon.casaprestamo.presentation.admin.supervision.components.TabCartera
import com.moon.casaprestamo.presentation.admin.supervision.components.TabFolios
import com.moon.casaprestamo.presentation.admin.supervision.components.TabSolicitudes
import com.moon.casaprestamo.presentation.cliente.cartera.PagoRowPrototipo
import com.moon.casaprestamo.presentation.cliente.cartera.SpecificCard
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

    Column(
        modifier = modifier.fillMaxSize().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BarraSuperior(
            recaudacion           = if (uiState.tab == SupervisionTab.FOLIOS) uiState.recaudacionFolios else uiState.recaudacionCartera,
            recaudacionLabel      = if (uiState.tab == SupervisionTab.FOLIOS) "FOLIOS" else "CARTERA",
            recaudacionCargando   = uiState.tab == SupervisionTab.FOLIOS && uiState.foliosLoading,
            solicitudesPendientes = uiState.solicitudesPendientes,
            tabActual             = uiState.tab,
            fechaDesde            = uiState.fechaDesde,
            fechaHasta            = uiState.fechaHasta,
            onLoad                = onSetFechas,
            onClickRecaudacion    = { onSetTab(SupervisionTab.CARTERA) },
            onClickSolicitudes    = { onSetTab(SupervisionTab.SOLICITUDES) },
            onFechasChange        = onSetFechas,
        )

        when (uiState.tab) {
            SupervisionTab.CARTERA -> TabCartera(
                uiState   = uiState,
                onSwitch  = { onSetTab(SupervisionTab.FOLIOS) },
                onDetalle = onAbrirEstadoCuenta
            )
            SupervisionTab.FOLIOS -> TabFolios(
                uiState        = uiState,
                onSwitch       = { onSetTab(SupervisionTab.CARTERA) },
                onCargarFolios = onCargarFolios,
                onClickFolio   = onAbrirEstadoCuenta
            )
            SupervisionTab.SOLICITUDES -> TabSolicitudes(
                uiState        = uiState,
                onVolver       = { onSetTab(SupervisionTab.CARTERA) },
                onAbrirDetalle = onAbrirSolicitud
            )
        }

        uiState.mensaje?.let { msg ->
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                onLimpiarMensaje()
            }
            Surface(
                color    = if (msg.contains("✅")) Verde.copy(0.12f) else Rojo.copy(0.12f),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    msg, modifier = Modifier.padding(12.dp),
                    color = if (msg.contains("✅")) Verde else Rojo,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Modales
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

@Composable
fun EstadoDeCuentaModal(detalle: TicketPrestamoDetalle, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.92f)
        ) {
            Column {
                Box(Modifier.fillMaxWidth().background(Oscuro).padding(20.dp)) {
                    Column {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text("Estado de Cuenta", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "${detalle.nombre} ${detalle.apellidoPaterno} ${detalle.apellidoMaterno ?: ""}".trim().uppercase(),
                                        color = Rojo, fontWeight = FontWeight.Black, fontSize = 11.sp
                                    )
                                    Text("|", color = Color.White.copy(0.3f))
                                    Text("EXPEDIENTE: ${detalle.folio}", color = Color.White.copy(0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, null, tint = Color.White)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { /* TODO compartir */ },
                                colors  = ButtonDefaults.buttonColors(containerColor = Rojo),
                                shape   = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Email, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("COMPARTIR", fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { /* TODO descargar PDF */ },
                                colors  = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border  = BorderStroke(1.dp, Color.White.copy(0.3f)),
                                shape   = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Download, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("DESCARGAR", fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // FIX 2: total pagado nunca negativo
                    item {
                        val liquidado = maxOf(0.0, detalle.montoTotal - detalle.saldoPendiente)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SpecificCard("SALDO PENDIENTE", "\$${String.format("%,.0f", detalle.saldoPendiente)}", Oscuro, Color.White, Modifier.weight(1f))
                            SpecificCard("TOTAL PAGADO",    "\$${String.format("%,.0f", liquidado)}",             Verde, Color.White, Modifier.weight(1f))
                        }
                    }
                    item {
                        val cuota = if (detalle.plazoMeses > 0) detalle.montoTotal / detalle.plazoMeses else 0.0
                        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("CUOTA MENSUAL", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                                Text("\$${String.format("%,.0f", cuota)}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Rojo)
                            }
                        }
                    }
                    item {
                        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Person, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                                    Text("TITULAR", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                                }
                                Text("${detalle.nombre} ${detalle.apellidoPaterno}".uppercase(), fontWeight = FontWeight.Black, fontSize = 15.sp)
                                detalle.telefono?.let { Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline) }
                            }
                        }
                    }
                    item {
                        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.AccountBalance, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                                    Text("CONDICIONES", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                                }
                                Row(Modifier.fillMaxWidth()) {
                                    CondItem("MONTO", "\$${String.format("%,.0f", detalle.montoTotal)}", Modifier.weight(1f))
                                    CondItem("PLAZO", "${detalle.plazoMeses} Meses", Modifier.weight(1f))
                                }
                                Row(Modifier.fillMaxWidth()) {
                                    val tasa = if (detalle.tasaInteres > 1) "${detalle.tasaInteres.toInt()}%" else "${(detalle.tasaInteres * 100).toInt()}%"
                                    CondItem("TASA",  tasa, Modifier.weight(1f))
                                    CondItem("PAGOS", "${detalle.pagosRealizados}/${detalle.totalPagos}", Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("CALENDARIO DE PAGOS", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.outline)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                            CalH("NO.",    Modifier.width(30.dp))
                            CalH("FECHA",  Modifier.weight(1f))
                            CalH("MONTO",  Modifier.weight(1f))
                            CalH("ESTADO", Modifier.width(80.dp))
                        }
                    }
                    items(detalle.pagos, key = { it.idPago }) { pago ->
                        PagoRowPrototipo(pago)
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// MODAL: DETALLE SOLICITUD
// ═════════════════════════════════════════════════════════════

@Composable
internal fun DetalleSolicitudModal(
    prestamo: PrestamoPendienteAdmin,
    onDismiss: () -> Unit,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth(0.95f)) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                    Column {
                        Text("FOLIO #${prestamo.idPrestamo.toString().padStart(5, '0')}", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline, letterSpacing = 1.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("${prestamo.nombre ?: ""} ${prestamo.apellidoPaterno ?: ""}".trim().uppercase(), fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                    Surface(color = Amarillo.copy(0.15f), shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, Modifier.size(14.dp), tint = Amarillo)
                            Text("PENDIENTE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Amarillo)
                        }
                    }
                }
                HorizontalDivider()
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Column {
                        Text("MONTO SOLICITADO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                        Text("\$${String.format("%,.0f", prestamo.montoTotal)}", fontSize = 28.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("PLAZO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                        Text("${prestamo.plazoMeses} MESES", fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
                if (!prestamo.curp.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Badge, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                        Text(prestamo.curp!!, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
                prestamo.email?.let {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Email, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                        Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
                Text("Solicitud: ${prestamo.fechaCreacion.take(10)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onRechazar, modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Rojo)
                    ) {
                        Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("RECHAZAR", fontWeight = FontWeight.Black)
                    }
                    Button(
                        onClick = onAprobar, modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Oscuro)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("APROBAR", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
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