package com.moon.casaprestamo.ui.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moon.casaprestamo.data.models.PrestamoPendienteAdmin
import com.moon.casaprestamo.presentation.admin.supervision.*

@Composable
internal fun TabSolicitudes(
    uiState: SupervisionUiState,
    onVolver: () -> Unit,
    onAbrirDetalle: (PrestamoPendienteAdmin) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtrados by remember(uiState.solicitudes, query) {
        derivedStateOf {
            uiState.solicitudes.filter {
                query.isBlank() || listOfNotNull(it.nombre, it.apellidoPaterno, it.curp, it.folio)
                    .joinToString(" ").contains(query, ignoreCase = true)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(44.dp), RoundedCornerShape(14.dp), Amarillo) {
                Icon(Icons.Default.Schedule, null, tint = Color.White, modifier = Modifier.padding(10.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("APROBACIÓN DE CRÉDITOS", fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text("TRÁMITES DE CLIENTES NUEVOS", fontSize = 9.sp, letterSpacing = 0.8.sp, color = MaterialTheme.colorScheme.outline)
            }
            TextButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("VOLVER A CARTERA", fontSize = 11.sp)
            }
        }

        OutlinedTextField(
            value = query, onValueChange = { query = it },
            placeholder = { Text("Filtrar resultados por nombre o ID...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedBorderColor    = Color.Transparent, focusedBorderColor = Amarillo
            )
        )

        when {
            uiState.solicitudesLoading -> LoadBox()
            filtrados.isEmpty() -> Box(Modifier.fillMaxWidth().padding(48.dp), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Inbox, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline.copy(0.3f))
                    Text("No hay solicitudes pendientes", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                }
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtrados, key = { it.idPrestamo }) { sol ->
                    SolicitudCard(prestamo = sol, onVerMas = { onAbrirDetalle(sol) })
                }
            }
        }
    }
}

@Composable
private fun SolicitudCard(prestamo: PrestamoPendienteAdmin, onVerMas: () -> Unit) {
    val inicial = (prestamo.nombre?.firstOrNull() ?: '?').uppercaseChar()
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(Modifier.size(40.dp), CircleShape, Amarillo.copy(0.2f)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(inicial.toString(), fontWeight = FontWeight.Black, color = Amarillo, fontSize = 16.sp)
                        }
                    }
                    Column {
                        Text(
                            "${prestamo.nombre ?: ""} ${prestamo.apellidoPaterno ?: ""}".trim().uppercase(),
                            fontWeight = FontWeight.Black, fontSize = 14.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Schedule, null, Modifier.size(12.dp), tint = Amarillo)
                            Text("POR REVISAR", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Amarillo)
                        }
                    }
                }
                Text(
                    "REF: ${prestamo.folio ?: "SOL-${prestamo.idPrestamo}"}",
                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline, fontFamily = FontFamily.Monospace
                )
            }
            Column {
                Text("MONTO SOLICITADO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline, letterSpacing = 0.5.sp)
                Text("\$${String.format("%,.0f", prestamo.montoTotal)}", fontSize = 26.sp, fontWeight = FontWeight.Black)
            }
            Button(
                onClick = onVerMas, modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Oscuro)
            ) {
                Text("VER MÁS", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    }
}
