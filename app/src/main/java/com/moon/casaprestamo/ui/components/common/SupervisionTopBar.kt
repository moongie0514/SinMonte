package com.moon.casaprestamo.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.moon.casaprestamo.presentation.admin.supervision.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
internal fun BarraSuperior(
    recaudacion: Double,
    recaudacionCargando: Boolean,
    solicitudesPendientes: Int,
    tabActual: SupervisionTab,
    fechaDesde: String,
    fechaHasta: String,
    onLoad: (String, String) -> Unit,
    onClickRecaudacion: () -> Unit,
    onClickSolicitudes: () -> Unit,
    onFechasChange: (String, String) -> Unit
) {
    var showSelectorFechas by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { onLoad("", "") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // FILA 1: KPIs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KpiCard(
                titulo     = "RECAUDACIÓN",
                valor      = if (recaudacionCargando) "CARGANDO..." else "\$${String.format("%,.0f", recaudacion)}",
                icono      = Icons.Default.AttachMoney,
                color      = Verde,
                isSelected = tabActual != SupervisionTab.SOLICITUDES,
                onClick    = onClickRecaudacion,
                modifier   = Modifier.weight(1f)
            )
            KpiCard(
                titulo     = "SOLICITUDES",
                valor      = "$solicitudesPendientes NUEVAS",
                icono      = Icons.Default.Schedule,
                color      = Amarillo,
                isSelected = tabActual == SupervisionTab.SOLICITUDES,
                onClick    = onClickSolicitudes,
                modifier   = Modifier.weight(1f)
            )
        }

        // FILA 2: Botón selector de fechas
        OutlinedButton(
            onClick  = { showSelectorFechas = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape    = RoundedCornerShape(12.dp),
            border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(
                if (fechaDesde.isNotBlank() && fechaHasta.isNotBlank())
                    "Del $fechaDesde al $fechaHasta"
                else
                    "Filtrar por rango de fechas",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (fechaDesde.isNotBlank()) {
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.Close, null, Modifier.size(16.dp).clickable {
                        onFechasChange("", "")
                    },
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }

    // FIX 6: Selector de fechas limpio — dos DatePicker simples
    if (showSelectorFechas) {
        SelectorFechasDialog(
            inicialDesde = fechaDesde,
            inicialHasta = fechaHasta,
            onDismiss    = { showSelectorFechas = false },
            onConfirmar  = { desde, hasta ->
                onFechasChange(desde, hasta)
                showSelectorFechas = false
            }
        )
    }
}

@Composable
private fun KpiCard(
    titulo: String,
    valor: String,
    icono: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(16.dp),
        color    = if (isSelected) color.copy(0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.4f),
        border   = if (isSelected) BorderStroke(1.5.dp, color) else null,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(Modifier.size(32.dp), CircleShape, color.copy(0.15f)) {
                Icon(icono, null, tint = color, modifier = Modifier.padding(6.dp))
            }
            Column {
                Text(titulo, fontSize = 9.sp, fontWeight = FontWeight.Black, color = color, letterSpacing = 0.5.sp)
                Text(valor, fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// FIX 6: SELECTOR DE FECHAS — dos campos simples sin DateRangePicker
// ═════════════════════════════════════════════════════════════

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