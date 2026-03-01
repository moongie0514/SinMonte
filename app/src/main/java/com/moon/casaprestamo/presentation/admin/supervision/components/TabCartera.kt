package com.moon.casaprestamo.presentation.admin.supervision.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import com.moon.casaprestamo.presentation.admin.supervision.CarteraAdminItem

@Composable
internal fun TabCartera(
    cartera: List<CarteraAdminItem>,
    isLoading: Boolean,
    fechaDesde: String,
    fechaHasta: String,
    onSwitch: () -> Unit,
    onDetalle: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }

    val filtrados by remember(cartera, fechaDesde, fechaHasta, query) {
        derivedStateOf {
            cartera.filter { item ->
                val pasaQuery = query.isBlank() || listOf(item.nombreCliente, item.curp, item.folio).joinToString(" ").contains(query, true)
                val desde = fechaDesde.takeIf { it.isNotBlank() }?.let { fechaStringAMillis(displayAApiDate(it)) }
                val hasta = fechaHasta.takeIf { it.isNotBlank() }?.let { fechaStringAMillis(displayAApiDate(it))?.plus(86_400_000L) }
                val fechaItem = fechaStringAMillis(item.fechaAprobacion)
                val pasaFechas = when {
                    desde == null && hasta == null -> true
                    fechaItem == null -> true
                    desde != null && hasta != null -> fechaItem in desde..hasta
                    desde != null -> fechaItem >= desde
                    else -> fechaItem <= hasta!!
                }
                pasaQuery && pasaFechas
            }
        }
    }

    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            HeaderTablaSupervision(
                titulo = "CARTERA ACTIVA",
                subtitulo = "SUPERVISIÓN DE CRÉDITOS VIGENTES",
                color = Oscuro,
                icono = { Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.padding(10.dp)) },
                onSwitch = onSwitch,
                switchIcon = { Icon(Icons.Default.SwapHoriz, "Folios") }
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
                    filtrados.isEmpty() -> EmptyBox("Sin registros activos")
                    else -> LazyColumn(Modifier.fillMaxSize()) {
                        stickyHeader {
                            Row(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TH("CLIENTE", Modifier.weight(1.4f)); Spacer(Modifier.width(8.dp))
                                TH("FECHA", Modifier.weight(0.8f)); Spacer(Modifier.width(8.dp))
                                TH("MONTO", Modifier.weight(0.9f)); Spacer(Modifier.width(8.dp))
                                TH("ESTADO", Modifier.weight(0.9f))
                            }
                            HorizontalDivider()
                        }
                        items(filtrados, key = { it.idPrestamo }) { item ->
                            CarteraRow(item = item, onDetalle = { onDetalle(item.folio) })
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CarteraRow(item: CarteraAdminItem, onDetalle: () -> Unit) {
    val nombreLinea1 = item.nombreCliente.split(" ").take(2).joinToString(" ")
    val nombreLinea2 = item.nombreCliente.split(" ").drop(2).joinToString(" ")
    val fechaRaw = item.fechaAprobacion.take(10)
    val fechaFormateada = fechaRaw.lastIndexOf("-").takeIf { it > 0 }?.let { fechaRaw.substring(0, it) + "\n" + fechaRaw.substring(it) } ?: fechaRaw

    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onDetalle).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1.4f)) {
            Text(nombreLinea1, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 14.sp)
            if (nombreLinea2.isNotBlank()) Text(nombreLinea2, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 14.sp)
            Text(item.folio, fontSize = 10.sp, color = Rojo, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        Spacer(Modifier.width(8.dp))
        Text(fechaFormateada, Modifier.weight(0.8f), fontSize = 11.sp, lineHeight = 12.sp, color = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.width(8.dp))
        Text("\$${String.format("%,.0f", item.montoTotal)}", Modifier.weight(0.9f), fontSize = 11.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(0.9f)) {
            val (label, color) = when (item.estado.uppercase()) {
                "ACTIVO" -> "ACTIVO" to Verde
                "MORA" -> "EN MORA" to Rojo
                "LIQUIDADO" -> "LIQUIDADO" to Color(0xFF3B82F6)
                else -> item.estado.uppercase() to MaterialTheme.colorScheme.outline
            }
            EstadoGestionChip(label, color)
        }
    }
}
