package com.moon.casaprestamo.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.moon.casaprestamo.presentation.admin.supervision.*


@Composable
fun TabCartera(
    uiState: SupervisionUiState,
    onSwitch: () -> Unit,
    onDetalle: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }

    // FIX 5: filtro por rango de fechas además del query de texto
    val filtrados = uiState.cartera.filter { item ->
        val pasaQuery = query.isBlank() || listOf(item.nombreCliente, item.curp, item.folio)
            .joinToString(" ").contains(query, ignoreCase = true)

        val pasaFechas = run {
            val desde = uiState.fechaDesde.let { if (it.isBlank()) null else fechaStringAMillis(displayAApiDate(it)) }
            val hasta = uiState.fechaHasta.let { if (it.isBlank()) null else fechaStringAMillis(displayAApiDate(it))?.plus(86_400_000L) }
            val fechaItem = fechaStringAMillis(item.fechaAprobacion)
            when {
                desde == null && hasta == null -> true
                fechaItem == null -> true
                desde != null && hasta != null -> fechaItem in desde..hasta
                desde != null -> fechaItem >= desde
                hasta != null -> fechaItem <= hasta
                else -> true
            }
        }
        pasaQuery && pasaFechas
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(Modifier.size(44.dp), RoundedCornerShape(14.dp), Oscuro) {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.padding(10.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("CARTERA ACTIVA", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text("SUPERVISIÓN DE CRÉDITOS VIGENTES", fontSize = 9.sp, letterSpacing = 0.8.sp, color = MaterialTheme.colorScheme.outline)
                }
                IconButton(onClick = onSwitch) {
                    Icon(Icons.Default.SwapHoriz, "Folios")
                }
            }

            OutlinedTextField(
                value = query, onValueChange = { query = it },
                placeholder = { Text("Filtrar resultados por nombre o ID...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                singleLine = true, shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedBorderColor    = Color.Transparent, focusedBorderColor = Rojo
                )
            )

            Spacer(Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.carteraLoading -> LoadBox()
                    filtrados.isEmpty()    -> EmptyBox("Sin registros activos")
                    else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                        stickyHeader {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // FIX 3: columnas correctas con espacio entre ellas
                                TH("CLIENTE",  Modifier.weight(1.4f))
                                Spacer(Modifier.width(8.dp))
                                TH("FECHA",    Modifier.weight(0.8f))
                                Spacer(Modifier.width(8.dp))
                                TH("MONTO",    Modifier.weight(0.9f))
                                Spacer(Modifier.width(8.dp))
                                TH("ESTADO",   Modifier.weight(0.9f))
                            }
                            HorizontalDivider()
                        }
                        items(filtrados, key = { it.idPrestamo }) { item ->
                            CarteraRow(
                                item = item,
                                onDetalle = { onDetalle(item.folio) })
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                        }
                    }
                }
            }
        }
    }
}

// FIX 3: CarteraRow con columnas corregidas
@Composable
private fun CarteraRow(item: CarteraAdminItem, onDetalle: () -> Unit) {
    // Nombre en líneas separadas para que no se comprima
    val nombreLinea1 = "${item.nombreCliente.split(" ").take(2).joinToString(" ")}"
    val nombreLinea2 = item.nombreCliente.split(" ").drop(2).joinToString(" ")

    // Fecha con salto antes del último guion
    val fechaRaw = item.fechaAprobacion.take(10)
    val fechaFormateada = if (fechaRaw.contains("-")) {
        val last = fechaRaw.lastIndexOf("-")
        fechaRaw.substring(0, last) + "\n" + fechaRaw.substring(last)
    } else fechaRaw

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetalle() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna CLIENTE: nombre + folio
        Column(modifier = Modifier.weight(1.4f)) {
            Text(
                nombreLinea1,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 14.sp
            )
            if (nombreLinea2.isNotBlank()) {
                Text(nombreLinea2, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 14.sp)
            }
            Text(
                item.folio,
                fontSize = 10.sp,
                color = Rojo,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.width(8.dp))

        // Columna FECHA
        Text(
            fechaFormateada,
            modifier = Modifier.weight(0.8f),
            fontSize = 11.sp,
            lineHeight = 12.sp,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(Modifier.width(8.dp))

        // Columna MONTO
        Text(
            "\$${String.format("%,.0f", item.montoTotal)}",
            modifier = Modifier.weight(0.9f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(Modifier.width(8.dp))

        // FIX 3: ESTADO real desde railway (ACTIVO / MORA / LIQUIDADO)
        Box(modifier = Modifier.weight(0.9f)) {
            val (bgColor, textColor, label) = when (item.estado.uppercase()) {
                "ACTIVO"    -> Triple(Verde.copy(0.12f),   Verde,   "ACTIVO")
                "MORA"      -> Triple(Rojo.copy(0.12f),    Rojo,    "EN MORA")
                "LIQUIDADO" -> Triple(Color(0xFF3B82F6).copy(0.12f), Color(0xFF3B82F6), "LIQUIDADO")
                else        -> Triple(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.colorScheme.outline, item.estado.uppercase())
            }
            Surface(color = bgColor, shape = RoundedCornerShape(50)) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    maxLines = 1
                )
            }
        }
    }
}
