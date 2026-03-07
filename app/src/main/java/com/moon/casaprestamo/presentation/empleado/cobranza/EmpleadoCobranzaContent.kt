package com.moon.casaprestamo.presentation.empleado.cobranza

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moon.casaprestamo.data.models.PagoData
import com.moon.casaprestamo.data.models.PagoPendiente
import com.moon.casaprestamo.presentation.cliente.cartera.PagoRowCliente

private val Rojo  = Color(0xFFA6032F)
private val Verde = Color(0xFF10B981)
private val Oscuro = Color(0xFF0F172A)

@Composable
fun EmpleadoCobranzaContent(
    uiState:          CobranzaUiState,
    idEmpleado:       Int,
    onRegistrarPago:  (Int, Int, String) -> Unit,
    onLiquidarTodo:   (Int, Int, String) -> Unit,
    onLimpiarMensaje: () -> Unit,
    modifier:         Modifier = Modifier
) {
    var prestamoExpandido  by remember { mutableStateOf<Int?>(null) }
    var pagoSeleccionado   by remember { mutableStateOf<PagoPendiente?>(null) }
    var prestamoALiquidar  by remember { mutableStateOf<PagoPendiente?>(null) }
    var metodoPago         by remember { mutableStateOf("EFECTIVO") }
    var query              by remember { mutableStateOf("") }

    // Agrupar pagos por préstamo para mostrar una fila por préstamo en la tabla
    val prestamosFiltrados = remember(uiState.pagosPendientes, query) {
        uiState.pagosPendientes
            .groupBy { it.id_prestamo }
            .values
            .map { it.first() } // representante de cada préstamo
            .filter {
                query.isBlank() ||
                        it.nombreClienteUi.contains(query, ignoreCase = true) ||
                        it.folio.orEmpty().contains(query, ignoreCase = true) ||
                        it.curp.orEmpty().contains(query, ignoreCase = true)
            }
    }

    fun pagosDelPrestamo(idPrestamo: Int) =
        uiState.pagosPendientes.filter { it.id_prestamo == idPrestamo }.sortedBy { it.numero_pago }

    fun totalPendiente(idPrestamo: Int) =
        uiState.pagosPendientes.filter { it.id_prestamo == idPrestamo }.sumOf { it.monto }

    // ── Dialogs ──────────────────────────────────────────────────
    if (pagoSeleccionado != null) {
        ConfirmarPagoDialog(
            pago           = pagoSeleccionado!!,
            metodoPago     = metodoPago,
            onMetodoChange = { metodoPago = it },
            onConfirmar    = {
                onRegistrarPago(pagoSeleccionado!!.id_pago, idEmpleado, metodoPago)
                pagoSeleccionado = null
            },
            onDismiss = { pagoSeleccionado = null }
        )
    }

    if (prestamoALiquidar != null) {
        ConfirmarLiquidacionDialog(
            pago           = prestamoALiquidar!!,
            totalPendiente = totalPendiente(prestamoALiquidar!!.id_prestamo),
            metodoPago     = metodoPago,
            onMetodoChange = { metodoPago = it },
            onConfirmar    = {
                onLiquidarTodo(prestamoALiquidar!!.id_prestamo, idEmpleado, metodoPago)
                prestamoALiquidar = null
            },
            onDismiss = { prestamoALiquidar = null }
        )
    }

    Column(
        modifier            = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Encabezado ───────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "PAGOS PENDIENTES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight    = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    ),
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(2.dp))
            }
            if (prestamosFiltrados.isNotEmpty()) {
                Surface(color = Verde.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp)) {
                    Row(
                        modifier              = Modifier.padding( vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Payments, null, Modifier.size(16.dp), tint = Verde)
                        Text(
                            "${prestamosFiltrados.size} CRÉDITOS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black, fontSize = 10.sp
                            ),
                            color = Verde
                        )
                    }
                }
            }
        }

        // ── Mensaje resultado ────────────────────────────────────
        uiState.mensaje?.let { msg ->
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                onLimpiarMensaje()
            }
            Surface(
                color    = if (msg.contains("✅")) Verde.copy(alpha = 0.12f) else Rojo.copy(alpha = 0.12f),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Text(
                    msg,
                    modifier   = Modifier.padding(12.dp),
                    color      = if (msg.contains("✅")) Verde else Rojo,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Rojo)
            }
            return@Column
        }

        if (prestamosFiltrados.isEmpty() && query.isBlank()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, Modifier.size(72.dp), tint = Verde.copy(alpha = 0.3f))
                    Text(
                        "No hay pagos pendientes",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            return@Column
        }

        // ── Tabla ────────────────────────────────────────────────
        Card(
            shape     = RoundedCornerShape(24.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.fillMaxWidth()) {

                // Buscador
                OutlinedTextField(
                    value         = query,
                    onValueChange = { query = it },
                    placeholder   = { Text("Buscar por CURP o Folio de préstamo…", fontSize = 13.sp) },
                    leadingIcon   = { Icon(Icons.Default.Search, null, Modifier.size(18.dp)) },
                    modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    singleLine    = true,
                    shape         = RoundedCornerShape(50),
                    colors        = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedBorderColor    = Color.Transparent,
                        focusedBorderColor      = Rojo
                    )
                )

                // Encabezado de columnas
                CobranzaTableHeader()
                HorizontalDivider()

                if (prestamosFiltrados.isEmpty()) {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Sin resultados para \"$query\"",
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 8.dp)) {
                        items(prestamosFiltrados, key = { it.id_prestamo }) { pago ->
                            val expandido = prestamoExpandido == pago.id_prestamo
                            val pagos     = pagosDelPrestamo(pago.id_prestamo)

                            // ── Fila de la tabla ──────────────────
                            CobranzaTableRow(
                                pago      = pago,
                                expandido = expandido,
                                onClick   = {
                                    prestamoExpandido = if (expandido) null else pago.id_prestamo
                                }
                            )

                            // ── Panel expandido con cabecera + calendario ──
                            AnimatedVisibility(
                                visible = expandido,
                                enter   = expandVertically(),
                                exit    = shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                ) {
                                    // Cabecera tipo imagen: CAJA DE COBRANZA + selector método
                                    PanelCabeceraCobranza(
                                        nombreCliente = pago.nombreClienteUi,
                                        metodoPago    = metodoPago,
                                        onMetodoChange = { metodoPago = it }
                                    )

                                    // Encabezado tabla de pagos
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text("NO.",   Modifier.width(30.dp),  fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                                        Text("FECHA", Modifier.weight(1f),    fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                                        Text("MONTO", Modifier.weight(1f),    fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                                        Text("",      Modifier.width(80.dp),  fontSize = 9.sp)
                                    }

                                    // Filas de pagos
                                    pagos.forEach { p ->
                                        // Convertir PagoPendiente → PagoData para reutilizar PagoRowCliente
                                        val pagoData = PagoData(
                                            idPago = p.id_pago,
                                            numeroPago = p.numero_pago,
                                            fechaVencimiento = p.fecha_vencimiento.take(10),
                                            fechaPago = null,
                                            monto = p.monto,
                                            estado = p.estado ?: "pendiente"                                        )
                                        PagoRowCliente(
                                            pago       = pagoData,
                                            enProceso  = false,
                                            puedePagar = true,
                                            onPagar    = { pagoSeleccionado = p },
                                            modifier   = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }

                                    // Botón liquidar todo
                                    if (pagos.size > 1) {
                                        Button(
                                            onClick  = { prestamoALiquidar = pago },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                                .height(44.dp),
                                            shape  = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Oscuro)
                                        ) {
                                            Icon(Icons.Default.Done, null, Modifier.size(16.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "LIQUIDAR TODO (${pagos.size} pagos)",
                                                fontWeight = FontWeight.Black,
                                                fontSize   = 12.sp
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))
                                }
                            }

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

// ── Encabezado de columnas de la tabla ───────────────────────────
@Composable
private fun CobranzaTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("CLIENTE",      Modifier.weight(1.6f), fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
        Text("PRÉSTAMO",     Modifier.weight(0.9f), fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
        Text("MENSUALIDAD",  Modifier.weight(0.9f), fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
        Text("ESTATUS",      Modifier.weight(0.8f), fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
    }
}

// ── Fila de la tabla ─────────────────────────────────────────────
@Composable
private fun CobranzaTableRow(
    pago:      PagoPendiente,
    expandido: Boolean,
    onClick:   () -> Unit
) {
    val (estadoColor, estadoLabel) = when (pago.estadoPrestamo?.uppercase()) {
        "MOROSO" -> Color(0xFFEF4444) to "MOROSO"
        else     -> Verde             to "ACTIVO"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                if (expandido) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // CLIENTE
        Column(modifier = Modifier.weight(1.6f)) {
            val partes = pago.nombreClienteUi.split(" ")
            Text(partes.take(2).joinToString(" "), fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 14.sp)
            if (partes.size > 2)
                Text(partes.drop(2).joinToString(" "), fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 14.sp)
            Text(
                pago.curp.orEmpty(),
                fontSize     = 9.sp,
                color        = MaterialTheme.colorScheme.outline,
                fontFamily   = FontFamily.Monospace,
                fontWeight   = FontWeight.Bold
            )
        }
        // PRÉSTAMO
        pago.folio?.let {
            Text(
                it,
                modifier     = Modifier.weight(0.9f),
                fontSize     = 11.sp,
                fontWeight   = FontWeight.Bold,
                color        = Rojo,
                fontFamily   = FontFamily.Monospace
            )
        }
        // MENSUALIDAD
        Text(
            "$${String.format("%,.0f", pago.monto)}",
            modifier   = Modifier.weight(0.9f),
            fontSize   = 11.sp,
            fontWeight = FontWeight.Black
        )
        // ESTATUS
        Surface(color = estadoColor.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
            Text(
                estadoLabel,
                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                fontSize   = 8.sp,
                fontWeight = FontWeight.Black,
                color      = estadoColor
            )
        }
    }
}

// ── Cabecera del panel expandido (imagen de referencia) ──────────
@Composable
private fun PanelCabeceraCobranza(
    nombreCliente:  String,
    metodoPago:     String,
    onMetodoChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Oscuro)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "CAJA DE\nCOBRANZA",
                style      = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color      = Color.White,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                nombreCliente.uppercase(),
                fontSize   = 10.sp,
                fontWeight = FontWeight.Bold,
                color      = Rojo
            )
        }
    }
}
private val Amarillo = Color(0xFFF59E0B)

// ── Dialog: confirmar pago de una mensualidad ─────────────────────
@Composable
fun ConfirmarPagoDialog(
    pago:           PagoPendiente,
    metodoPago:     String,
    onMetodoChange: (String) -> Unit,
    onConfirmar:    () -> Unit,
    onDismiss:      () -> Unit
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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("EFECTIVO", "PAYPAL").forEach { metodo ->
                        Row(
                            modifier          = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = metodoPago == metodo,
                                onClick  = { onMetodoChange(metodo) }
                            )
                            Text(metodo, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors  = ButtonDefaults.buttonColors(containerColor = Verde)
            ) { Text("CONFIRMAR", fontWeight = FontWeight.Black) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}

// ── Dialog: confirmar liquidación total del préstamo ──────────────
@Composable
fun ConfirmarLiquidacionDialog(
    pago:           PagoPendiente,
    totalPendiente: Double,
    metodoPago:     String,
    onMetodoChange: (String) -> Unit,
    onConfirmar:    () -> Unit,
    onDismiss:      () -> Unit
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

                // Advertencia
                Surface(
                    color = Amarillo.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier              = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Amarillo)
                        Text(
                            "Se registrarán todos los pagos pendientes del préstamo ${pago.folio}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Text("Cliente: ${pago.nombreClienteUi}", style = MaterialTheme.typography.bodyMedium)

                Text(
                    "Saldo total a liquidar: $${String.format("%,.2f", totalPendiente)}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Black,
                        color      = Amarillo
                    )
                )

                Text(
                    "MÉTODO DE PAGO:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.outline
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("EFECTIVO", "PAYPAL").forEach { metodo ->
                        Row(
                            modifier          = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = metodoPago == metodo,
                                onClick  = { onMetodoChange(metodo) }
                            )
                            Text(metodo, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors  = ButtonDefaults.buttonColors(containerColor = Amarillo)
            ) { Text("LIQUIDAR TODO", fontWeight = FontWeight.Black, color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}