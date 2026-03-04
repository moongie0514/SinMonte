package com.moon.casaprestamo.ui.components.supervision


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import com.moon.casaprestamo.presentation.admin.supervision.CarteraAdminItem

/**
 * Pestaña de Cartera Activa.
 *
 * @param cartera     Lista completa de créditos vigentes.
 * @param fechaDesde  Fecha "desde" del filtro global (dd/MM/yyyy), puede estar vacía.
 * @param fechaHasta  Fecha "hasta" del filtro global (dd/MM/yyyy), puede estar vacía.
 * @param isLoading   Indica si la carga está en progreso.
 * @param onSwitch    Callback para cambiar a la pestaña de Folios.
 * @param onDetalle   Callback con el folio del préstamo seleccionado.
 */
@Composable
fun TabCartera(
    cartera:    List<CarteraAdminItem>,
    fechaDesde: String,
    fechaHasta: String,
    isLoading:  Boolean,
    onSwitch:   () -> Unit,
    onDetalle:  (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }

    // derivedStateOf evita re-composiciones innecesarias al escribir en el buscador
    val filtrados by remember(cartera, query, fechaDesde, fechaHasta) {
        derivedStateOf {
            cartera.filter { item ->
                val pasaQuery = query.isBlank() || listOf(item.nombreCliente, item.curp, item.folio)
                    .joinToString(" ").contains(query, ignoreCase = true)

                val pasaFechas: Boolean = run {
                    val desdeMs: Long? = if (fechaDesde.isBlank()) null
                    else fechaStringAMillis(displayAApiDate(fechaDesde))
                    val hastaMs: Long? = if (fechaHasta.isBlank()) null
                    else fechaStringAMillis(displayAApiDate(fechaHasta))
                        ?.let { it + 86_400_000L }
                    val itemMs: Long?  = fechaStringAMillis(item.fechaAprobacion)
                    when {
                        desdeMs == null && hastaMs == null -> true
                        itemMs  == null                   -> true
                        desdeMs != null && hastaMs != null -> itemMs in desdeMs..hastaMs
                        desdeMs != null                   -> itemMs >= desdeMs
                        hastaMs != null                   -> itemMs <= hastaMs
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
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.fillMaxWidth()) {

            // ── Encabezado tarjeta ────────────────────────────
            Row(
                modifier          = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(Modifier.size(44.dp), RoundedCornerShape(14.dp), Oscuro) {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.padding(10.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("CARTERA ACTIVA", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text(
                        "SUPERVISIÓN DE CRÉDITOS VIGENTES",
                        fontSize      = 9.sp,
                        letterSpacing = 0.8.sp,
                        color         = MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = onSwitch) {
                    Icon(Icons.Default.SwapHoriz, "Folios")
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
                    filtrados.isEmpty() -> EmptyBox("Sin registros activos")
                    else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                        stickyHeader {
                            HeaderTablaSupervision(
                                listOf(
                                    "CLIENTE" to 1.4f,
                                    "FECHA"   to 0.8f,
                                    "MONTO"   to 0.9f,
                                    "ESTADO"  to 0.9f
                                )
                            )
                            HorizontalDivider()
                        }
                        items(filtrados, key = { it.idPrestamo }) { item ->
                            CarteraRow(item = item, onDetalle = { onDetalle(item.folio) })
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

// ─── Fila de Cartera ──────────────────────────────────────

@Composable
private fun CarteraRow(item: CarteraAdminItem, onDetalle: () -> Unit) {
    val partes       = item.nombreCliente.split(" ")
    val nombreLinea1 = partes.take(2).joinToString(" ")
    val nombreLinea2 = partes.drop(2).joinToString(" ")

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
        Column(modifier = Modifier.weight(1.4f)) {
            Text(nombreLinea1, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 14.sp)
            if (nombreLinea2.isNotBlank())
                Text(nombreLinea2, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 14.sp)
            Text(item.folio, fontSize = 10.sp, color = Rojo, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
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
            "\$${String.format("%,.0f", item.montoTotal)}",
            modifier   = Modifier.weight(0.9f),
            fontSize   = 11.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.weight(0.9f)) {
            EstadoGestionChip(item.estado)
        }
    }
}