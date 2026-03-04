package com.moon.casaprestamo.ui.components.supervision

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.moon.casaprestamo.data.models.PrestamoPendienteAdmin
import com.moon.casaprestamo.data.models.TicketDetalle
import com.moon.casaprestamo.data.models.TicketPrestamoDetalle
import com.moon.casaprestamo.presentation.cliente.cartera.PagoRowPrototipo
import com.moon.casaprestamo.presentation.cliente.cartera.SpecificCard

// ═══════════════════════════════════════════════════════════
// Dialog: Estado de Cuenta
// ═══════════════════════════════════════════════════════════

@Composable
fun EstadoDeCuentaModal(detalle: TicketPrestamoDetalle, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape    = RoundedCornerShape(24.dp),
            color    = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.92f)
        ) {
            Column {
                // ── Header oscuro ──────────────────────────────────
                Box(Modifier.fillMaxWidth().background(Oscuro).padding(20.dp)) {
                    Column {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text("Estado de Cuenta", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    verticalAlignment    = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "${detalle.nombre} ${detalle.apellidoPaterno} ${detalle.apellidoMaterno ?: ""}".trim().uppercase(),
                                        color      = Rojo,
                                        fontWeight = FontWeight.Black,
                                        fontSize   = 11.sp
                                    )
                                    Text("|", color = Color.White.copy(alpha = 0.3f))
                                    Text(
                                        "EXPEDIENTE: ${detalle.folio}",
                                        color      = Color.White.copy(alpha = 0.7f),
                                        fontSize   = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
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
                                border  = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                shape   = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Download, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("DESCARGAR", fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // ── Cuerpo con LazyColumn ──────────────────────────
                LazyColumn(
                    modifier        = Modifier.fillMaxSize(),
                    contentPadding  = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        val liquidado = maxOf(0.0, detalle.montoTotal - detalle.saldoPendiente)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SpecificCard("SALDO PENDIENTE", "\$${String.format("%,.0f", detalle.saldoPendiente)}", Oscuro, Color.White, Modifier.weight(1f))
                            SpecificCard("TOTAL PAGADO",    "\$${String.format("%,.0f", liquidado)}",              Verde, Color.White, Modifier.weight(1f))
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

// ═══════════════════════════════════════════════════════════
// Dialog: Detalle de Solicitud (solo Admin)
// ═══════════════════════════════════════════════════════════

/**
 * Modal de detalle de solicitud con botones de APROBAR / RECHAZAR.
 * Solo debe abrirse cuando el usuario es Administrador.
 * La capa de seguridad extra está en [TabSolicitudes] y [AdminSupervisionContent].
 */
@Composable
internal fun DetalleSolicitudModal(
    prestamo:  PrestamoPendienteAdmin,
    onDismiss: () -> Unit,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape    = RoundedCornerShape(24.dp),
            color    = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                    Column {
                        Text(
                            "FOLIO #${prestamo.idPrestamo.toString().padStart(5, '0')}",
                            fontSize      = 10.sp,
                            fontWeight    = FontWeight.Black,
                            color         = MaterialTheme.colorScheme.outline,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${prestamo.nombre ?: ""} ${prestamo.apellidoPaterno ?: ""}".trim().uppercase(),
                            fontWeight = FontWeight.Black,
                            fontSize   = 18.sp
                        )
                    }
                    Surface(color = Amarillo.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
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
                        onClick  = onRechazar,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = Rojo)
                    ) {
                        Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("RECHAZAR", fontWeight = FontWeight.Black)
                    }
                    Button(
                        onClick  = onAprobar,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Oscuro)
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

// ═══════════════════════════════════════════════════════════
// Dialog: Carga de Estado de Cuenta
// ═══════════════════════════════════════════════════════════

@Composable
fun EstadoCuentaLoadingDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(Modifier.size(80.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Rojo)
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Dialog: Ticket de Pago (al presionar fila en Libro de Folios)
// ═══════════════════════════════════════════════════════════

/**
 * Recibo de pago estilo ticket de caja.
 * Muestra los datos del [ticket] seleccionado en el Libro de Folios.
 * El campo [nombreEmpleado] proviene de [TicketDetalle.nombreEmpleado]
 * cuando el backend lo devuelva; de lo contrario muestra "–".
 */
@Composable
fun TicketPagoModal(
    ticket: TicketDetalle,
    onDismiss: () -> Unit
) {
    val cajero = if (ticket.nombreEmpleado.isNullOrBlank()) "–" else ticket.nombreEmpleado.uppercase()

    // Formatear fecha "yyyy-MM-dd..." → "dd/MM/yyyy"
    val fechaDisplay = try {
        val raw = ticket.fechaGeneracion.take(10)
        val sdfIn  = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val sdfOut = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        sdfOut.format(sdfIn.parse(raw)!!)
    } catch (e: Exception) { ticket.fechaGeneracion.take(10) }

    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape    = RoundedCornerShape(20.dp),
            color    = Color.White,
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            Column(
                modifier            = Modifier.padding(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Botón cerrar ──────────────────────────────────
                Box(Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick  = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFF64748B))
                    }
                }

                // ── Encabezado empresa ────────────────────────────
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "MONTE SIN PIEDAD S.A.",
                        fontWeight    = FontWeight.Black,
                        fontSize      = 16.sp,
                        letterSpacing = 0.5.sp,
                        fontFamily    = FontFamily.Monospace,
                        color         = Color(0xFF0F172A)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "SUCURSAL CENTRO #084",
                        fontSize      = 10.sp,
                        letterSpacing = 0.5.sp,
                        fontFamily    = FontFamily.Monospace,
                        color         = Color(0xFF64748B)
                    )
                    Text(
                        "RFC: MSP-920101-AAA",
                        fontSize   = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color      = Color(0xFF64748B)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Línea punteada ────────────────────────────────
                TicketDivider()

                Spacer(Modifier.height(14.dp))

                // ── Filas de datos ────────────────────────────────
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TicketFila("FECHA:",   fechaDisplay)
                    TicketFila("FOLIO:",   ticket.folio)
                    TicketFila("CAJERO:",  cajero)
                    TicketFila("CLIENTE:", "${ticket.nombre} ${ticket.apellidoPaterno}".trim().uppercase())
                    TicketFila("PRÉSTAMO:", ticket.folioPrestamo)
                    TicketFila("MÉTODO:",  ticket.metodoPago.uppercase())
                }

                Spacer(Modifier.height(14.dp))

                // ── Línea sólida ──────────────────────────────────
                HorizontalDivider(
                    modifier  = Modifier.padding(horizontal = 28.dp),
                    thickness = 1.5.dp,
                    color     = Color(0xFF0F172A)
                )

                Spacer(Modifier.height(14.dp))

                // ── Total ─────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "TOTAL:",
                        fontWeight = FontWeight.Black,
                        fontSize   = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        color      = androidx.compose.ui.graphics.Color(0xFF0F172A)
                    )
                    Text(
                        "\$${String.format("%,.0f", ticket.montoPagado)}",
                        fontWeight = FontWeight.Black,
                        fontSize   = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        color      = androidx.compose.ui.graphics.Color(0xFF0F172A)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── Código de barras ──────────────────────────────
                TicketBarcode(folio = ticket.folio)

                Spacer(Modifier.height(6.dp))

                Text(
                    ticket.folio,
                    fontSize   = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color      = Color(0xFF64748B)
                )

                Spacer(Modifier.height(18.dp))

                // ── Pie de página ─────────────────────────────────
                Text(
                    "*** GRACIAS POR SU PREFERENCIA ***",
                    fontSize      = 10.sp,
                    fontFamily    = FontFamily.Monospace,
                    letterSpacing = 0.3.sp,
                    color         = androidx.compose.ui.graphics.Color(0xFF94A3B8)
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ─── Helpers internos del ticket ──────────────────────────

/** Fila etiqueta / valor del ticket. */
@Composable
private fun TicketFila(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Top
    ) {
        Text(
            label,
            fontSize   = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color      = androidx.compose.ui.graphics.Color(0xFF64748B)
        )
        Text(
            value,
            fontSize   = 12.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color      = androidx.compose.ui.graphics.Color(0xFF0F172A),
            modifier   = Modifier.fillMaxWidth(0.6f),
            textAlign  = androidx.compose.ui.text.style.TextAlign.End,
            maxLines   = 2
        )
    }
}

/** Línea punteada separadora estilo ticket físico. */
@Composable
private fun TicketDivider() {
    Text(
        text      = "- ".repeat(28),
        fontSize  = 10.sp,
        fontFamily = FontFamily.Monospace,
        color     = androidx.compose.ui.graphics.Color(0xFFCBD5E1),
        maxLines  = 1,
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        overflow  = androidx.compose.ui.text.style.TextOverflow.Clip
    )
}

/**
 * Código de barras generado con Canvas a partir del folio.
 * Alterna barras anchas y angostas según los bytes del texto.
 */
@Composable
private fun TicketBarcode(folio: String) {
    val barColor = androidx.compose.ui.graphics.Color(0xFF0F172A)
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .width(180.dp)
            .height(48.dp)
    ) {
        val bytes  = folio.toByteArray()
        val totalBars = 60
        val barUnit   = size.width / (totalBars * 1.6f)
        var x = 0f
        repeat(totalBars) { i ->
            val byte    = bytes[i % bytes.size].toInt() and 0xFF
            val isWide  = (byte + i) % 3 == 0
            val barW    = if (isWide) barUnit * 2.4f else barUnit
            val gapW    = barUnit * 0.6f
            if (i % 2 == 0) {
                drawRect(
                    color   = barColor,
                    topLeft = androidx.compose.ui.geometry.Offset(x, 0f),
                    size    = androidx.compose.ui.geometry.Size(barW, size.height)
                )
            }
            x += barW + gapW
        }
    }
}