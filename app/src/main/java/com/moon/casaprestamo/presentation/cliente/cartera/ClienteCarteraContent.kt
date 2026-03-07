package com.moon.casaprestamo.presentation.cliente.cartera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moon.casaprestamo.data.models.CarteraUiState
import com.moon.casaprestamo.data.models.PagoData
import com.moon.casaprestamo.data.models.PrestamoConPagos

@Composable
fun ClienteCarteraContent(
    uiState:           CarteraUiState,
    onPagar:           (idPago: Int) -> Unit,
    onToggleExpansion: (idPrestamo: Int) -> Unit,
    onLimpiarMensaje:  () -> Unit
) {
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensajePago) {
        uiState.mensajePago?.let {
            snackbarState.showSnackbar(it)
            onLimpiarMensaje()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarState) }) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                return@Scaffold
            }

            LazyColumn(
                modifier       = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {

                // ── SECCIÓN 1: CARDS RESUMEN GENERAL ────────────────
                item {
                    LazyRow(
                        contentPadding = PaddingValues(top = 0.dp, bottom = 16.dp, start = 0.dp, end = 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { SummaryCard("CAPITAL",   "$${String.format("%,.0f", uiState.capitalOtorgado)}", Icons.Default.AttachMoney,  Color(0xFF2563EB)) }
                        item { SummaryCard("PAGADO",    "$${String.format("%,.0f", uiState.montoLiquidado)}",
                            Icons.AutoMirrored.Filled.TrendingUp,   Color(0xFF10B981)) }
                        item { SummaryCard("PENDIENTE", "$${String.format("%,.0f", uiState.saldoPendiente)}",  Icons.Default.PriorityHigh, MaterialTheme.colorScheme.primary) }
                    }
                }

                // ── SECCIÓN 2: ENCABEZADO HISTORIAL ─────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            "MIS PRÉSTAMOS",
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "${uiState.prestamosConPagos.size} crédito${if (uiState.prestamosConPagos.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // ── SECCIÓN 3: LISTA DE PRÉSTAMOS ────────────────────
                if (uiState.prestamosConPagos.isEmpty()) {
                    item {
                        Box(
                            modifier          = Modifier.fillMaxWidth().padding(48.dp),
                            contentAlignment  = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint     = MaterialTheme.colorScheme.outline
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "No tienes préstamos activos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = uiState.prestamosConPagos,
                        key   = { it.prestamo.idPrestamo }
                    ) { prestamoConPagos ->
                        val expandido = uiState.prestamosExpandidos.contains(prestamoConPagos.prestamo.idPrestamo)
                        PrestamoCard(
                            prestamoConPagos = prestamoConPagos,
                            expandido        = expandido,
                            pagoEnProceso    = uiState.pagoEnProceso,
                            onToggle         = { onToggleExpansion(prestamoConPagos.prestamo.idPrestamo) },
                            onPagar          = onPagar,
                            modifier         = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Tarjeta de préstamo expandible ───────────────────────────────
@Composable
fun PrestamoCard(
    prestamoConPagos: PrestamoConPagos,
    expandido:        Boolean,
    pagoEnProceso:    Int?,
    onToggle:         () -> Unit,
    onPagar:          (idPago: Int) -> Unit,
    modifier:         Modifier = Modifier
) {
    val prestamo   = prestamoConPagos.prestamo
    val pagos      = prestamoConPagos.pagos
    val esVigente  = prestamo.estado.equals("ACTIVO", ignoreCase = true) ||
            prestamo.estado.equals("MORA",   ignoreCase = true) ||
            prestamo.estado.equals("MOROSO", ignoreCase = true)

    // Calcular progreso de este préstamo específico
    val pagados      = pagos.count { it.estado.equals("pagado", ignoreCase = true) }
    val totalPagos   = pagos.size
    val progreso     = if (totalPagos > 0) pagados.toFloat() / totalPagos.toFloat() else 0f
    val montoPagado  = pagos.filter { it.estado.equals("pagado", ignoreCase = true) }.sumOf { it.monto }

    val (estadoColor, estadoLabel) = when (prestamo.estado.uppercase()) {
        "ACTIVO"          -> Color(0xFFF59E0B) to "ACTIVO"
        "MORA", "MOROSO"  -> Color(0xFFEF4444) to "EN MORA"
        "LIQUIDADO"       -> Color(0xFF10B981) to "LIQUIDADO"
        else              -> Color(0xFF64748B) to prestamo.estado
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // ── Header clickeable ─────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ícono de estado
                Box(
                    modifier         = Modifier
                        .size(40.dp)
                        .background(estadoColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (prestamo.estado.equals("LIQUIDADO", ignoreCase = true))
                            Icons.Default.CheckCircle
                        else
                            Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint     = estadoColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Info central
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            prestamo.folio ?: "MSP-${prestamo.idPrestamo}",
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black
                        )
                        Surface(
                            color = estadoColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                estadoLabel,
                                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize   = 8.sp,
                                fontWeight = FontWeight.Black,
                                color      = estadoColor
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Capital: $${String.format("%,.0f", prestamo.montoTotal)}  •  ${prestamo.plazoMeses} meses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(8.dp))

                    // Barra de progreso
                    if (totalPagos > 0) {
                        LinearProgressIndicator(
                            progress        = { progreso },
                            modifier        = Modifier.fillMaxWidth().height(4.dp),
                            color           = estadoColor,
                            trackColor      = estadoColor.copy(alpha = 0.15f),
                            strokeCap       = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$pagados de $totalPagos pagos  •  Pagado: $${String.format("%,.0f", montoPagado)}",
                            fontSize = 9.sp,
                            color    = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Flecha expandir/colapsar
                Icon(
                    if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expandido) "Colapsar" else "Expandir",
                    tint     = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }

            // ── Calendario de pagos (animado) ─────────────────────
            AnimatedVisibility(
                visible = expandido,
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )

                    // Encabezado tabla
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("NO.",   modifier = Modifier.width(30.dp),  fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                        Text("FECHA", modifier = Modifier.weight(1f),    fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                        Text("MONTO", modifier = Modifier.weight(1f),    fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                        Text("",      modifier = Modifier.width(80.dp),  fontSize = 9.sp)
                    }

                    if (pagos.isEmpty()) {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    } else {
                        pagos.forEach { pago ->
                            PagoRowCliente(
                                pago       = pago,
                                enProceso  = pagoEnProceso == pago.idPago,
                                puedePagar = esVigente,
                                onPagar    = { onPagar(pago.idPago) },
                                modifier   = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ── Fila de pago ─────────────────────────────────────────────────
@Composable
fun PagoRowCliente(
    pago:       PagoData,
    enProceso:  Boolean,
    puedePagar: Boolean,
    onPagar:    () -> Unit,
    modifier:   Modifier = Modifier
) {
    Row(
        modifier          = modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(pago.numeroPago.toString(), modifier = Modifier.width(30.dp), fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
        Text(pago.fechaVencimiento ?: "", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Black)
        Text("$${String.format("%,.0f", pago.monto)}", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Black)

        Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.CenterEnd) {
            when {
                pago.estado.equals("pagado", ignoreCase = true) -> {
                    Surface(color = Color(0xFF0F172A), shape = RoundedCornerShape(8.dp)) {
                        Text("PAGADO", modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
                !puedePagar -> {
                    // Préstamo liquidado — no mostrar botón
                    Surface(color = Color(0xFF6366F1), shape = RoundedCornerShape(8.dp)) {
                        Text("CERRADO", modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
                enProceso -> {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color       = if (pago.estado.equals("atrasado", ignoreCase = true)) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                    )
                }
                else -> {
                    if (pago.esPagable) {
                        val btnColor = if (pago.estado.equals("atrasado", ignoreCase = true))
                            Color(0xFFEF4444) else Color(0xFF10B981)
                        Button(
                            onClick        = onPagar,
                            colors         = ButtonDefaults.buttonColors(containerColor = btnColor),
                            shape          = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier       = Modifier.height(28.dp)
                        ) {
                            Text("PAGAR", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    } else {
                        Surface(color = Color(0xFFCBD5E1), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                "PAGAR",
                                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                fontSize   = 8.sp,
                                color      = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF1F5F9))
}

// ── Composables auxiliares ────────────────────────────────────────

@Composable
fun SummaryCard(label: String, value: String, icon: ImageVector, iconColor: Color) {
    Surface(
        modifier = Modifier.width(180.dp),
        shape    = RoundedCornerShape(20.dp),
        color    = MaterialTheme.colorScheme.surface,
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.size(32.dp).background(iconColor.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
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
    Surface(modifier = modifier.height(80.dp), shape = RoundedCornerShape(16.dp), color = containerColor, border = if (isBordered) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.Center) {
            Text(label, fontSize = 8.sp, fontWeight = FontWeight.Black, color = contentColor.copy(0.7f))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = contentColor)
        }
    }
}


// PagoRowPrototipo — se mantiene para no romper referencias del módulo de supervisión
@Composable
fun PagoRowPrototipo(pago: PagoData) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(pago.numeroPago.toString(), modifier = Modifier.width(30.dp), fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
        Text(pago.fechaVencimiento ?: "", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Black)
        Text("$${String.format("%,.0f", pago.monto)}", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Black)
        val (statusColor, label) = when {
            pago.estado.lowercase() == "pagado"   -> Color(0xFF10B981) to "PAGADO"
            pago.estado.lowercase() == "atrasado" -> Color(0xFFEF4444) to "ATRASADO"
            pago.esPagable                         -> Color(0xFF3B82F6) to "PAGAR"      // siguiente habilitado — azul
            else                                   -> Color(0xFFCBD5E1) to "BLOQUEADO"  // los demás — gris claro
        }
        Surface(color = statusColor, shape = RoundedCornerShape(8.dp)) {
            Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Black)
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF1F5F9))
}