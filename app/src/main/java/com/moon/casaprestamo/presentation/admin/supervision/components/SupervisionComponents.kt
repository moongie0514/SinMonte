package com.moon.casaprestamo.presentation.admin.supervision.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moon.casaprestamo.data.models.TicketDetalle
import java.text.SimpleDateFormat
import java.util.*

internal val Rojo = Color(0xFFA6032F)
internal val Oscuro = Color(0xFF0F172A)
internal val Verde = Color(0xFF10B981)
internal val Amarillo = Color(0xFFF59E0B)

internal typealias MovimientoDia = TicketDetalle

internal fun fechaStringAMillis(fecha: String): Long? = try {
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha)?.time
} catch (_: Exception) { null }

internal fun displayAApiDate(display: String): String = try {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val api = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    api.format(sdf.parse(display)!!)
} catch (_: Exception) { display }

internal fun milisAFecha(milis: Long?): String {
    if (milis == null) return ""
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(milis))
}

@Composable
internal fun HeaderTablaSupervision(
    titulo: String,
    subtitulo: String,
    color: Color,
    icono: @Composable () -> Unit,
    onSwitch: (() -> Unit)? = null,
    switchIcon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(Modifier.size(44.dp), RoundedCornerShape(14.dp), color) {
            Box(contentAlignment = Alignment.Center) { icono() }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text(subtitulo, fontSize = 9.sp, letterSpacing = 0.8.sp, color = MaterialTheme.colorScheme.outline)
        }
        if (onSwitch != null && switchIcon != null) {
            IconButton(onClick = onSwitch) { switchIcon() }
        }
    }
}

@Composable
internal fun EstadoGestionChip(texto: String, color: Color) {
    Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(50)) {
        Text(
            texto,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = color,
            maxLines = 1
        )
    }
}

@Composable
internal fun TH(text: String, modifier: Modifier) =
    Text(text, modifier = modifier, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.5.sp, color = MaterialTheme.colorScheme.outline)

@Composable
internal fun LoadBox() =
    Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) { CircularProgressIndicator(color = Rojo) }

@Composable
internal fun EmptyBox(msg: String) =
    Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) { Text(msg, color = MaterialTheme.colorScheme.outline) }
