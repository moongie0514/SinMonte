package com.moon.casaprestamo.ui.components.supervision

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ═══════════════════════════════════════════════════════════════════
// CONSTANTES DE COLOR — fuente única de verdad para todo el módulo
// ═══════════════════════════════════════════════════════════════════

val Rojo     = Color(0xFFA6032F)
val Verde    = Color(0xFF10B981)
val Amarillo = Color(0xFFF59E0B)
val Oscuro   = Color(0xFF0F172A)

// ═══════════════════════════════════════════════════════════════════
// UTILIDADES DE FECHA
// ═══════════════════════════════════════════════════════════════════

/** Convierte milisegundos (Long?) a "dd/MM/yyyy". Devuelve "" si null. */
fun milisAFecha(millis: Long?): String {
    if (millis == null) return ""
    return try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
    } catch (e: Exception) { "" }
}

/** Convierte "dd/MM/yyyy" (display) → "yyyy-MM-dd" (API). */
fun displayAApiDate(display: String): String {
    if (display.isBlank()) return ""
    return try {
        val sdfIn  = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfOut = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdfOut.format(sdfIn.parse(display)!!)
    } catch (e: Exception) { "" }
}

/** Parsea "yyyy-MM-dd" y devuelve milisegundos, o null si falla. */
fun fechaStringAMillis(fecha: String): Long? {
    if (fecha.isBlank()) return null
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha)?.time
    } catch (e: Exception) { null }
}

// ═══════════════════════════════════════════════════════════════════
// MICRO-COMPOSABLES REUTILIZABLES
// ═══════════════════════════════════════════════════════════════════

/** Encabezado de columna para tablas de supervisión. */
@Composable
fun TH(text: String, modifier: Modifier = Modifier) {
    Text(
        text          = text,
        modifier      = modifier,
        fontSize      = 9.sp,
        fontWeight    = FontWeight.Black,
        letterSpacing = 0.8.sp,
        color         = MaterialTheme.colorScheme.outline
    )
}

/** Encabezado de columna en el calendario de pagos. */
@Composable
fun CalH(text: String, modifier: Modifier = Modifier) {
    Text(
        text       = text,
        modifier   = modifier,
        fontSize   = 10.sp,
        fontWeight = FontWeight.Black,
        color      = Color(0xFF94A3B8)
    )
}

/** Par etiqueta / valor para secciones de condiciones del préstamo. */
@Composable
fun CondItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black)
    }
}

/**
 * Fila de encabezados estándar para tablas de Cartera y Folios.
 * [columnas] lista de pares (etiqueta, weight).
 * Ejemplo: HeaderTablaSupervision(listOf("CLIENTE" to 1.4f, "FECHA" to 0.8f))
 */
@Composable
fun HeaderTablaSupervision(columnas: List<Pair<String, Float>>) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        columnas.forEachIndexed { index, (label, weight) ->
            TH(label, Modifier.weight(weight))
            if (index < columnas.lastIndex) Spacer(Modifier.width(8.dp))
        }
    }
}

/** Chip de estado: ACTIVO / MORA / LIQUIDADO / PAGADO / PENDIENTE / APROBADO / RECHAZADO. */
@Composable
fun EstadoGestionChip(estado: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (estado.uppercase()) {
        "ACTIVO"    -> Triple(Verde.copy(alpha = 0.12f),              Verde,             "ACTIVO")
        "MORA"      -> Triple(Rojo.copy(alpha = 0.12f),               Rojo,              "EN MORA")
        "LIQUIDADO" -> Triple(Color(0xFF3B82F6).copy(alpha = 0.12f),  Color(0xFF3B82F6), "LIQUIDADO")
        "PAGADO"    -> Triple(Rojo.copy(alpha = 0.12f),               Rojo,              "PAGADO")
        "PENDIENTE" -> Triple(Amarillo.copy(alpha = 0.12f),           Amarillo,          "PENDIENTE")
        "APROBADO"  -> Triple(Verde.copy(alpha = 0.12f),              Verde,             "APROBADO")
        "RECHAZADO" -> Triple(Rojo.copy(alpha = 0.12f),               Rojo,              "RECHAZADO")
        else        -> Triple(
            MaterialTheme.colorScheme.surfaceContainerLow,
            MaterialTheme.colorScheme.outline,
            estado.uppercase()
        )
    }
    Box(modifier = modifier) {
        Surface(color = bgColor, shape = RoundedCornerShape(50)) {
            Text(
                text       = label,
                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize   = 9.sp,
                fontWeight = FontWeight.Black,
                color      = textColor,
                maxLines   = 1
            )
        }
    }
}

// ─── Helpers de estado vacío / cargando ──────────────────

@Composable
fun LoadBox() {
    Box(
        modifier         = Modifier.fillMaxWidth().padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Rojo)
    }
}

@Composable
fun EmptyBox(mensaje: String = "Sin registros") {
    Box(
        modifier         = Modifier.fillMaxWidth().padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.Inbox,
                contentDescription = null,
                modifier           = Modifier.size(64.dp),
                tint               = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            Text(
                text       = mensaje,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.outline
            )
        }
    }
}