package com.moon.casaprestamo.presentation.admin.cuentas

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
import com.moon.casaprestamo.ui.components.cuentas.*

@Composable
fun AdminCuentasContent(
    uiState:         CuentasUiState,
    onLoad:          (String) -> Unit,
    onCrearEmpleado: (String, String, String, String, String, String, String) -> Unit,
    onCrearCliente:  (String, String, String, String, String, String, String, String) -> Unit,
    onEditarUsuario: (Int, String, String, String, String, String, String) -> Unit,
    onToggleEstado:  (Int, Boolean) -> Unit
) {
    var tabPersonal   by rememberSaveable { mutableStateOf(true) }
    var query         by rememberSaveable { mutableStateOf("") }
    var showNuevo     by remember { mutableStateOf(false) }
    var editarUsuario by remember { mutableStateOf<UsuarioResumen?>(null) }

    val rol = if (tabPersonal) "Empleado" else "Cliente"
    LaunchedEffect(tabPersonal) { onLoad(rol) }

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
                    color    = if (tabPersonal) Color(0xFF0B1736) else MaterialTheme.colorScheme.primary,
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.PersonSearch, null, tint = Color.White, modifier = Modifier.padding(12.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    if (tabPersonal) "Gestión de Personal" else "Directorio de Clientes",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                )
            }
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
                    placeholder   = { Text("Buscar...", fontSize = 14.sp) },
                    leadingIcon   = { Icon(Icons.Default.Search, null, Modifier.size(20.dp)) },
                    trailingIcon  = {
                        IconButton(onClick = { tabPersonal = !tabPersonal }) {
                            Icon(Icons.Default.SwapHoriz, null)
                        }
                    },
                    modifier   = Modifier.fillMaxWidth().padding(16.dp),
                    singleLine = true,
                    shape      = RoundedCornerShape(16.dp),
                    colors     = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeaderCell("IDENTIDAD", Modifier.weight(1.8f))
                    HeaderCell("DOC",       Modifier.weight(1.8f))
                    HeaderCell("REGISTRO",  Modifier.weight(1f))
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filtrados, key = { it.idUsuario }) { u ->
                                CuentaRow(
                                    usuario    = u,
                                    isPersonal = tabPersonal,
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
    if (showNuevo) {
        NuevoRegistroDialog(
            onDismiss       = { showNuevo = false },
            onCrearEmpleado = { n, ap, am, em, tel, pass, rolSel ->
                onCrearEmpleado(n, ap, am, em, tel, pass, rolSel)
                showNuevo = false
                onLoad(rol)
            },
            onCrearCliente = { n, ap, am, em, tel, curp, ine, pass ->
                onCrearCliente(n, ap, am, em, tel, curp, ine, pass)
                showNuevo = false
                onLoad(rol)
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
                onLoad(rol)
            }
        )
    }
}