package com.moon.casaprestamo.ui.components.cuentas

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.moon.casaprestamo.data.models.UsuarioResumen

private val Rojo   = Color(0xFFA6032F)
private val Verde  = Color(0xFF10B981)
private val Oscuro = Color(0xFF0F172A)

// ─── Encabezado de columna ────────────────────────────────
@Composable
fun HeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier      = modifier,
        fontWeight    = FontWeight.Bold,
        fontSize      = 10.sp,
        letterSpacing = 0.8.sp,
        color         = MaterialTheme.colorScheme.outline
    )
}

// ─── Chip de estado activo/inactivo ──────────────────────
@Composable
fun EstadoChip(activo: Boolean) {
    val bgColor   = if (activo) Verde.copy(alpha = 0.12f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
    val textColor = if (activo) Color(0xFF059669) else MaterialTheme.colorScheme.error
    Surface(color = bgColor, shape = RoundedCornerShape(50), modifier = Modifier.padding(top = 4.dp)) {
        Text(
            if (activo) "Activo" else "Inactivo",
            color    = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ─── Generador de clave temporal ─────────────────────────
fun generarClaveTemporal(length: Int = 10): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789"
    return (1..length).map { chars.random() }.joinToString("")
}

// ─── Fila de tabla ────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CuentaRow(
    usuario:    UsuarioResumen,
    isPersonal: Boolean,
    onEditar:   () -> Unit,
    onToggle:   () -> Unit,
    onEliminar: () -> Unit
) {
    val nombreCompleto = listOfNotNull(usuario.nombre, usuario.apellidoPaterno, usuario.apellidoMaterno)
        .joinToString(" ").trim()
    val emailFormateado = usuario.email.replace("@", "\n@")
    val fechaRaw = usuario.fechaRegistro?.take(10) ?: "-"
    val fechaFormateada = if (fechaRaw.length > 8) {
        val i = fechaRaw.lastIndexOf("-")
        fechaRaw.substring(0, i) + "\n" + fechaRaw.substring(i)
    } else fechaRaw

    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = { showMenu = true })
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1.8f)) {
            Text(nombreCompleto, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, lineHeight = 14.sp))
            Text(emailFormateado, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 12.sp), color = Color.Gray)
        }
        Column(modifier = Modifier.weight(1.8f)) {
            if (!usuario.curp.isNullOrBlank()) {
                Text(usuario.curp!!, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), maxLines = 2)
            }
            if (isPersonal) EstadoChip(usuario.activo)
        }
        Text(
            text     = fechaFormateada,
            modifier = Modifier.weight(1f),
            style    = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 12.sp),
            color    = Color.Gray
        )
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text        = { Text("Editar") },
                onClick     = { showMenu = false; onEditar() },
                leadingIcon = { Icon(Icons.Default.Edit, null, Modifier.size(20.dp)) }
            )
            DropdownMenuItem(
                text        = { Text(if (usuario.activo) "Desactivar" else "Activar") },
                onClick     = { showMenu = false; onToggle() },
                leadingIcon = { Icon(if (usuario.activo) Icons.Default.PersonOff else Icons.Default.PersonAdd, null, Modifier.size(20.dp)) }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            DropdownMenuItem(
                text        = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                onClick     = { showMenu = false; onEliminar() },
                leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) }
            )
        }
    }
}

// ─── Dialog: Editar usuario ───────────────────────────────
@Composable
fun EditarUsuarioDialog(
    usuario:   UsuarioResumen,
    onDismiss: () -> Unit,
    onGuardar: (String, String, String, String, String, String) -> Unit
) {
    var nombre by remember(usuario.idUsuario) { mutableStateOf(usuario.nombre) }
    var apP    by remember(usuario.idUsuario) { mutableStateOf(usuario.apellidoPaterno) }
    var apM    by remember(usuario.idUsuario) { mutableStateOf(usuario.apellidoMaterno ?: "") }
    var tel    by remember(usuario.idUsuario) { mutableStateOf(usuario.telefono ?: "") }
    var curp   by remember(usuario.idUsuario) { mutableStateOf(usuario.curp ?: "") }
    var ine    by remember(usuario.idUsuario) { mutableStateOf(usuario.noIdentificacion ?: "") }

    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape    = RoundedCornerShape(24.dp),
            color    = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column {
                // Header oscuro
                Box(
                    modifier = Modifier.fillMaxWidth().background(Oscuro).padding(20.dp)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("EDITAR USUARIO", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Text(
                                usuario.email,
                                color    = Rojo,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                    }
                }

                Column(
                    modifier            = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FormField("Nombre(s)",         nombre, { nombre = it })
                    FormField("Apellido Paterno",  apP,    { apP = it })
                    FormField("Apellido Materno",  apM,    { apM = it })
                    FormField("Teléfono",          tel,    { tel = it })

                    HorizontalDivider()
                    Text("Información protegida", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)

                    FormField("CURP",              curp,   { curp = it })
                    FormField("No. identificación",ine,    { ine = it })

                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(14.dp)) {
                            Text("CANCELAR", fontWeight = FontWeight.Black)
                        }
                        Button(
                            onClick  = { onGuardar(nombre, apP, apM, tel, curp, ine) },
                            modifier = Modifier.weight(2f).height(52.dp),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = Oscuro)
                        ) {
                            Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

// ─── Dialog: Nuevo cliente (solo cliente — para empleados) ─
@Composable
fun NuevoClienteDialog(
    onDismiss:     () -> Unit,
    onCrearCliente: (String, String, String, String, String, String, String, String) -> Unit
) {
    var nombre   by remember { mutableStateOf("") }
    var apP      by remember { mutableStateOf("") }
    var apM      by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var curp     by remember { mutableStateOf("") }
    var ine      by remember { mutableStateOf("") }
    var clave    by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape    = RoundedCornerShape(24.dp),
            color    = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column {
                // Header
                Box(modifier = Modifier.fillMaxWidth().background(Oscuro).padding(20.dp)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(Modifier.size(40.dp), CircleShape, Rojo.copy(alpha = 0.2f)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.PersonAdd, null, tint = Rojo, modifier = Modifier.size(20.dp))
                                }
                            }
                            Column {
                                Text("NUEVO CLIENTE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                Text("Registro de cuenta de cliente", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                    }
                }

                Column(
                    modifier            = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Datos personales
                    Text("DATOS PERSONALES", fontSize = 9.sp, fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.outline, letterSpacing = 1.sp)

                    FormField("Nombre(s)",        nombre,   { nombre = it })
                    FormField("Apellido Paterno", apP,      { apP = it })
                    FormField("Apellido Materno", apM,      { apM = it })
                    FormField("Correo electrónico", email,  { email = it })
                    FormField("Teléfono",         telefono, { telefono = it })

                    HorizontalDivider()
                    Text("IDENTIFICACIÓN", fontSize = 9.sp, fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.outline, letterSpacing = 1.sp)

                    FormField("CURP",              curp, { curp = it })
                    FormField("No. identificación", ine, { ine = it })

                    HorizontalDivider()
                    Text("ACCESO", fontSize = 9.sp, fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.outline, letterSpacing = 1.sp)

                    // Contraseña temporal
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value         = clave,
                            onValueChange = {},
                            modifier      = Modifier.weight(1f),
                            readOnly      = true,
                            label         = { Text("Contraseña temporal") },
                            singleLine    = true,
                            shape         = RoundedCornerShape(12.dp)
                        )
                        OutlinedButton(
                            onClick  = { clave = generarClaveTemporal() },
                            modifier = Modifier.height(56.dp),
                            shape    = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("GENERAR", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    if (clave.isNotBlank()) {
                        Surface(
                            color = Verde.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier              = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Key, null, Modifier.size(16.dp), tint = Verde)
                                Text(
                                    "Comparte esta clave con el cliente: $clave",
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = Verde
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(14.dp)) {
                            Text("CANCELAR", fontWeight = FontWeight.Black)
                        }
                        Button(
                            onClick  = { onCrearCliente(nombre, apP, apM, email, telefono, curp, ine, clave) },
                            modifier = Modifier.weight(2f).height(52.dp),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = Rojo),
                            enabled  = nombre.isNotBlank() && email.isNotBlank() && clave.isNotBlank()
                        ) {
                            Icon(Icons.Default.PersonAdd, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("CREAR CLIENTE", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

// ─── Dialog: Nuevo registro (admin — permite empleado y cliente) ──
@Composable
fun NuevoRegistroDialog(
    onDismiss:      () -> Unit,
    onCrearEmpleado: (String, String, String, String, String, String, String) -> Unit,
    onCrearCliente:  (String, String, String, String, String, String, String, String) -> Unit
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

    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape    = RoundedCornerShape(24.dp),
            color    = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column {
                // Header
                Box(modifier = Modifier.fillMaxWidth().background(Oscuro).padding(20.dp)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("NUEVO REGISTRO", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Text(
                                if (tabCliente) "Cuenta de cliente" else "Cuenta de personal",
                                color    = Rojo,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                    }
                }

                Column(
                    modifier            = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Selector tipo
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = !tabCliente, onClick = { tabCliente = false }, label = { Text("Personal") }, modifier = Modifier.weight(1f))
                        FilterChip(selected = tabCliente,  onClick = { tabCliente = true  }, label = { Text("Cliente")  }, modifier = Modifier.weight(1f))
                    }

                    Text("DATOS PERSONALES", fontSize = 9.sp, fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.outline, letterSpacing = 1.sp)

                    FormField("Nombre(s)",          nombre,   { nombre = it })
                    FormField("Apellido Paterno",   apP,      { apP = it })
                    FormField("Apellido Materno",   apM,      { apM = it })
                    FormField("Correo electrónico", email,    { email = it })
                    FormField("Teléfono",           telefono, { telefono = it })

                    if (tabCliente) {
                        HorizontalDivider()
                        Text("IDENTIFICACIÓN", fontSize = 9.sp, fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.outline, letterSpacing = 1.sp)
                        FormField("CURP",               curp, { curp = it })
                        FormField("No. identificación", ine,  { ine = it })
                    } else {
                        HorizontalDivider()
                        Text("ROL", fontSize = 9.sp, fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.outline, letterSpacing = 1.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = rolEmp == "Empleado", onClick = { rolEmp = "Empleado" }, label = { Text("Empleado") }, modifier = Modifier.weight(1f))
                            FilterChip(selected = rolEmp == "Admin",    onClick = { rolEmp = "Admin"    }, label = { Text("Admin")    }, modifier = Modifier.weight(1f))
                        }
                    }

                    HorizontalDivider()
                    Text("ACCESO", fontSize = 9.sp, fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.outline, letterSpacing = 1.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = clave, onValueChange = {}, modifier = Modifier.weight(1f),
                            readOnly = true, label = { Text("Contraseña temporal") },
                            singleLine = true, shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedButton(onClick = { clave = generarClaveTemporal() }, modifier = Modifier.height(56.dp), shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("GENERAR", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    if (clave.isNotBlank()) {
                        Surface(color = Verde.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Key, null, Modifier.size(16.dp), tint = Verde)
                                Text("Clave temporal: $clave", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Verde)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(14.dp)) {
                            Text("CANCELAR", fontWeight = FontWeight.Black)
                        }
                        Button(
                            onClick = {
                                if (tabCliente) onCrearCliente(nombre, apP, apM, email, telefono, curp, ine, clave)
                                else            onCrearEmpleado(nombre, apP, apM, email, telefono, clave, rolEmp)
                            },
                            modifier = Modifier.weight(2f).height(52.dp),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = Oscuro),
                            enabled  = nombre.isNotBlank() && email.isNotBlank() && clave.isNotBlank()
                        ) {
                            Text("CONFIRMAR", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

// ─── Campo de formulario reutilizable ─────────────────────
@Composable
fun FormField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        modifier      = Modifier.fillMaxWidth(),
        singleLine    = true,
        shape         = RoundedCornerShape(12.dp)
    )
}