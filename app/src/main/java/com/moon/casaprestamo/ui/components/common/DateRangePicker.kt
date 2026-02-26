package com.moon.casaprestamo.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

/**
 * DateRangePicker - Selector de rango de fechas
 * Usado en: Cobranza, GestionPrestamos, Reportes
 */
@Composable
fun DateRangePicker(
    fechaInicio: String,
    fechaFin: String,
    onFechaInicioChange: (String) -> Unit,
    onFechaFinChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fecha Inicio
            DateInputField(
                value = fechaInicio,
                onValueChange = onFechaInicioChange,
                placeholder = "Inicio",
                modifier = Modifier.weight(1f)
            )

            // Separador
            Text(
                text = "/",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            // Fecha Fin
            DateInputField(
                value = fechaFin,
                onValueChange = onFechaFinChange,
                placeholder = "Fin",
                modifier = Modifier.weight(1f)
            )

            // Botón de limpiar
            if (fechaInicio.isNotEmpty() || fechaFin.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Limpiar",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DateInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            placeholder = {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            singleLine = true,
            readOnly = true
        )

        // Overlay clickable para abrir el picker
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(
                    Modifier.padding(0.dp)
                )
        ) {
            // Aquí normalmente abrirías un DatePicker nativo
            // Por ahora usamos un TextField editable
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                placeholder = {
                    Text(
                        placeholder,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                singleLine = true
            )
        }
    }
}

/**
 * DateRangePicker alternativo con selector de presets
 */
@Composable
fun DateRangePickerWithPresets(
    fechaInicio: String,
    fechaFin: String,
    onFechaInicioChange: (String) -> Unit,
    onFechaFinChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPreset by remember { mutableStateOf<DatePreset?>(null) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Presets rápidos
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DatePreset.values().forEach { preset ->
                FilterChip(
                    selected = selectedPreset == preset,
                    onClick = {
                        selectedPreset = preset
                        val dates = preset.getDates()
                        onFechaInicioChange(dates.first)
                        onFechaFinChange(dates.second)
                    },
                    label = {
                        Text(
                            preset.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Selector de fechas manual
        DateRangePicker(
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            onFechaInicioChange = {
                selectedPreset = null
                onFechaInicioChange(it)
            },
            onFechaFinChange = {
                selectedPreset = null
                onFechaFinChange(it)
            },
            onClear = {
                selectedPreset = null
                onClear()
            }
        )
    }
}

enum class DatePreset(val label: String) {
    HOY("Hoy"),
    SEMANA("Semana"),
    MES("Mes"),
    TRIMESTRE("3 meses");

    fun getDates(): Pair<String, String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val hoy = sdf.format(calendar.time)

        return when (this) {
            HOY -> Pair(hoy, hoy)
            SEMANA -> {
                calendar.add(Calendar.DAY_OF_MONTH, -7)
                Pair(sdf.format(calendar.time), hoy)
            }
            MES -> {
                calendar.add(Calendar.MONTH, -1)
                Pair(sdf.format(calendar.time), hoy)
            }
            TRIMESTRE -> {
                calendar.add(Calendar.MONTH, -3)
                Pair(sdf.format(calendar.time), hoy)
            }
        }
    }
}