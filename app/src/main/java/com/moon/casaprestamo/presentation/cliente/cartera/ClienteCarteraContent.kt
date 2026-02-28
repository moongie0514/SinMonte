package com.moon.casaprestamo.presentation.cliente.cartera

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun ClienteCarteraContent(
    uiState: CarteraUiState,
    onRetry: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    // 1. Extraemos los datos una sola vez para asegurar consistencia
    val prestamoActual = uiState.prestamosConPagos.firstOrNull {
        it.prestamo.estado.equals("ACTIVO", ignoreCase = true) ||
            it.prestamo.estado.equals("MORA", ignoreCase = true)
    } ?: uiState.prestamosConPagos.firstOrNull()
    val pagosVigentes = prestamoActual?.pagos ?: emptyList()

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colorScheme.primary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background), // BackgroundLight
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // --- SECCIÓN 1: RESUMEN GENERAL ---
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

        // --- SECCIÓN 2: CRÉDITO VIGENTE ---
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                HeaderCredito(
                    folio = "P-${prestamoActual?.prestamo?.idPrestamo ?: "0"}"
                )

                Spacer(Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = colorScheme.surface,
                    border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // AQUÍ VINCULAMOS LOS VALORES REALES DEL UISTATE
                        SpecificCard(
                            label = "SALDO",
                            value = "$${String.format("%,.0f", uiState.saldoPendiente)}",
                            containerColor = Color(0xFF0F172A),
                            contentColor = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        SpecificCard(
                            label = "PAGADO",
                            value = "$${String.format("%,.0f", uiState.montoLiquidado)}",
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- SECCIÓN 3: ENCABEZADO DE TABLA ---
        item {
            Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 8.dp)) {
                Text("CALENDARIO DE PAGOS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    TableHeaderText("NO.", Modifier.width(30.dp))
                    TableHeaderText("FECHA", Modifier.weight(1f))
                    TableHeaderText("MONTO", Modifier.weight(1f))
                    TableHeaderText("ESTADO", Modifier.width(75.dp), TextAlign.End)
                }
            }
        }

        // --- SECCIÓN 4: LOS PAGOS (SIN BUCLES FOR) ---
        // Usamos 'items' directamente del LazyColumn.
        // Esto evita que se repitan porque LazyColumn gestiona el índice de forma única.
        items(
            items = pagosVigentes,
            key = { it.idPago } // Esto evita que Compose se confunda
        ) { pago ->
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                PagoRowPrototipo(pago)
            }
        }
        if (pagosVigentes.isEmpty()) {
            item {
                Text(
                    text = "No hay calendario disponible para este préstamo.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
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
        color = Color(0xFF94A3B8), // OutlineLight
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
fun PagoRowPrototipo(pago: PagoData) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(pago.numeroPago.toString(), modifier = Modifier.width(30.dp), fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
        Text(pago.fechaVencimiento ?: "", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Black)
        Text("$${String.format("%,.0f", pago.monto)}", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Black)

        val (statusColor, label) = when(pago.estado.lowercase()) {
            "pagado" -> Color(0xFF10B981) to "PAGADO"
            "atrasado" -> Color(0xFFEF4444) to "ATRASADO"
            else -> Color(0xFF64748B) to "PENDIENTE"
        }

        Surface(color = statusColor, shape = RoundedCornerShape(8.dp)) {
            Text(
                label,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Black
            )
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF1F5F9))
}

