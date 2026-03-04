package com.moon.casaprestamo.ui.components.supervision

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moon.casaprestamo.presentation.admin.supervision.SupervisionTab
import com.moon.casaprestamo.ui.components.common.SelectorFechasDialog

/**
 * Barra superior con KPIs y selector de fechas.
 *
 * @param esAdmin Si es `false`:
 *   - La tarjeta RECAUDACIÓN muestra "Acceso restringido" y no es clicable.
 *   - La tarjeta SOLICITUDES sigue siendo visible (contador), pero no navega.
 */
@Composable
internal fun BarraSuperior(
    recaudacion:           Double,
    recaudacionCargando:   Boolean,
    solicitudesPendientes: Int,
    tabActual:             SupervisionTab,
    fechaDesde:            String,
    fechaHasta:            String,
    onLoad:                (String, String) -> Unit,
    onClickRecaudacion:    () -> Unit,
    onClickSolicitudes:    () -> Unit,
    onFechasChange:        (String, String) -> Unit,
    esAdmin:               Boolean = true
) {
    var showSelectorFechas by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { onLoad("", "") }

    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Fila de KPIs ──────────────────────────────────────
        Row(
            modifier            = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // KPI RECAUDACIÓN — restringido para empleados
            if (esAdmin) {
                KpiCard(
                    titulo     = "RECAUDACIÓN",
                    valor      = if (recaudacionCargando) "CARGANDO…" else "\$${String.format("%,.0f", recaudacion)}",
                    icono      = Icons.Default.AttachMoney,
                    color      = Verde,
                    isSelected = tabActual != SupervisionTab.SOLICITUDES,
                    onClick    = onClickRecaudacion,
                    modifier   = Modifier.weight(1f)
                )
            } else {
                // Tarjeta bloqueada para empleados
                KpiCardBloqueada(
                    titulo   = "RECAUDACIÓN",
                    modifier = Modifier.weight(1f)
                )
            }

            // KPI SOLICITUDES — solo visible para Admin
            if (esAdmin) {
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
        }

        // ── Selector de rango de fechas ───────────────────────
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
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (fechaDesde.isNotBlank()) {
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.Close,
                    null,
                    Modifier.size(16.dp).clickable { onFechasChange("", "") },
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }

    // Diálogo selector de fechas (reutilizado del archivo existente)
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

// ─── KPI Card normal ──────────────────────────────────────

@Composable
private fun KpiCard(
    titulo:     String,
    valor:      String,
    icono:      ImageVector,
    color:      Color,
    isSelected: Boolean,
    onClick:    () -> Unit,
    modifier:   Modifier = Modifier
) {
    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(16.dp),
        color    = if (isSelected) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border   = if (isSelected) BorderStroke(1.5.dp, color) else null,
        modifier = modifier
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(Modifier.size(32.dp), CircleShape, color.copy(alpha = 0.15f)) {
                Icon(icono, null, tint = color, modifier = Modifier.padding(6.dp))
            }
            Column {
                Text(titulo, fontSize = 9.sp, fontWeight = FontWeight.Black, color = color, letterSpacing = 0.5.sp)
                Text(valor,  fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

// ─── KPI Card bloqueada para empleados ───────────────────

@Composable
private fun KpiCardBloqueada(
    titulo:   String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = modifier
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(Modifier.size(32.dp), CircleShape, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) {
                Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), modifier = Modifier.padding(6.dp))
            }
            Column {
                Text(titulo, fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), letterSpacing = 0.5.sp)
                Text("Acceso restringido", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            }
        }
    }
}