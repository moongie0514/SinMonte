package com.moon.casaprestamo.presentation.admin.cuentas

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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

// ─────────────────────────────────────────────────────────────
// CONTENT PRINCIPAL
// ─────────────────────────────────────────────────────────────

@Composable
fun AdminCuentasContent(
    uiState: CuentasUiState,
    onLoad: (String) -> Unit,
    onCrearEmpleado: (String, String, String, String, String, String, String) -> Unit,
    onCrearCliente: (String, String, String, String, String, String, String, String) -> Unit,
    onEditarUsuario: (Int, String, String, String, String, String, String) -> Unit,
    onToggleEstado: (Int, Boolean) -> Unit
) {
    var tabPersonal by rememberSaveable { mutableStateOf(true) }
    var query by rememberSaveable { mutableStateOf("") }
    var showNuevo by remember { mutableStateOf(false) }
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
        modifier = Modifier.fillMaxSize().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(50.dp), // Ajustado de 60 a 50 para balance
                    color = if (tabPersonal) Color(0xFF0B1736) else MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(14.dp)
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
                onClick = { showNuevo = true },
                modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }

        // --- BUSCADOR + TABLA ---
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Buscador
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        IconButton(onClick = { tabPersonal = !tabPersonal }) {
                            Icon(Icons.Default.SwapHoriz, null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                )

                // Cabecera Tabla
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeaderCell("IDENTIDAD", Modifier.weight(1.8f))
                    HeaderCell("DOC", Modifier.weight(1.8f))
                    HeaderCell("REGISTRO", Modifier.weight(1f))
                }

                // Lista Scrolleable
                Box(modifier = Modifier.fillMaxSize()) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filtrados, key = { it.idUsuario }) { u ->
                                // Usamos la nueva CuentaRow sin menú de 3 puntos
                                CuentaRow(
                                    usuario = u,
                                    isPersonal = tabPersonal,
                                    onEditar = { editarUsuario = u },
                                    onEliminar = {},
                                    onToggle = { onToggleEstado(u.idUsuario, !u.activo) }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }
        }

        // Mensaje de feedback
        uiState.mensaje?.let {
            Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }

    // ── DIALOGS ───────────────────────────────────────────────
    if (showNuevo) {
        NuevoRegistroDialog(
            onDismiss = { showNuevo = false },
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

// ─────────────────────────────────────────────────────────────
// FILA DE TABLA
// ─────────────────────────────────────────────────────────────

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CuentaRow(
    usuario: UsuarioResumen,
    isPersonal: Boolean,
    onEditar: () -> Unit,
    onToggle: () -> Unit,
    onEliminar: () -> Unit // Agregamos la acción de eliminar
) {
    val nombreCompleto = listOfNotNull(usuario.nombre, usuario.apellidoPaterno, usuario.apellidoMaterno)
        .joinToString(" ").trim()

    // --- LÓGICA DE SALTO DE LÍNEA INTELIGENTE ---
    // Agregamos un salto de línea antes del @ en el correo
    val emailFormateado = usuario.email.replace("@", "\n@")

    // Agregamos un salto de línea antes del último guion en la fecha (ej: 2026-02\n-28)
    val fechaRaw = usuario.fechaRegistro?.take(10) ?: "-"
    val fechaFormateada = if (fechaRaw.length > 8) {
        val lastDashIndex = fechaRaw.lastIndexOf("-")
        fechaRaw.substring(0, lastDashIndex) + "\n" + fechaRaw.substring(lastDashIndex)
    } else {
        fechaRaw
    }

    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* Opcional: ver detalles */ },
                onLongClick = { showMenu = true }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp) // Espacio físico entre columnas
    ) {
        // Columna Identidad (1.8f)
        Column(modifier = Modifier.weight(1.8f)) {
            Text(
                nombreCompleto,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, lineHeight = 14.sp)
            )
            Text(
                emailFormateado,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 12.sp),
                color = Color.Gray
            )
        }

        // Columna Documento (1.8f)
        Column(modifier = Modifier.weight(1.8f)) {
            if (!usuario.curp.isNullOrBlank()) {
                Text(
                    usuario.curp!!,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    maxLines = 2
                )
            }
            if (isPersonal) {
                EstadoChip(usuario.activo)
            }
        }

        // Columna Fecha (1f)
        Text(
            text = fechaFormateada,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 12.sp),
            color = Color.Gray
        )

        // --- MENÚ CON TODAS LAS ACCIONES ---
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("Editar") },
                onClick = { showMenu = false; onEditar() },
                leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp)) }
            )
            DropdownMenuItem(
                text = { Text(if (usuario.activo) "Desactivar" else "Activar") },
                onClick = { showMenu = false; onToggle() },
                leadingIcon = {
                    Icon(
                        if (usuario.activo) Icons.Default.PersonOff else Icons.Default.PersonAdd,
                        null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            DropdownMenuItem(
                text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                onClick = { showMenu = false; onEliminar() },
                leadingIcon = {
                    Icon(
                        Icons.Default.DeleteOutline,
                        null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }
}
// ─────────────────────────────────────────────────────────────
// CHIP DE ESTADO
// ─────────────────────────────────────────────────────────────

@Composable
private fun EstadoChip(activo: Boolean) {
    val bgColor = if (activo)
        Color(0xFF10B981).copy(alpha = 0.12f)
    else
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)

    val textColor = if (activo)
        Color(0xFF059669)
    else
        MaterialTheme.colorScheme.error

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            if (activo) "Activo" else "Inactivo",
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────

@Composable
private fun HeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 0.8.sp,
        color = MaterialTheme.colorScheme.outline
    )
}

private fun generarClaveTemporal(length: Int = 10): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789"
    return (1..length).map { chars.random() }.joinToString("")
}

// ─────────────────────────────────────────────────────────────
// DIALOG: NUEVO REGISTRO
// ─────────────────────────────────────────────────────────────

@Composable
private fun NuevoRegistroDialog(
    onDismiss: () -> Unit,
    onCrearEmpleado: (String, String, String, String, String, String, String) -> Unit,
    onCrearCliente: (String, String, String, String, String, String, String, String) -> Unit
) {
    var tabCliente by remember { mutableStateOf(false) }
    var nombre     by remember { mutableStateOf("") }
    var apP        by remember { mutableStateOf("") }
    var apM        by remember { mutableStateOf("") }
    var email      by remember { mutableStateOf("") }
    var telefono   by remember { mutableStateOf("") }
    var curp       by remember { mutableStateOf("") }
    var ine        by remember { mutableStateOf("") }
    var clave      by remember { mutableStateOf("") }
    var rolEmp     by remember { mutableStateOf("Empleado") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                if (tabCliente)
                    onCrearCliente(nombre, apP, apM, email, telefono, curp, ine, clave)
                else
                    onCrearEmpleado(nombre, apP, apM, email, telefono, clave, rolEmp)
            }) { Text("CONFIRMAR") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } },
        title = { Text("Nuevo Registro", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // Selector de tipo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !tabCliente,
                        onClick  = { tabCliente = false },
                        label    = { Text("Personal") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = tabCliente,
                        onClick  = { tabCliente = true },
                        label    = { Text("Cliente") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(nombre,   { nombre = it },   label = { Text("Nombre(s)") },        modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(apP,      { apP = it },      label = { Text("Apellido paterno") },  modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(apM,      { apM = it },      label = { Text("Apellido materno") },  modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(email,    { email = it },    label = { Text("Correo") },            modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(telefono, { telefono = it }, label = { Text("Teléfono") },          modifier = Modifier.fillMaxWidth(), singleLine = true)

                if (tabCliente) {
                    OutlinedTextField(curp, { curp = it }, label = { Text("CURP") },              modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(ine,  { ine = it },  label = { Text("No. identificación") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                } else {
                    // Toggle de rol con FilterChip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = rolEmp == "Empleado",
                            onClick  = { rolEmp = "Empleado" },
                            label    = { Text("Empleado") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = rolEmp == "Admin",
                            onClick  = { rolEmp = "Admin" },
                            label    = { Text("Admin") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Contraseña temporal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value         = clave,
                        onValueChange = {},
                        modifier      = Modifier.weight(1f),
                        readOnly      = true,
                        label         = { Text("Contraseña temporal") },
                        singleLine    = true
                    )
                    OutlinedButton(
                        onClick = { clave = generarClaveTemporal() },
                        modifier = Modifier.height(56.dp)
                    ) { Text("GENERAR", fontSize = 11.sp) }
                }
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────
// DIALOG: EDITAR USUARIO
// ─────────────────────────────────────────────────────────────

@Composable
private fun EditarUsuarioDialog(
    usuario: UsuarioResumen,
    onDismiss: () -> Unit,
    onGuardar: (String, String, String, String, String, String) -> Unit
) {
    var nombre by remember(usuario.idUsuario) { mutableStateOf(usuario.nombre) }
    var apP    by remember(usuario.idUsuario) { mutableStateOf(usuario.apellidoPaterno) }
    var apM    by remember(usuario.idUsuario) { mutableStateOf(usuario.apellidoMaterno ?: "") }
    var tel    by remember(usuario.idUsuario) { mutableStateOf(usuario.telefono ?: "") }
    var curp   by remember(usuario.idUsuario) { mutableStateOf(usuario.curp ?: "") }
    var ine    by remember(usuario.idUsuario) { mutableStateOf(usuario.noIdentificacion ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onGuardar(nombre, apP, apM, tel, curp, ine) }) { Text("GUARDAR") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } },
        title = {
            Column {
                Text("Editar usuario", fontWeight = FontWeight.Black)
                Text(
                    usuario.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") },            modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(apP,   { apP = it },    label = { Text("Apellido Paterno") },   modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(apM,   { apM = it },    label = { Text("Apellido Materno") },   modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(tel,   { tel = it },    label = { Text("Teléfono") },           modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(curp,  { curp = it },   label = { Text("CURP") },               modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(ine,   { ine = it },    label = { Text("No. identificación") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        }
    )
}