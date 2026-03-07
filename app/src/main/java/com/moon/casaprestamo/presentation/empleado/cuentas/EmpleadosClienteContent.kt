package com.moon.casaprestamo.presentation.empleado.cuentas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moon.casaprestamo.data.models.UsuarioResumen
import com.moon.casaprestamo.presentation.admin.cuentas.CuentasUiState
import com.moon.casaprestamo.ui.components.cuentas.*

@Composable
fun EmpleadoCuentasContent(
    uiState:        CuentasUiState,
    onLoad:         (String) -> Unit,
    onCrearCliente: (String, String, String, String, String, String, String, String) -> Unit,
    onEditarUsuario:(Int, String, String, String, String, String, String) -> Unit,
    onToggleEstado: (Int, Boolean) -> Unit
) {
    var query         by rememberSaveable { mutableStateOf("") }
    var showNuevo     by remember { mutableStateOf(false) }
    var editarUsuario by remember { mutableStateOf<UsuarioResumen?>(null) }

    // Empleados solo ven clientes — se carga una vez
    LaunchedEffect(Unit) { onLoad("Cliente") }

    val filtrados = uiState.usuarios.filter {
        query.isBlank() || listOfNotNull(
            it.nombre, it.apellidoPaterno, it.apellidoMaterno,
            it.email, it.curp, it.noIdentificacion
        ).joinToString(" ").contains(query, ignoreCase = true)
    }

    Column(
        modifier            = Modifier.fillMaxSize().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header ──────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    color    = MaterialTheme.colorScheme.primary,
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.PersonSearch, null, tint = Color.White, modifier = Modifier.padding(12.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Directorio de Clientes",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                    )
                    Text(
                        "GESTIÓN DE CUENTAS",
                        fontSize      = 9.sp,
                        fontWeight    = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color         = MaterialTheme.colorScheme.outline
                    )
                }
            }
            // Empleado SÍ puede crear clientes
            IconButton(
                onClick  = { showNuevo = true },
                modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }

        // ── Tabla ────────────────────────────────────────────
        Card(
            modifier  = Modifier.fillMaxWidth().weight(1f),
            shape     = RoundedCornerShape(24.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value         = query,
                    onValueChange = { query = it },
                    placeholder   = { Text("Buscar por nombre, CURP o email...", fontSize = 14.sp) },
                    leadingIcon   = { Icon(Icons.Default.Search, null, Modifier.size(20.dp)) },
                    modifier      = Modifier.fillMaxWidth().padding(16.dp),
                    singleLine    = true,
                    shape         = RoundedCornerShape(16.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )

                // Encabezado tabla
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeaderCell("IDENTIDAD", Modifier.weight(1.8f))
                    HeaderCell("CURP",      Modifier.weight(1.8f))
                    HeaderCell("REGISTRO",  Modifier.weight(1f))
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                        filtrados.isEmpty() && query.isBlank() -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(Icons.Default.People, null, Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    Text("Sin clientes registrados",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                        filtrados.isEmpty() -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Sin resultados para \"$query\"",
                                    color = MaterialTheme.colorScheme.outline,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filtrados, key = { it.idUsuario }) { u ->
                                CuentaRow(
                                    usuario    = u,
                                    isPersonal = false,     // clientes no muestran chip activo/inactivo
                                    onEditar   = { editarUsuario = u },
                                    onEliminar = {},
                                    onToggle   = { onToggleEstado(u.idUsuario, !u.activo) }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }
        }

        // ── Feedback ─────────────────────────────────────────
        uiState.mensaje?.let { msg ->
            Surface(
                color    = if (msg.contains("✅")) Color(0xFF10B981).copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    msg,
                    modifier   = Modifier.padding(12.dp),
                    color      = if (msg.contains("✅")) Color(0xFF059669) else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────

    // Solo NuevoClienteDialog — sin opción de crear empleados
    if (showNuevo) {
        NuevoClienteDialog(
            onDismiss      = { showNuevo = false },
            onCrearCliente = { n, ap, am, em, tel, curp, ine, pass ->
                onCrearCliente(n, ap, am, em, tel, curp, ine, pass)
                showNuevo = false
                onLoad("Cliente")
            }
        )
    }

    editarUsuario?.let { u ->
        EditarUsuarioDialog(
            usuario   = u,
            onDismiss = { editarUsuario = null },
            onGuardar = { n, ap, am, tel, curp, ine ->
                onEditarUsuario(u.idUsuario, n, ap, am, tel, curp, ine)
                editarUsuario = null
                onLoad("Cliente")
            }
        )
    }
}