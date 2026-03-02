package com.moon.casaprestamo.ui.components.supervision

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moon.casaprestamo.data.models.TicketDetalle
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Pestaña Libro de Folios — registro cronológico de pagos.
 *
 * @param folios         Lista de tickets de pago ([TicketDetalle]).
 * @param fechaDesde     Fecha "desde" del filtro global (dd/MM/yyyy), puede vacía.
 * @param fechaHasta     Fecha "hasta" del filtro global (dd/MM/yyyy), puede vacía.
 * @param isLoading      Indica si la carga está en progreso.
 * @param onSwitch       Callback para volver a la pestaña de Cartera.
 * @param onCargarFolios Invocado al entrar para solicitar datos al ViewModel.
 * @param onClickTicket  Callback con el [TicketDetalle] completo al presionar una fila.
 */
@Composable
fun TabLibroFolios(
    folios:         List<TicketDetalle>,
    fechaDesde:     String,
    fechaHasta:     String,
    isLoading:      Boolean,
    onSwitch:       () -> Unit,
    onCargarFolios: (String?) -> Unit,
    onClickTicket:  (TicketDetalle) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }

    // Carga inicial sin filtro de fecha
    LaunchedEffect(Unit) { onCargarFolios(null) }

    // derivedStateOf evita re-composiciones innecesarias al escribir en el buscador
    val filtrados by remember(folios, query, fechaDesde, fechaHasta) {
        derivedStateOf {
            folios.filter { ticket ->
                val pasaQuery = query.isBlank() || listOf(
                    ticket.folio, ticket.folioPrestamo, ticket.nombre, ticket.apellidoPaterno
                ).joinToString(" ").contains(query, ignoreCase = true)

                val pasaFechas: Boolean = run {
                    val desdeMs: Long? = if (fechaDesde.isBlank()) null
                    else fechaStringAMillis(displayAApiDate(fechaDesde))
                    // +86400000 para incluir todo el día final; tipado explícito evita type mismatch
                    val hastaMs: Long? = if (fechaHasta.isBlank()) null
                    else fechaStringAMillis(displayAApiDate(fechaHasta))
                        ?.let { it + 86_400_000L }
                    val ticketMs: Long? = try {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .parse(ticket.fechaGeneracion.take(10))?.time
                    } catch (e: Exception) { null }

                    when {
                        desdeMs == null && hastaMs == null -> true
                        ticketMs == null                  -> true
                        desdeMs != null && hastaMs != null -> ticketMs in desdeMs..hastaMs
                        desdeMs != null                   -> ticketMs >= desdeMs
                        hastaMs != null                   -> ticketMs <= hastaMs
                        else                              -> true
                    }
                }
                pasaQuery && pasaFechas
            }
        }
    }

    Card(
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier  = Modifier.fillMaxWidth().heightIn(min = 380.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {

            // ── Encabezado tarjeta ────────────────────────────
            Row(
                modifier          = Modifier.padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(Modifier.size(44.dp), RoundedCornerShape(14.dp), Rojo) {
                    Icon(Icons.Default.Receipt, null, tint = Color.White, modifier = Modifier.padding(10.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("LIBRO DE FOLIOS", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text(
                        "REGISTRO CRONOLÓGICO DE PAGOS",
                        fontSize      = 9.sp,
                        letterSpacing = 0.8.sp,
                        color         = MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = onSwitch) {
                    Icon(Icons.Default.SwapHoriz, "Cartera")
                }
            }

            // ── Buscador ──────────────────────────────────────
            OutlinedTextField(
                value         = query,
                onValueChange = { query = it },
                placeholder   = { Text("Filtrar por nombre o ID…", fontSize = 13.sp) },
                leadingIcon   = { Icon(Icons.Default.Search, null, Modifier.size(18.dp)) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                singleLine    = true,
                shape         = RoundedCornerShape(50),
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedBorderColor    = Color.Transparent,
                    focusedBorderColor      = Rojo
                )
            )
            Spacer(Modifier.padding(top = 8.dp))

            // ── Tabla ─────────────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading           -> LoadBox()
                    filtrados.isEmpty() -> EmptyBox("Sin folios registrados")
                    else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                        stickyHeader {
                            HeaderTablaSupervision(
                                listOf(
                                    "CLIENTE"  to 1.4f,
                                    "FECHA"    to 0.8f,
                                    "IMPORTE"  to 0.9f,
                                    "MÉTODO"   to 0.9f
                                )
                            )
                            HorizontalDivider()
                        }
                        items(filtrados, key = { it.idTicket }) { ticket ->
                            FolioRow(
                                ticket       = ticket,
                                onClickTicket = { onClickTicket(ticket) }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Fila de Folio ────────────────────────────────────────

@Composable
private fun FolioRow(ticket: TicketDetalle, onClickTicket: () -> Unit) {
    val nombreCompleto = listOf(ticket.nombre, ticket.apellidoPaterno).joinToString(" ").trim()
    val partes         = nombreCompleto.split(" ")
    val nombreLinea1   = partes.take(2).joinToString(" ")
    val nombreLinea2   = partes.drop(2).joinToString(" ")

    val fechaRaw = ticket.fechaGeneracion.take(10)
    val fechaFormateada = if (fechaRaw.contains("-")) {
        val last = fechaRaw.lastIndexOf("-")
        fechaRaw.substring(0, last) + "\n" + fechaRaw.substring(last)
    } else fechaRaw

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickTicket() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.4f)) {
            Text(nombreLinea1, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 14.sp)
            if (nombreLinea2.isNotBlank())
                Text(nombreLinea2, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 14.sp)
            Text(
                ticket.folio,
                color      = Rojo,
                fontWeight = FontWeight.Bold,
                fontSize   = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            fechaFormateada,
            modifier   = Modifier.weight(0.8f),
            fontSize   = 11.sp,
            lineHeight = 12.sp,
            color      = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "\$${String.format("%,.0f", ticket.montoPagado)}",
            modifier   = Modifier.weight(0.9f),
            fontSize   = 11.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.width(8.dp))
        // Método de pago (EFECTIVO / TRANSFERENCIA / etc.)
        Text(
            ticket.metodoPago.uppercase().take(10),
            modifier   = Modifier.weight(0.9f),
            fontSize   = 9.sp,
            fontWeight = FontWeight.Black,
            color      = Verde,
            maxLines   = 1
        )
    }
}