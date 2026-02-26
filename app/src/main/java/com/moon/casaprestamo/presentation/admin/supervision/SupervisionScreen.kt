package com.moon.casaprestamo.presentation.admin.supervision

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moon.casaprestamo.ui.theme.CasaPrestamoTheme

// ════════════════════════════════════════════════
// COLORES
// ════════════════════════════════════════════════
private val Oscuro  = Color(0xFF0F172A)
private val Rojo    = Color(0xFFA6032F)
private val Verde   = Color(0xFF10B981)
private val Amber   = Color(0xFFF59E0B)
private val Azul    = Color(0xFF3B82F6)
private val Gris50  = Color(0xFFF8FAFC)
private val Gris100 = Color(0xFFF1F5F9)
private val Gris400 = Color(0xFF94A3B8)

// ════════════════════════════════════════════════
// MODELOS LOCALES (reemplazar con los de data.models cuando Railway esté listo)
// ════════════════════════════════════════════════
data class PrestamoSupervision(
    val idPrestamo: Int,
    val nombreCliente: String,
    val curp: String,
    val fechaInicio: String,
    val montoTotal: Double,
    val saldoPendiente: Double,
    val estado: String  // "ACTIVO" | "MORA" | "LIQUIDADO" | "PENDIENTE"
)

data class TicketSupervision(
    val folio: String,
    val cliente: String,
    val fechaPago: String,
    val montoPagado: Double,
    val metodoPago: String,
    val empleado: String
)

enum class VistaSupervision { PRESTAMOS, TICKETS, SOLICITUDES }

// ════════════════════════════════════════════════
// PANTALLA PRINCIPAL
// isAdmin=false → empleado (sin tab de solicitudes si no hay pendientes)
// isAdmin=true  → admin (siempre ve solicitudes)
// ════════════════════════════════════════════════
@Composable
fun SupervisionScreen(
    prestamos: List<PrestamoSupervision> = emptyList(),
    tickets: List<TicketSupervision> = emptyList(),
    isAdmin: Boolean = false,
    onAprobarPrestamo: (Int) -> Unit = {},
    onRechazarPrestamo: (Int) -> Unit = {}
) {
    var vista by remember { mutableStateOf(VistaSupervision.PRESTAMOS) }
    var busqueda by remember { mutableStateOf("") }
    var prestamoDetalle by remember { mutableStateOf<PrestamoSupervision?>(null) }
    var ticketDetalle by remember { mutableStateOf<TicketSupervision?>(null) }

    val solicitudes      = prestamos.filter { it.estado == "PENDIENTE" }
    val activos          = prestamos.filter { it.estado != "PENDIENTE" }
    val totalRecaudado   = tickets.sumOf { it.montoPagado }
    val mostrarSolicitudes = isAdmin || solicitudes.isNotEmpty()

    val prestamosFiltrados = activos.filter {
        it.nombreCliente.contains(busqueda, ignoreCase = true) ||
                it.curp.contains(busqueda, ignoreCase = true) ||
                it.idPrestamo.toString().contains(busqueda)
    }
    val ticketsFiltrados = tickets.filter {
        it.folio.contains(busqueda, ignoreCase = true) ||
                it.cliente.contains(busqueda, ignoreCase = true)
    }

    Scaffold(containerColor = Gris100) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // ── HEADER OSCURO ──────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Oscuro)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text  = when (vista) {
                        VistaSupervision.PRESTAMOS   -> "CARTERA ACTIVA"
                        VistaSupervision.TICKETS     -> "LIBRO DE FOLIOS"
                        VistaSupervision.SOLICITUDES -> "SOLICITUDES"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight    = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color         = Color.White
                    )
                )

                // Chips de navegación
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NavChip(
                        label   = "CARTERA",
                        valor   = "${activos.size}",
                        activo  = vista == VistaSupervision.PRESTAMOS,
                        color   = Azul,
                        onClick = { vista = VistaSupervision.PRESTAMOS; busqueda = "" },
                        modifier = Modifier.weight(1f)
                    )
                    NavChip(
                        label   = "FOLIOS",
                        valor   = "$${(totalRecaudado / 1000).toInt()}K",
                        activo  = vista == VistaSupervision.TICKETS,
                        color   = Verde,
                        onClick = { vista = VistaSupervision.TICKETS; busqueda = "" },
                        modifier = Modifier.weight(1f)
                    )
                    if (mostrarSolicitudes) {
                        NavChip(
                            label   = "SOLICITUDES",
                            valor   = "${solicitudes.size}",
                            activo  = vista == VistaSupervision.SOLICITUDES,
                            color   = Amber,
                            onClick = { vista = VistaSupervision.SOLICITUDES; busqueda = "" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── BUSCADOR ──────────────────────────────────────────
            if (vista != VistaSupervision.SOLICITUDES) {
                OutlinedTextField(
                    value         = busqueda,
                    onValueChange = { busqueda = it },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape         = RoundedCornerShape(12.dp),
                    placeholder   = {
                        Text(
                            "Buscar por nombre o ID...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gris400
                        )
                    },
                    leadingIcon  = { Icon(Icons.Default.Search, null, tint = Gris400, modifier = Modifier.size(18.dp)) },
                    trailingIcon = if (busqueda.isNotEmpty()) {{
                        IconButton(onClick = { busqueda = "" }) {
                            Icon(Icons.Default.Close, null, tint = Gris400, modifier = Modifier.size(16.dp))
                        }
                    }} else null,
                    singleLine   = true,
                    colors       = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = Rojo,
                        unfocusedBorderColor    = Color.Transparent,
                        focusedContainerColor   = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color      = Oscuro
                    )
                )
            }

            // ── CONTENIDO ─────────────────────────────────────────
            when (vista) {
                VistaSupervision.PRESTAMOS ->
                    if (prestamosFiltrados.isEmpty()) EmptyState("Sin préstamos activos")
                    else LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(prestamosFiltrados, key = { it.idPrestamo }) { p ->
                            TarjetaPrestamo(prestamo = p, onClick = { prestamoDetalle = p })
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }

                VistaSupervision.TICKETS ->
                    if (ticketsFiltrados.isEmpty()) EmptyState("Sin tickets registrados")
                    else LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ticketsFiltrados, key = { it.folio }) { t ->
                            TarjetaTicket(ticket = t, onClick = { ticketDetalle = t })
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }

                VistaSupervision.SOLICITUDES ->
                    if (solicitudes.isEmpty()) EmptyState("Sin solicitudes pendientes")
                    else LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(solicitudes, key = { it.idPrestamo }) { s ->
                            TarjetaSolicitud(
                                solicitud  = s,
                                onAprobar  = { onAprobarPrestamo(s.idPrestamo) },
                                onRechazar = { onRechazarPrestamo(s.idPrestamo) }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
            }
        }
    }

    // Modales
    prestamoDetalle?.let { ModalDetallePrestamo(it) { prestamoDetalle = null } }
    ticketDetalle?.let   { ModalDetalleTicket(it)   { ticketDetalle = null } }
}

// ════════════════════════════════════════════════
// NAV CHIP
// ════════════════════════════════════════════════
@Composable
private fun NavChip(
    label: String,
    valor: String,
    activo: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier  = modifier.clickable { onClick() },
        shape     = RoundedCornerShape(12.dp),
        color     = if (activo) color.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
        border    = if (activo) BorderStroke(1.dp, color.copy(alpha = 0.5f)) else null
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize      = 7.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 0.3.sp,
                    color         = if (activo) color else Gris400
                )
            )
            Text(
                text  = valor,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Black,
                    color      = if (activo) color else Color.White
                ),
                maxLines = 1
            )
        }
    }
}

// ════════════════════════════════════════════════
// TARJETA PRÉSTAMO
// ════════════════════════════════════════════════
@Composable
private fun TarjetaPrestamo(
    prestamo: PrestamoSupervision,
    onClick: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier              = Modifier.padding(14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier         = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Oscuro),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    prestamo.nombreCliente.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = Color.White)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    prestamo.nombreCliente,
                    style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black, color = Oscuro),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    prestamo.curp,
                    style = MaterialTheme.typography.labelSmall.copy(color = Gris400, fontSize = 9.sp, letterSpacing = 0.3.sp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "$${prestamo.saldoPendiente.toLong().formatMiles()} pendiente",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        color      = if (prestamo.estado == "MORA") Rojo else Oscuro
                    )
                )
            }
            EstadoBadge(prestamo.estado)
        }
    }
}

// ════════════════════════════════════════════════
// TARJETA TICKET
// ════════════════════════════════════════════════
@Composable
private fun TarjetaTicket(
    ticket: TicketSupervision,
    onClick: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier              = Modifier.padding(14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier         = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Rojo.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Receipt, null, tint = Rojo, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    ticket.folio,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Black, color = Rojo,
                        fontFamily = FontFamily.Monospace
                    )
                )
                Text(
                    ticket.cliente,
                    style    = MaterialTheme.typography.labelSmall.copy(color = Gris400, fontSize = 10.sp),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$${ticket.montoPagado.toLong().formatMiles()}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black, color = Oscuro)
                )
                Text(
                    ticket.metodoPago.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall.copy(color = Gris400, fontSize = 9.sp)
                )
            }
        }
    }
}

// ════════════════════════════════════════════════
// TARJETA SOLICITUD
// ════════════════════════════════════════════════
@Composable
private fun TarjetaSolicitud(
    solicitud: PrestamoSupervision,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(1.dp, Color(0xFFFEF3C7))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier         = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        solicitud.nombreCliente.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = Amber)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        solicitud.nombreCliente,
                        style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black, color = Oscuro),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "REF: ${solicitud.idPrestamo}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Gris400, fontSize = 9.sp)
                    )
                }
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFEF3C7)) {
                    Text(
                        "POR REVISAR",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style    = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 7.sp, fontWeight = FontWeight.Black,
                            color = Amber, letterSpacing = 0.3.sp
                        )
                    )
                }
            }
            Text(
                "$${solicitud.montoTotal.toLong().formatMiles()}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, color = Oscuro)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick  = onRechazar,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape    = RoundedCornerShape(12.dp),
                    border   = BorderStroke(1.dp, Rojo.copy(alpha = 0.3f)),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = Rojo)
                ) {
                    Text("RECHAZAR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp))
                }
                Button(
                    onClick  = onAprobar,
                    modifier = Modifier.weight(2f).height(44.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Oscuro)
                ) {
                    Text("APROBAR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp, color = Color.White))
                }
            }
        }
    }
}

// ════════════════════════════════════════════════
// MODALES
// ════════════════════════════════════════════════
@Composable
private fun ModalDetallePrestamo(prestamo: PrestamoSupervision, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color.White,
        shape            = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text("ESTADO DE CUENTA", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = Oscuro))
                Text(prestamo.nombreCliente, style = MaterialTheme.typography.bodySmall.copy(color = Gris400))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FilaDetalle("EXPEDIENTE", "#${prestamo.idPrestamo}")
                FilaDetalle("CURP", prestamo.curp)
                FilaDetalle("MONTO TOTAL", "$${prestamo.montoTotal.toLong().formatMiles()}")
                FilaDetalle("SALDO PENDIENTE", "$${prestamo.saldoPendiente.toLong().formatMiles()}", colorValor = if (prestamo.estado == "MORA") Rojo else Verde)
                FilaDetalle("FECHA", prestamo.fechaInicio)
                FilaDetalle("ESTADO", prestamo.estado)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CERRAR", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black, color = Oscuro))
            }
        }
    )
}

@Composable
private fun ModalDetalleTicket(ticket: TicketSupervision, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color.White,
        shape            = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    ticket.folio,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black, color = Rojo, fontFamily = FontFamily.Monospace
                    )
                )
                Text("Comprobante de pago", style = MaterialTheme.typography.bodySmall.copy(color = Gris400))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FilaDetalle("CLIENTE", ticket.cliente)
                FilaDetalle("FECHA", ticket.fechaPago)
                FilaDetalle("IMPORTE", "$${ticket.montoPagado.toLong().formatMiles()}", colorValor = Verde)
                FilaDetalle("MÉTODO", ticket.metodoPago)
                FilaDetalle("AUTORIZÓ", ticket.empleado)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CERRAR", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black, color = Oscuro))
            }
        }
    )
}

// ════════════════════════════════════════════════
// MICRO-COMPONENTES
// ════════════════════════════════════════════════
@Composable
private fun EstadoBadge(estado: String) {
    val (bg, fg) = when (estado) {
        "ACTIVO"    -> Color(0xFFECFDF5) to Verde
        "MORA"      -> Color(0xFFFEF2F2) to Rojo
        "LIQUIDADO" -> Gris100          to Gris400
        else        -> Color(0xFFFEF3C7) to Amber
    }
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Text(
            estado,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style    = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.sp, fontWeight = FontWeight.Black, color = fg, letterSpacing = 0.3.sp
            )
        )
    }
}

@Composable
private fun FilaDetalle(label: String, valor: String, colorValor: Color = Oscuro) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Gris400, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.3.sp))
        Text(valor, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black, color = colorValor))
    }
}

@Composable
private fun EmptyState(mensaje: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Inbox, null, tint = Gris400, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text(mensaje, style = MaterialTheme.typography.bodyMedium.copy(color = Gris400, fontWeight = FontWeight.Bold))
        }
    }
}

private fun Long.formatMiles(): String {
    var s = this.toString(); var r = ""; var c = 0
    for (i in s.indices.reversed()) { if (c > 0 && c % 3 == 0) r = ",$r"; r = "${s[i]}$r"; c++ }
    return r
}

// ════════════════════════════════════════════════
// PREVIEWS
// ════════════════════════════════════════════════
private val prestamosMock = listOf(
    PrestamoSupervision(1, "Hangelica Reyes",  "REYH950412MDFXXX01", "2024-01-15", 15000.0, 8500.0,  "ACTIVO"),
    PrestamoSupervision(2, "Roberto Garza",    "GARR880903HNLXXX02", "2024-02-20", 20000.0, 20000.0, "MORA"),
    PrestamoSupervision(3, "María López",      "LOPM001215MDFXXX03", "2023-11-10", 10000.0, 0.0,     "LIQUIDADO"),
    PrestamoSupervision(4, "Carlos Mendoza",   "MECC990718HDLXXX04", "2024-03-10", 25000.0, 25000.0, "PENDIENTE"),
)
private val ticketsMock = listOf(
    TicketSupervision("TP-00001", "Hangelica Reyes", "2024-03-01", 1500.0, "EFECTIVO",      "Juan E."),
    TicketSupervision("TP-00002", "Roberto Garza",   "2024-03-05", 2000.0, "TRANSFERENCIA", "Juan E."),
    TicketSupervision("TP-00003", "María López",     "2024-03-08", 1200.0, "TARJETA",       "Ana R."),
)

@Preview(showBackground = true, showSystemUi = true, name = "Empleado — Cartera")
@Composable
fun PreviewEmpleadoCartera() {
    CasaPrestamoTheme {
        SupervisionScreen(prestamos = prestamosMock, tickets = ticketsMock, isAdmin = false)
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Admin — Solicitudes")
@Composable
fun PreviewAdminSolicitudes() {
    CasaPrestamoTheme {
        SupervisionScreen(prestamos = prestamosMock, tickets = ticketsMock, isAdmin = true)
    }
}