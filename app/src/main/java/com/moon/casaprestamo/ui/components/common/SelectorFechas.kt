package com.moon.casaprestamo.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.moon.casaprestamo.ui.components.supervision.milisAFecha
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorFechasDialog(
    inicialDesde: String,
    inicialHasta: String,
    onDismiss: () -> Unit,
    onConfirmar: (String, String) -> Unit
) {
    // Usamos dos DatePickerState independientes
    val hoy = Calendar.getInstance()

    fun parseMillis(display: String): Long? {
        if (display.isBlank()) return null
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(display)?.time
        } catch (e: Exception) { null }
    }

    val stateDesde = rememberDatePickerState(
        initialSelectedDateMillis = parseMillis(inicialDesde) ?: hoy.timeInMillis
    )
    val stateHasta = rememberDatePickerState(
        initialSelectedDateMillis = parseMillis(inicialHasta) ?: hoy.timeInMillis
    )

    var paso by remember { mutableStateOf(0) } // 0 = elegir desde, 1 = elegir hasta

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape    = RoundedCornerShape(24.dp),
            color    = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
        ) {
            Column {
                // Indicador de paso
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (paso == 0) "Fecha de inicio" else "Fecha de fin",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Text(
                            if (paso == 0) "Selecciona desde cuándo" else "Selecciona hasta cuándo",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    // Chips visuales de paso
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0, 1).forEach { i ->
                            Surface(
                                shape = CircleShape,
                                color = if (i == paso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(8.dp)
                            ) {}
                        }
                    }
                }

                // Resumen de selección actual
                if (stateDesde.selectedDateMillis != null || stateHasta.selectedDateMillis != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ResumenFechaChip(
                            label  = "Desde",
                            fecha  = milisAFecha(stateDesde.selectedDateMillis),
                            activo = paso == 0,
                            onClick = { paso = 0 },
                            modifier = Modifier.weight(1f)
                        )
                        ResumenFechaChip(
                            label  = "Hasta",
                            fecha  = milisAFecha(stateHasta.selectedDateMillis),
                            activo = paso == 1,
                            onClick = { paso = 1 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // DatePicker sin título extra
                DatePicker(
                    state    = if (paso == 0) stateDesde else stateHasta,
                    showModeToggle = false,
                    title    = null,
                    headline = null,
                    modifier = Modifier.fillMaxWidth()
                )

                // Botones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) { Text("CANCELAR") }

                    if (paso == 0) {
                        Button(
                            onClick  = { paso = 1 },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(12.dp),
                            enabled  = stateDesde.selectedDateMillis != null
                        ) { Text("SIGUIENTE") }
                    } else {
                        Button(
                            onClick = {
                                val desde = milisAFecha(stateDesde.selectedDateMillis)
                                val hasta = milisAFecha(stateHasta.selectedDateMillis)
                                onConfirmar(desde, hasta)
                            },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(12.dp),
                            enabled  = stateHasta.selectedDateMillis != null
                        ) { Text("APLICAR") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenFechaChip(
    label: String,
    fecha: String,
    activo: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(10.dp),
        color    = if (activo) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        border   = if (activo) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
            Text(fecha.ifBlank { "—" }, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
    }
}