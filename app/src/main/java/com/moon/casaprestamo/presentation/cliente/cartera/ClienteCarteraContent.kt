package com.moon.casaprestamo.presentation.cliente.cartera

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moon.casaprestamo.data.models.CarteraUiState
import com.moon.casaprestamo.data.models.PagoData
import com.moon.casaprestamo.data.models.PrestamoConPagos

@Composable
fun ClienteCarteraContent(
    uiState: CarteraUiState,
    onRetry: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var expandedPrestamoId by rememberSaveable { mutableStateOf<Int?>(null) }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colorScheme.primary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SummaryCard("CAPITAL", "$${String.format("%,.0f", uiState.capitalOtorgado)}", Icons.Default.AttachMoney, Color(0xFF2563EB))
                }
                item {
                    SummaryCard("LIQUIDADO", "$${String.format("%,.0f", uiState.montoLiquidado)}", Icons.Default.TrendingUp, Color(0xFF10B981))
                }
                item {
                    SummaryCard("PENDIENTE", "$${String.format("%,.0f", uiState.saldoPendiente)}", Icons.Default.PriorityHigh, colorScheme.primary)
                }
            }
        }

        item {
            Text(
                "HISTORIAL DE PRÉSTAMOS",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }

        items(uiState.prestamosConPagos, key = { it.prestamo.idPrestamo }) { prestamoConPagos ->
            val prestamo = prestamoConPagos.prestamo
            val isExpanded = expandedPrestamoId == prestamo.idPrestamo

            PrestamoHistorialCard(
                prestamoConPagos = prestamoConPagos,
                isExpanded = isExpanded,
                onToggle = {
                    expandedPrestamoId = if (isExpanded) null else prestamo.idPrestamo
                }
            )
        }

        if (uiState.prestamosConPagos.isEmpty()) {
            item {
                Text(
                    text = "No hay préstamos registrados.",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PrestamoHistorialCard(
    prestamoConPagos: PrestamoConPagos,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val prestamo = prestamoConPagos.prestamo
    val estado = prestamo.estado.uppercase()
    val pagado = (prestamo.montoTotal - prestamo.saldoPendiente).coerceAtLeast(0.0)
    val pendiente = prestamo.saldoPendiente.coerceAtLeast(0.0)
    val progreso = if (prestamo.montoTotal > 0.0) (pagado / prestamo.montoTotal).coerceIn(0.0, 1.0) else 0.0

    val estadoColor = when (estado) {
        "ACTIVO" -> Color(0xFF10B981)
        "MORA", "MOROSO" -> Color(0xFFF59E0B)
        "LIQUIDADO" -> Color(0xFF64748B)
        "RECHAZADO" -> Color(0xFF94A3B8)
        else -> Color(0xFF94A3B8)
    }

    Card(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(prestamo.folio ?: "MSP-${prestamo.idPrestamo}", fontWeight = FontWeight.Black)
                Surface(color = estadoColor, shape = RoundedCornerShape(20.dp)) {
                    Text(estado, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Monto solicitado: $${String.format("%,.0f", prestamo.montoTotal)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { progreso.toFloat() }, modifier = Modifier.fillMaxWidth().height(8.dp), color = Color(0xFF10B981), trackColor = Color(0xFFE2E8F0))
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Pagado: $${String.format("%,.0f", pagado)}", fontSize = 12.sp)
                Text("Pendiente: $${String.format("%,.0f", pendiente)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            if (isExpanded) {
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    TableHeaderText("NO.", Modifier.width(30.dp))
                    TableHeaderText("FECHA", Modifier.weight(1f))
                    TableHeaderText("MONTO", Modifier.weight(1f))
                    TableHeaderText("ESTADO", Modifier.width(90.dp), TextAlign.End)
                }
                prestamoConPagos.pagos.forEach { pago ->
                    PagoRowPrototipo(
                        pago = pago,
                        mostrarBotonPagar = estado in setOf("ACTIVO", "MORA", "MOROSO")
                    )
                }
                if (prestamoConPagos.pagos.isEmpty()) {
                    Text(
                        text = "No hay calendario disponible para este préstamo.",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun TableHeaderText(text: String, modifier: Modifier, align: TextAlign = TextAlign.Start) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        color = Color(0xFF94A3B8),
        textAlign = align
    )
}

@Composable
fun SummaryCard(label: String, value: String, icon: ImageVector, iconColor: Color) {
    Surface(
        modifier = Modifier.width(180.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(32.dp).background(iconColor.copy(0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun SpecificCard(label: String, value: String, containerColor: Color, contentColor: Color, modifier: Modifier, isBordered: Boolean = false) {
    Surface(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = if (isBordered) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.Center) {
            Text(label, fontSize = 8.sp, fontWeight = FontWeight.Black, color = contentColor.copy(0.7f))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = contentColor)
        }
    }
}

@Composable
fun HeaderCredito(folio: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Icon(Icons.Default.CalendarToday, null, tint = Color(0xFFA6032F), modifier = Modifier.padding(10.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("FOLIO: $folio", fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }
        Surface(color = Color(0xFF10B981), shape = RoundedCornerShape(20.dp)) {
            Text(
                "ACTIVO",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White
            )
        }
    }
}

@Composable
fun PagoRowPrototipo(pago: PagoData, mostrarBotonPagar: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(pago.numeroPago.toString(), modifier = Modifier.width(30.dp), fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
        Text(pago.fechaVencimiento, modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Black)
        Text("$${String.format("%,.0f", pago.monto)}", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Black)

        val (statusColor, label) = when (pago.estado.lowercase()) {
            "pagado" -> Color(0xFF10B981) to "PAGADO"
            "atrasado" -> Color(0xFFEF4444) to "ATRASADO"
            else -> Color(0xFF64748B) to "PENDIENTE"
        }

        if (mostrarBotonPagar && !pago.estado.equals("pagado", ignoreCase = true)) {
            OutlinedButton(
                onClick = { },
                modifier = Modifier.height(28.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("PAGAR", fontSize = 8.sp, fontWeight = FontWeight.Black)
            }
        } else {
            Surface(color = statusColor, shape = RoundedCornerShape(8.dp)) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Black
                )
            }
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF1F5F9))
}
