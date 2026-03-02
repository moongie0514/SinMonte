package com.moon.casaprestamo.presentation.admin.supervision.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moon.casaprestamo.presentation.admin.supervision.SupervisionTab

@Composable
internal fun BarraSuperior(
    recaudacion: Double,
    recaudacionLabel: String,
    recaudacionCargando: Boolean,
    solicitudesPendientes: Int,
    tabActual: SupervisionTab,
    fechaDesde: String,
    fechaHasta: String,
    esAdmin: Boolean,
    onLoad: (String, String) -> Unit,
    onClickRecaudacion: () -> Unit,
    onClickSolicitudes: () -> Unit,
    onFechasChange: (String, String) -> Unit
) {
    var showSelectorFechas by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { onLoad("", "") }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiCard(
                titulo = "RECAUDACIÓN ($recaudacionLabel)",
                valor = if (!esAdmin) "ACCESO RESTRINGIDO" else if (recaudacionCargando) "CARGANDO..." else "\$${String.format("%,.0f", recaudacion)}",
                icono = Icons.Default.AttachMoney,
                color = Verde,
                isSelected = tabActual != SupervisionTab.SOLICITUDES,
                onClick = { if (esAdmin) onClickRecaudacion() },
                enabled = esAdmin,
                modifier = Modifier.weight(1f)
            )
            if (esAdmin) {
                KpiCard(
                    titulo = "SOLICITUDES",
                    valor = "$solicitudesPendientes NUEVAS",
                    icono = Icons.Default.Schedule,
                    color = Amarillo,
                    isSelected = tabActual == SupervisionTab.SOLICITUDES,
                    onClick = onClickSolicitudes,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        OutlinedButton(
            onClick = { showSelectorFechas = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(
                if (fechaDesde.isNotBlank() && fechaHasta.isNotBlank()) "Del $fechaDesde al $fechaHasta" else "Filtrar por rango de fechas",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (fechaDesde.isNotBlank()) {
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.Close, null, Modifier.size(16.dp).clickable { onFechasChange("", "") }, tint = MaterialTheme.colorScheme.outline)
            }
        }
    }

    if (showSelectorFechas) {
        SelectorFechasDialog(
            inicialDesde = fechaDesde,
            inicialHasta = fechaHasta,
            onDismiss = { showSelectorFechas = false },
            onConfirmar = { d, h -> onFechasChange(d, h); showSelectorFechas = false }
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
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) color.copy(0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.4f),
        border = if (isSelected) BorderStroke(1.5.dp, color) else null,
        modifier = modifier
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(Modifier.size(32.dp), CircleShape, color.copy(0.15f)) {
                Icon(icono, null, tint = color, modifier = Modifier.padding(6.dp))
            }
            Column {
                Text(titulo, fontSize = 9.sp, fontWeight = FontWeight.Black, color = color, letterSpacing = 0.5.sp)
                Text(valor, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
