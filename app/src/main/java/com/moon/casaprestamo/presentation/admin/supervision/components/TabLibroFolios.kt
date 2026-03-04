package com.moon.casaprestamo.presentation.admin.supervision.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
internal fun TabLibroFolios(
    movimientos: List<MovimientoDia>,
    isLoading: Boolean,
    fechaDesde: String,
    fechaHasta: String,
    onSwitch: () -> Unit,
    onCargarFolios: (String?) -> Unit,
    onClickFolio: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) { onCargarFolios(null) }

    val filtrados by remember(movimientos, fechaDesde, fechaHasta, query) {
        derivedStateOf {
            movimientos.filter { ticket ->
                val pasaQuery = query.isBlank() || listOf(ticket.folio, ticket.folioPrestamo, ticket.nombre, ticket.apellidoPaterno).joinToString(" ").contains(query, true)
                val desde = fechaDesde.takeIf { it.isNotBlank() }?.let { fechaStringAMillis(displayAApiDate(it)) }
                val hasta = fechaHasta.takeIf { it.isNotBlank() }?.let { fechaStringAMillis(displayAApiDate(it))?.plus(86_400_000L) }
                val fechaTicket = try { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(ticket.fechaGeneracion.take(10))?.time } catch (_: Exception) { null }
                val pasaFechas = when {
                    desde == null && hasta == null -> true
                    fechaTicket == null -> true
                    desde != null && hasta != null -> fechaTicket in desde..hasta
                    desde != null -> fechaTicket >= desde
                    else -> fechaTicket <= hasta!!
                }
                pasaQuery && pasaFechas
            }
        }
    }

    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 380.dp)) {
        Column(Modifier.fillMaxWidth()) {
            HeaderTablaSupervision(
                titulo = "LIBRO DE FOLIOS",
                subtitulo = "REGISTRO CRONOLÓGICO DE PAGOS",
                color = Rojo,
                icono = { Icon(Icons.Default.Receipt, null, tint = Color.White, modifier = Modifier.padding(10.dp)) },
                onSwitch = onSwitch,
                switchIcon = { Icon(Icons.Default.SwapHoriz, "Cartera") }
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Filtrar resultados por nombre o ID...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                singleLine = true,
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Rojo
                )
            )
            Spacer(Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading -> LoadBox()
                    filtrados.isEmpty() -> EmptyBox("Sin folios registrados")
                    else -> LazyColumn(Modifier.fillMaxSize()) {
                        stickyHeader {
                            Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                TH("CLIENTE", Modifier.weight(1.4f)); Spacer(Modifier.width(8.dp))
                                TH("FECHA", Modifier.weight(0.8f)); Spacer(Modifier.width(8.dp))
                                TH("IMPORTE", Modifier.weight(0.9f)); Spacer(Modifier.width(8.dp))
                                TH("ESTADO", Modifier.weight(0.9f))
                            }
                            HorizontalDivider()
                        }
                        items(filtrados, key = { it.idTicket }) { ticket ->
                            FolioRow(ticket = ticket, onClickFolio = { onClickFolio(ticket.folioPrestamo) })
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolioRow(ticket: MovimientoDia, onClickFolio: () -> Unit) {
    val nombreCompleto = listOf(ticket.nombre, ticket.apellidoPaterno).joinToString(" ").trim()
    val nombreLinea1 = nombreCompleto.split(" ").take(2).joinToString(" ")
    val nombreLinea2 = nombreCompleto.split(" ").drop(2).joinToString(" ")
    val fechaRaw = ticket.fechaGeneracion.take(10)
    val fechaFormateada = fechaRaw.lastIndexOf("-").takeIf { it > 0 }?.let { fechaRaw.substring(0, it) + "\n" + fechaRaw.substring(it) } ?: fechaRaw

    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClickFolio).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1.4f)) {
            Text(nombreLinea1, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 14.sp)
            if (nombreLinea2.isNotBlank()) Text(nombreLinea2, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 14.sp)
            Text(ticket.folio, color = Rojo, fontWeight = FontWeight.Bold, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
        Spacer(Modifier.width(8.dp))
        Text(fechaFormateada, Modifier.weight(0.8f), fontSize = 11.sp, lineHeight = 12.sp, color = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.width(8.dp))
        Text("\$${String.format("%,.0f", ticket.montoPagado)}", Modifier.weight(0.9f), fontSize = 11.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(0.9f)) { EstadoGestionChip("PAGADO", Rojo) }
    }
}
