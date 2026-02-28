package com.moon.casaprestamo.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moon.casaprestamo.data.models.CrearEmpleadoRequest
import com.moon.casaprestamo.data.models.EditarUsuarioAdminRequest
import com.moon.casaprestamo.data.models.RegistroRequest
import com.moon.casaprestamo.data.models.UsuarioResumen
import com.moon.casaprestamo.data.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CuentasUiState(
    val isLoading: Boolean = false,
    val usuarios: List<UsuarioResumen> = emptyList(),
    val error: String? = null,
    val mensaje: String? = null
)

private fun generarClaveTemporal(length: Int = 10): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789"
    return (1..length).map { chars.random() }.joinToString("")
}

@HiltViewModel
class AdminCuentasViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(CuentasUiState())
    val uiState: StateFlow<CuentasUiState> = _uiState.asStateFlow()

    fun cargar(rol: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, mensaje = null) }
            try {
                val response = apiService.obtenerUsuarios(rol = rol)
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(isLoading = false, usuarios = response.body()?.usuarios.orEmpty())
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Error HTTP ${response.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Error de red") }
            }
        }
    }

    fun crearEmpleado(
        nombre: String,
        apellidoPaterno: String,
        apellidoMaterno: String,
        email: String,
        telefono: String,
        password: String,
        rol: String
    ) {
        viewModelScope.launch {
            try {
                val r = apiService.crearEmpleado(
                    CrearEmpleadoRequest(
                        nombre = nombre,
                        apellidoPaterno = apellidoPaterno,
                        apellidoMaterno = apellidoMaterno.ifBlank { null },
                        email = email,
                        password = password,
                        telefono = telefono.ifBlank { null },
                        rol = rol
                    )
                )
                _uiState.update { it.copy(mensaje = if (r.isSuccessful) "Registro creado" else "No se pudo crear") }
            } catch (_: Exception) {
                _uiState.update { it.copy(mensaje = "Error al crear") }
            }
        }
    }

    fun crearCliente(
        nombre: String,
        apellidoPaterno: String,
        apellidoMaterno: String,
        email: String,
        telefono: String,
        curp: String,
        ine: String,
        password: String
    ) {
        viewModelScope.launch {
            try {
                val r = apiService.registrarCliente(
                    RegistroRequest(
                        nombre = nombre,
                        apellido_paterno = apellidoPaterno,
                        apellido_materno = apellidoMaterno.ifBlank { null },
                        email = email,
                        password = password,
                        curp = curp.ifBlank { null },
                        telefono = telefono.ifBlank { null },
                        direccion = null,
                        no_identificacion = ine.ifBlank { null },
                        fecha_nacimiento = null
                    )
                )
                _uiState.update { it.copy(mensaje = if (r.isSuccessful) "Cliente creado" else "No se pudo crear") }
            } catch (_: Exception) {
                _uiState.update { it.copy(mensaje = "Error al crear") }
            }
        }
    }

    fun editarUsuario(
        idUsuario: Int,
        nombre: String,
        apellidoPaterno: String,
        apellidoMaterno: String,
        telefono: String,
        curp: String,
        ine: String
    ) {
        viewModelScope.launch {
            try {
                val r = apiService.editarUsuarioAdmin(
                    idUsuario,
                    EditarUsuarioAdminRequest(
                        nombre = nombre,
                        apellidoPaterno = apellidoPaterno,
                        apellidoMaterno = apellidoMaterno.ifBlank { null },
                        telefono = telefono.ifBlank { null },
                        direccion = null,
                        curp = curp.ifBlank { null },
                        noIdentificacion = ine.ifBlank { null }
                    )
                )
                _uiState.update { it.copy(mensaje = if (r.isSuccessful) "Usuario actualizado" else "No se pudo actualizar") }
            } catch (_: Exception) {
                _uiState.update { it.copy(mensaje = "Error al actualizar") }
            }
        }
    }

    fun cambiarEstado(idUsuario: Int, activo: Boolean) {
        viewModelScope.launch {
            try {
                val r = apiService.cambiarEstadoUsuario(idUsuario, activo)
                _uiState.update { it.copy(mensaje = if (r.isSuccessful) "Estado actualizado" else "No se pudo actualizar estado") }
            } catch (_: Exception) {
                _uiState.update { it.copy(mensaje = "Error al cambiar estado") }
            }
        }
    }
}

@Composable
fun AdminCuentasContent(viewModel: AdminCuentasViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    var tabPersonal by rememberSaveable { mutableStateOf(true) }
    var query by rememberSaveable { mutableStateOf("") }
    var showNuevo by remember { mutableStateOf(false) }
    var editarUsuario by remember { mutableStateOf<UsuarioResumen?>(null) }

    val rol = if (tabPersonal) "Empleado" else "Cliente"

    LaunchedEffect(tabPersonal) { viewModel.cargar(rol) }

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
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    color = if (tabPersonal) Color(0xFF0B1736) else Color(0xFFB0003A),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        Icons.Default.PersonSearch,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Text(
                    if (tabPersonal) "Gestión de Personal" else "Directorio de Clientes",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
            }

            Button(
                onClick = { showNuevo = true },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0003A))
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
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                onToggleEstado = { viewModel.cambiarEstado(u.idUsuario, !u.activo) }
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
                viewModel.crearEmpleado(n, ap, am, em, tel, pass, rolSel)
                showNuevo = false
                viewModel.cargar(rol)
            },
            onCrearCliente = { n, ap, am, em, tel, curp, ine, pass ->
                viewModel.crearCliente(n, ap, am, em, tel, curp, ine, pass)
                showNuevo = false
                viewModel.cargar(rol)
            }
        )
    }

    editarUsuario?.let { u ->
        EditarUsuarioDialog(
            usuario = u,
            onDismiss = { editarUsuario = null },
            onGuardar = { n, ap, am, tel, curp, ine ->
                viewModel.editarUsuario(u.idUsuario, n, ap, am, tel, curp, ine)
                editarUsuario = null
                viewModel.cargar(rol)
            }
        )
    }
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
private fun CuentaRow(
    usuario: UsuarioResumen,
    onEditar: () -> Unit,
    onToggleEstado: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1.6f)) {
            Text(
                listOfNotNull(usuario.nombre, usuario.apellidoPaterno, usuario.apellidoMaterno).joinToString(" "),
                fontWeight = FontWeight.Bold
            )
            Text(usuario.email, color = MaterialTheme.colorScheme.outline)
        }
        Column(modifier = Modifier.weight(1.1f)) {
            Text(usuario.curp ?: "ACCESO SISTEMA", color = Color(0xFFB0003A), fontWeight = FontWeight.SemiBold)
            Text("ID: ${usuario.noIdentificacion ?: "-"}", color = MaterialTheme.colorScheme.outline)
        }
        Text(usuario.fechaRegistro?.take(10) ?: "-", modifier = Modifier.weight(1f))
        Row(modifier = Modifier.weight(0.8f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onToggleEstado) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Desactivar")
            }
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
                if (tabCliente) {
                    onCrearCliente(nombre, apP, apM, email, telefono, curp, ine, clave)
                } else {
                    onCrearEmpleado(nombre, apP, apM, email, telefono, clave, rolEmpleado)
                }
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { rolEmpleado = if (rolEmpleado == "Empleado") "Admin" else "Empleado" }) {
                            Text("Rol: $rolEmpleado")
                        }
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