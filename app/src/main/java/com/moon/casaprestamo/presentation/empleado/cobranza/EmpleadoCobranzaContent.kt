package com.moon.casaprestamo.presentation.empleado.cobranza

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moon.casaprestamo.data.models.PagoPendiente

private val Rojo = Color(0xFFA6032F)
private val Oscuro = Color(0xFF0F172A)
private val Verde = Color(0xFF10B981)
private val Amarillo = Color(0xFFF59E0B)

@Composable
fun EmpleadoCobranzaContent(
    uiState: CobranzaUiState,
    idEmpleado: Int,
    onRegistrarPago: (Int, Int, String) -> Unit,
    onLiquidarTodo: (Int, Int, String) -> Unit,   // ✅ PUNTO 3: nuevo callback
    onLimpiarMensaje: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pagoSeleccionado by remember { mutableStateOf<PagoPendiente?>(null) }
    var prestamoALiquidar by remember { mutableStateOf<PagoPendiente?>(null) }  // ✅ PUNTO 3
    var metodoPago by remember { mutableStateOf("EFECTIVO") }

    fun totalPendientePrestamo(idPrestamo: Int): Double =
        uiState.pagosPendientes
            .filter { it.id_prestamo == idPrestamo }
            .sumOf { it.monto }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "COBRANZA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    ),
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Pagos Pendientes",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black
                    )
                )
            }

            if (uiState.pagosPendientes.isNotEmpty()) {
                Surface(
                    color = Verde.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Verde
                        )
                        Text(
                            "${uiState.pagosPendientes.size} PAGOS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            ),
                            color = Verde
                        )
                    }
                }
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Rojo)
            }
        } else if (uiState.pagosPendientes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = Verde.copy(alpha = 0.3f)
                    )
                    Text(
                        "No hay pagos pendientes",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            // ✅ PUNTO 3: agrupamos por préstamo para saber cuántos pagos pendientes tiene cada uno
            val pagosPorPrestamo = uiState.pagosPendientes.groupBy { it.id_prestamo }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.pagosPendientes) { pago ->
                    val totalPendientesDelPrestamo = pagosPorPrestamo[pago.id_prestamo]?.size ?: 1
                    PagoCard(
                        pago = pago,
                        mostrarLiquidarTodo = totalPendientesDelPrestamo > 1,  // ✅ PUNTO 3
                        onSeleccionar = { pagoSeleccionado = pago },
                        onLiquidarTodo = { prestamoALiquidar = pago }          // ✅ PUNTO 3
                    )
                }
            }
        }

        uiState.mensaje?.let { mensaje ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (mensaje.contains("✅")) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (mensaje.contains("✅")) Icons.Default.CheckCircle else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (mensaje.contains("✅")) Verde else Rojo
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            mensaje,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (mensaje.contains("✅")) Verde else Rojo
                        )
                        uiState.ultimoFolio?.let { folio ->
                            Text(
                                "Folio: $folio",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    IconButton(onClick = onLimpiarMensaje) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            }
        }
    }

    // Dialog pago individual
    if (pagoSeleccionado != null) {
        ConfirmarPagoDialog(
            pago = pagoSeleccionado!!,
            metodoPago = metodoPago,
            onMetodoChange = { metodoPago = it },
            onConfirmar = {
                onRegistrarPago(pagoSeleccionado!!.id_pago, idEmpleado, metodoPago)
                pagoSeleccionado = null
            },
            onDismiss = { pagoSeleccionado = null }
        )
    }

    // ✅ PUNTO 3: Dialog liquidación total
    if (prestamoALiquidar != null) {
        ConfirmarLiquidacionDialog(
            pago = prestamoALiquidar!!,
            metodoPago = metodoPago,
            onMetodoChange = { metodoPago = it },
            onConfirmar = {
                onLiquidarTodo(prestamoALiquidar!!.id_prestamo, idEmpleado, metodoPago)
                prestamoALiquidar = null
            },
            onDismiss = { prestamoALiquidar = null }
        )
    }
}

@Composable
private fun PagoCard(
    pago: PagoPendiente,
    mostrarLiquidarTodo: Boolean,      // ✅ PUNTO 3
    onSeleccionar: () -> Unit,
    onLiquidarTodo: () -> Unit         // ✅ PUNTO 3
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        pago.nombreClienteUi.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Préstamo #${pago.id_prestamo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Surface(
                    color = Verde.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "PAGO #${pago.numero_pago}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp
                        ),
                        color = Verde
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "MONTO CUOTA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black, fontSize = 9.sp
                        ),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        "$${String.format("%,.2f", pago.monto)}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "SALDO TOTAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black, fontSize = 9.sp
                        ),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
"$${String.format("%,.2f", totalPendientePrestamo(pago.id_prestamo))}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Rojo
                    )
                }
            }

            Text(
                "Vence: ${pago.fecha_vencimiento}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            // Botón pago individual
            Button(
                onClick = onSeleccionar,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Oscuro)
            ) {
                Icon(Icons.Default.Payments, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    "REGISTRAR PAGO",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                )
            }

            // ✅ PUNTO 3: botón liquidar todo — solo si hay más de 1 pago pendiente en el préstamo
            if (mostrarLiquidarTodo) {
                OutlinedButton(
                    onClick = onLiquidarTodo,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Amarillo),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Amarillo)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
"LIQUIDAR TODO ($${String.format("%,.2f", totalPendientePrestamo(pago.id_prestamo))})",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmarPagoDialog(
    pago: PagoPendiente,
    metodoPago: String,
    onMetodoChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Confirmar Pago",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Cliente: ${pago.nombreClienteUi}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Monto: $${String.format("%,.2f", pago.monto)}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "MÉTODO DE PAGO:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.outline
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("EFECTIVO", "TRANSFERENCIA", "TARJETA").forEach { metodo ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = metodoPago == metodo, onClick = { onMetodoChange(metodo) })
                            Text(metodo)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = Verde)
            ) { Text("CONFIRMAR") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}

// ✅ PUNTO 3: Dialog para liquidación total — muestra el saldo completo
@Composable
private fun ConfirmarLiquidacionDialog(
    pago: PagoPendiente,
    totalPendiente: Double,
    metodoPago: String,
    onMetodoChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Liquidar Préstamo Completo",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    color = Amarillo.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Amarillo)
                        Text(
                            "Se registrarán todos los pagos pendientes del préstamo #${pago.id_prestamo}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Text("Cliente: ${pago.nombreClienteUi}", style = MaterialTheme.typography.bodyMedium)
                Text(
"Saldo total a liquidar: $${String.format("%,.2f", totalPendiente)}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Amarillo
                    )
                )
                Text(
                    "MÉTODO DE PAGO:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.outline
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("EFECTIVO", "TRANSFERENCIA", "TARJETA").forEach { metodo ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = metodoPago == metodo, onClick = { onMetodoChange(metodo) })
                            Text(metodo)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = Amarillo)
            ) { Text("LIQUIDAR TODO", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}