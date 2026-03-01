package com.moon.casaprestamo.presentation.admin.supervision.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.moon.casaprestamo.data.models.PrestamoPendienteAdmin
import com.moon.casaprestamo.data.models.TicketPrestamoDetalle
import com.moon.casaprestamo.presentation.cliente.cartera.PagoRowPrototipo
import com.moon.casaprestamo.presentation.cliente.cartera.SpecificCard
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectorFechasDialog(
    inicialDesde: String,
    inicialHasta: String,
    onDismiss: () -> Unit,
    onConfirmar: (String, String) -> Unit
) {
    val hoy = Calendar.getInstance()
    fun parseMillis(display: String): Long? = try {
        if (display.isBlank()) null else java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(display)?.time
    } catch (_: Exception) { null }

    val stateDesde = rememberDatePickerState(initialSelectedDateMillis = parseMillis(inicialDesde) ?: hoy.timeInMillis)
    val stateHasta = rememberDatePickerState(initialSelectedDateMillis = parseMillis(inicialHasta) ?: hoy.timeInMillis)
    var paso by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth(0.92f)) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(if (paso == 0) "Fecha de inicio" else "Fecha de fin", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Text(if (paso == 0) "Selecciona desde cuándo" else "Selecciona hasta cuándo", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0, 1).forEach { i ->
                            Surface(shape = CircleShape, color = if (i == paso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(8.dp)) {}
                        }
                    }
                }
                DatePicker(
                    state = if (paso == 0) stateDesde else stateHasta,
                    showModeToggle = false,
                    title = null,
                    headline = null,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("CANCELAR") }
                    if (paso == 0) {
                        Button(onClick = { paso = 1 }, modifier = Modifier.weight(1f), enabled = stateDesde.selectedDateMillis != null) { Text("SIGUIENTE") }
                    } else {
                        Button(
                            onClick = { onConfirmar(milisAFecha(stateDesde.selectedDateMillis), milisAFecha(stateHasta.selectedDateMillis)) },
                            modifier = Modifier.weight(1f),
                            enabled = stateHasta.selectedDateMillis != null
                        ) { Text("APLICAR") }
                    }
                }
            }
        }
    }
}

@Composable
internal fun EstadoDeCuentaModal(detalle: TicketPrestamoDetalle, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.92f)) {
            Column {
                Box(Modifier.fillMaxWidth().background(Oscuro).padding(20.dp)) {
                    Column {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text("Estado de Cuenta", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("EXPEDIENTE: ${detalle.folio}", color = Color.White.copy(0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color.White) }
                        }
                    }
                }
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        val liquidado = maxOf(0.0, detalle.montoTotal - detalle.saldoPendiente)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SpecificCard(label = "CAPITAL", value = "\$${String.format("%,.0f", detalle.montoTotal)}", containerColor = Oscuro, contentColor = Color.White, modifier = Modifier.weight(1f))
                            SpecificCard(label = "LIQUIDADO", value = "\$${String.format("%,.0f", liquidado)}", containerColor = Verde, contentColor = Color.White, modifier = Modifier.weight(1f))
                            SpecificCard(label = "SALDO", value = "\$${String.format("%,.0f", detalle.saldoPendiente)}", containerColor = Rojo, contentColor = Color.White, modifier = Modifier.weight(1f))
                        }
                    }
                    items(detalle.pagos, key = { it.idPago }) { pago -> PagoRowPrototipo(pago) }
                }
            }
        }
    }
}

@Composable
internal fun DetalleSolicitudModal(
    prestamo: PrestamoPendienteAdmin,
    permiteAprobar: Boolean,
    onDismiss: () -> Unit,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth(0.95f)) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("${prestamo.nombre ?: ""} ${prestamo.apellidoPaterno ?: ""}".trim().uppercase(), fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Solicitud: ${prestamo.fechaCreacion.take(10)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                Text("\$${String.format("%,.0f", prestamo.montoTotal)}", fontSize = 28.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)

                if (permiteAprobar) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onRechazar, modifier = Modifier.weight(1f).height(52.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Rojo), border = BorderStroke(1.dp, Rojo)) { Text("RECHAZAR", fontWeight = FontWeight.Black) }
                        Button(onClick = onAprobar, modifier = Modifier.weight(1f).height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Oscuro)) { Text("APROBAR", fontWeight = FontWeight.Black) }
                    }
                } else {
                    Surface(color = Amarillo.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp)) {
                        Text("Acceso restringido: solo administradores pueden aprobar/rechazar.", modifier = Modifier.padding(12.dp), color = Amarillo, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
