package com.moon.casaprestamo.presentation.admin.cuentas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moon.casaprestamo.data.models.UsuarioResumen

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
            it.nombre,
            it.apellidoPaterno,
            it.apellidoMaterno,
            it.email,
            it.curp,
            it.noIdentificacion
        ).joinToString(" ").contains(query, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp), // Controlamos el margen externo
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = if (tabPersonal) Color(0xFF0B1736) else MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        Icons.Default.PersonSearch,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(Modifier.size(12.dp))
                Text(
                    if (tabPersonal) "Gestión de Personal" else "Directorio de Clientes",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black
                    ),
                    modifier = Modifier.padding(0.dp)
                )
            }

            Button(
                onClick = { showNuevo = true },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(Modifier.size(6.dp))
                Text("AGREGAR", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text(if (tabPersonal) "Buscar empleado..." else "Buscar cliente...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    IconButton(onClick = { tabPersonal = !tabPersonal }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Cambiar pestaña")
                    }
                }

                HeaderRow()

                when {
                    uiState.isLoading -> CircularProgressIndicator()
                    uiState.error != null -> Text("Error: ${uiState.error}")
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filtrados) { u ->
                            CuentaRow(
                                usuario = u,
                                onEditar = { editarUsuario = u },
                                onToggleEstado = { onToggleEstado(u.idUsuario, !u.activo) }
                            )
                        }
                    }
                }
            }
        }

        uiState.mensaje?.let {
            Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }

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
            usuario = u,
            onDismiss = { editarUsuario = null },
            onGuardar = { n, ap, am, tel, curp, ine ->
                onEditarUsuario(u.idUsuario, n, ap, am, tel, curp, ine)
                editarUsuario = null
                onLoad(rol)
            }
        )
    }
}

private fun generarClaveTemporal(length: Int = 10): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789"
    return (1..length).map { chars.random() }.joinToString("")
}

@Composable
private fun HeaderRow() {
    Row(Modifier.fillMaxWidth()) {
        Text("IDENTIDAD", modifier = Modifier.weight(1.6f), fontWeight = FontWeight.Black)
        Text("DOCS", modifier = Modifier.weight(1.1f), fontWeight = FontWeight.Black)
        Text("REGISTRO", modifier = Modifier.weight(1f), fontWeight = FontWeight.Black)
        Text("ACCIONES", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Black)
    }
}

@Composable
private fun CuentaRow(usuario: UsuarioResumen, onEditar: () -> Unit, onToggleEstado: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1.6f)) {
            Text(
                listOfNotNull(usuario.nombre, usuario.apellidoPaterno, usuario.apellidoMaterno).joinToString(" "),
                fontWeight = FontWeight.Bold
            )
            Text(usuario.email, color = MaterialTheme.colorScheme.outline)
        }
        Column(modifier = Modifier.weight(1.1f)) {
            Text(usuario.curp ?: "ACCESO SISTEMA", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Text("ID: ${usuario.noIdentificacion ?: "-"}", color = MaterialTheme.colorScheme.outline)
        }
        Text(usuario.fechaRegistro?.take(10) ?: "-", modifier = Modifier.weight(1f))
        Row(modifier = Modifier.weight(0.8f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
            IconButton(onClick = onToggleEstado) { Icon(Icons.Default.DeleteOutline, contentDescription = "Desactivar") }
        }
    }
}

@Composable
private fun NuevoRegistroDialog(
    onDismiss: () -> Unit,
    onCrearEmpleado: (String, String, String, String, String, String, String) -> Unit,
    onCrearCliente: (String, String, String, String, String, String, String, String) -> Unit
) {
    var tabCliente by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var apP by remember { mutableStateOf("") }
    var apM by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var curp by remember { mutableStateOf("") }
    var ine by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var rolEmpleado by remember { mutableStateOf("Empleado") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                if (tabCliente) onCrearCliente(nombre, apP, apM, email, telefono, curp, ine, clave)
                else onCrearEmpleado(nombre, apP, apM, email, telefono, clave, rolEmpleado)
            }) { Text("CONFIRMAR") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } },
        title = { Text("Nuevo Registro", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = { tabCliente = false }, modifier = Modifier.weight(1f)) { Text("PERSONAL") }
                    FilledTonalButton(onClick = { tabCliente = true }, modifier = Modifier.weight(1f)) { Text("CLIENTE") }
                }
                OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre(s)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(apP, { apP = it }, label = { Text("Apellido paterno") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(apM, { apM = it }, label = { Text("Apellido materno") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(email, { email = it }, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(telefono, { telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
                if (tabCliente) {
                    OutlinedTextField(curp, { curp = it }, label = { Text("CURP") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(ine, { ine = it }, label = { Text("No. identificación") }, modifier = Modifier.fillMaxWidth())
                } else {
                    OutlinedButton(onClick = { rolEmpleado = if (rolEmpleado == "Empleado") "Admin" else "Empleado" }) {
                        Text("Rol: $rolEmpleado")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = clave,
                        onValueChange = {},
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        label = { Text("Contraseña temporal") }
                    )
                    OutlinedButton(onClick = { clave = generarClaveTemporal() }) { Text("GENERAR CLAVE") }
                }
            }
        }
    )
}

@Composable
private fun EditarUsuarioDialog(
    usuario: UsuarioResumen,
    onDismiss: () -> Unit,
    onGuardar: (String, String, String, String, String, String) -> Unit
) {
    var nombre by remember(usuario.idUsuario) { mutableStateOf(usuario.nombre) }
    var apP by remember(usuario.idUsuario) { mutableStateOf(usuario.apellidoPaterno) }
    var apM by remember(usuario.idUsuario) { mutableStateOf(usuario.apellidoMaterno ?: "") }
    var tel by remember(usuario.idUsuario) { mutableStateOf(usuario.telefono ?: "") }
    var curp by remember(usuario.idUsuario) { mutableStateOf(usuario.curp ?: "") }
    var ine by remember(usuario.idUsuario) { mutableStateOf(usuario.noIdentificacion ?: "") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = { onGuardar(nombre, apP, apM, tel, curp, ine) }) { Text("GUARDAR") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } },
        title = { Text("Editar usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(apP, { apP = it }, label = { Text("Apellido paterno") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(apM, { apM = it }, label = { Text("Apellido materno") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(tel, { tel = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(curp, { curp = it }, label = { Text("CURP") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(ine, { ine = it }, label = { Text("No. identificación") }, modifier = Modifier.fillMaxWidth())
            }
        }
    )
}
