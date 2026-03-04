package com.moon.casaprestamo.ui.components.supervision

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moon.casaprestamo.data.models.PrestamoPendienteAdmin

/**
 * Pestaña de Solicitudes de Crédito (solo Admin).
 *
 * @param solicitudes     Lista de préstamos en estado pendiente.
 * @param permiteAprobar  Si es `false` los botones de aprobación quedan bloqueados
 *                        y los diálogos de acción NO se invocan. Úsalo para Empleados.
 * @param isLoading       Indica si la carga está en progreso.
 * @param onVolver        Callback para regresar a la pestaña de Cartera.
 * @param onAbrirDetalle  Callback con el objeto de solicitud seleccionada.
 *                        Solo se invoca si [permiteAprobar] es `true`.
 */
@Composable
fun TabSolicitudes(
    solicitudes:    List<PrestamoPendienteAdmin>,
    permiteAprobar: Boolean,
    isLoading:      Boolean,
    onVolver:       () -> Unit,
    onAbrirDetalle: (PrestamoPendienteAdmin) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }

    // derivedStateOf evita re-composición al escribir en el buscador
    val filtrados by remember(solicitudes, query) {
        derivedStateOf {
            solicitudes.filter {
                query.isBlank() || listOfNotNull(it.nombre, it.apellidoPaterno, it.curp, it.folio)
                    .joinToString(" ").contains(query, ignoreCase = true)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Barra de título
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(44.dp), RoundedCornerShape(14.dp), Amarillo) {
                Icon(Icons.Default.Schedule, null, tint = Color.White, modifier = Modifier.padding(10.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("APROBACIÓN DE CRÉDITOS", fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text(
                    "TRÁMITES DE CLIENTES NUEVOS",
                    fontSize      = 9.sp,
                    letterSpacing = 0.8.sp,
                    color         = MaterialTheme.colorScheme.outline
                )
            }
            TextButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("VOLVER A CARTERA", fontSize = 11.sp)
            }
        }

        // Buscador
        OutlinedTextField(
            value         = query,
            onValueChange = { query = it },
            placeholder   = { Text("Filtrar por nombre o ID…", fontSize = 13.sp) },
            leadingIcon   = { Icon(Icons.Default.Search, null, Modifier.size(18.dp)) },
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = true,
            shape         = RoundedCornerShape(50),
            colors        = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedBorderColor    = Color.Transparent,
                focusedBorderColor      = Amarillo
            )
        )

        when {
            isLoading           -> LoadBox()
            filtrados.isEmpty() -> Box(Modifier.fillMaxWidth().padding(48.dp), Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Inbox, null, Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Text(
                        "No hay solicitudes pendientes",
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.outline
                    )
                }
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtrados, key = { it.idPrestamo }) { sol ->
                    SolicitudCard(
                        prestamo    = sol,
                        // Seguridad: si no es admin, el clic no abre el diálogo de aprobación
                        onVerMas    = if (permiteAprobar) {
                            { onAbrirDetalle(sol) }
                        } else {
                            { /* acceso restringido */ }
                        },
                        permiteAprobar = permiteAprobar
                    )
                }
            }
        }
    }
}

// ─── Tarjeta de Solicitud ─────────────────────────────────

@Composable
private fun SolicitudCard(
    prestamo:      PrestamoPendienteAdmin,
    onVerMas:      () -> Unit,
    permiteAprobar: Boolean
) {
    val inicial = (prestamo.nombre?.firstOrNull() ?: '?').uppercaseChar()

    Card(
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(Modifier.size(40.dp), CircleShape, Amarillo.copy(alpha = 0.2f)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(inicial.toString(), fontWeight = FontWeight.Black, color = Amarillo, fontSize = 16.sp)
                        }
                    }
                    Column {
                        Text(
                            "${prestamo.nombre ?: ""} ${prestamo.apellidoPaterno ?: ""}".trim().uppercase(),
                            fontWeight = FontWeight.Black,
                            fontSize   = 14.sp
                        )
                        Text(
                            "FOLIO #${prestamo.idPrestamo.toString().padStart(5, '0')}",
                            fontSize      = 10.sp,
                            fontWeight    = FontWeight.Bold,
                            color         = MaterialTheme.colorScheme.outline,
                            fontFamily    = FontFamily.Monospace,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
                EstadoGestionChip("PENDIENTE")
            }

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Column {
                    Text("MONTO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                    Text(
                        "\$${String.format("%,.0f", prestamo.montoTotal)}",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("PLAZO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                    Text("${prestamo.plazoMeses} MESES", fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            }

            // El botón "Ver detalle" solo está habilitado para Admin
            if (permiteAprobar) {
                androidx.compose.material3.Button(
                    onClick   = onVerMas,
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Oscuro)
                ) {
                    Text("VER DETALLE", fontWeight = FontWeight.Black)
                }
            } else {
                Surface(
                    color    = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape    = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Solo administradores pueden aprobar",
                        modifier  = Modifier.padding(12.dp),
                        fontSize  = 11.sp,
                        color     = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}