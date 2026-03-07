package com.moon.casaprestamo.auth

import android.app.DatePickerDialog
import android.os.Build
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moon.casaprestamo.data.models.RegistroUiState
import com.moon.casaprestamo.ui.components.inputs.MonteInput
import com.moon.casaprestamo.ui.theme.CasaPrestamoTheme
import java.util.Calendar

// ══════════════════════════════════════════════════════════════════
// FILTROS DE TEXTO — aplicados en onValueChange antes del evento
// ══════════════════════════════════════════════════════════════════

private object Filtro {

    // Letras (incluyendo acentos y ñ), espacios. Sin números ni símbolos.
    fun nombre(input: String): String =
        input.filter { it.isLetter() || it == ' ' }

    // A-Z, 0-9 únicamente (CURP solo tiene estos caracteres)
    fun curp(input: String): String =
        input.filter { it in 'A'..'Z' || it in '0'..'9' }

    // Solo dígitos, máximo 10
    fun telefono(input: String): String =
        input.filter { it.isDigit() }.take(10)

    // Letras, números, espacios y puntuación de dirección: , . # - /
    fun direccion(input: String): String =
        input.filter { it.isLetterOrDigit() || it in " ,.#-/" }

    // Letras, números y guión — sirve para INE, Pasaporte y Cédula
    fun identificacion(input: String): String =
        input.filter { it.isLetterOrDigit() || it == '-' }

    // Email: caracteres válidos de RFC 5321, sin espacios ni emojis
    fun email(input: String): String =
        input.filter { it.code in 33..126 }   // ASCII imprimible, sin espacio

    // Contraseña: ASCII imprimible sin espacios (permite símbolos especiales)
    fun password(input: String): String =
        input.filter { it.code in 33..126 }
}

// ══════════════════════════════════════════════════════════════════
// SCREEN
// ══════════════════════════════════════════════════════════════════

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegistroUsuarioScreen(
    onBack: () -> Unit,
    onRegistroExitoso: (email: String) -> Unit,
    viewModel: RegistroViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.registroExitoso) {
        if (uiState.registroExitoso) onRegistroExitoso(uiState.emailParaVerificar)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape  = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp, vertical = 32.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RegisterContentView(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    onBack  = onBack
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// CONTENT
// ══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterContentView(
    uiState: RegistroUiState,
    onEvent: (RegistroEvent) -> Unit,
    onBack: () -> Unit
) {
    val context  = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            val m = if (month + 1 < 10) "0${month + 1}" else "${month + 1}"
            val d = if (day < 10) "0$day" else "$day"
            onEvent(RegistroEvent.FechaNacimientoChanged("$year-$m-$d"))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.maxDate = System.currentTimeMillis()
    }

    Text(
        "REGISTRO DE CUENTA",
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp
        )
    )
    Spacer(Modifier.height(24.dp))

    // ════════════════════════════════════════
    // DATOS PERSONALES
    // ════════════════════════════════════════
    SectionLabel("DATOS PERSONALES")

    MonteInput(
        value         = uiState.nombre,
        onValueChange = { onEvent(RegistroEvent.NombreChanged(Filtro.nombre(it))) },
        placeholder   = "NOMBRE(S) *",
        leadingIcon   = Icons.Default.Person
    )
    Spacer(Modifier.height(8.dp))

    MonteInput(
        value         = uiState.apellidoPaterno,
        onValueChange = { onEvent(RegistroEvent.ApellidoPaternoChanged(Filtro.nombre(it))) },
        placeholder   = "APELLIDO PATERNO *",
        leadingIcon   = Icons.Default.Person
    )
    Spacer(Modifier.height(8.dp))

    MonteInput(
        value         = uiState.apellidoMaterno,
        onValueChange = { onEvent(RegistroEvent.ApellidoMaternoChanged(Filtro.nombre(it))) },
        placeholder   = "APELLIDO MATERNO",
        leadingIcon   = Icons.Default.Person
    )
    Spacer(Modifier.height(8.dp))

    // CURP — solo A-Z y 0-9, máx 18, contador
    CampoConContador(
        valor         = uiState.curp,
        limite        = 18,
        unidad        = "caracteres",
        onValueChange = {
            val filtrado = Filtro.curp(it.uppercase()).take(18)
            onEvent(RegistroEvent.CurpChanged(filtrado))
        },
        placeholder   = "CURP *",
        leadingIcon   = Icons.Default.Fingerprint
    )
    Spacer(Modifier.height(8.dp))

    // Teléfono — solo dígitos, máx 10, contador
    CampoConContador(
        valor         = uiState.telefono,
        limite        = 10,
        unidad        = "dígitos",
        onValueChange = { onEvent(RegistroEvent.TelefonoChanged(Filtro.telefono(it))) },
        placeholder   = "TELÉFONO CELULAR *",
        leadingIcon   = Icons.Default.Phone,
        keyboardType  = KeyboardType.Phone
    )
    Spacer(Modifier.height(8.dp))

    MonteInput(
        value         = uiState.direccion,
        onValueChange = { onEvent(RegistroEvent.DireccionChanged(Filtro.direccion(it))) },
        placeholder   = "DIRECCIÓN COMPLETA *",
        leadingIcon   = Icons.Default.Home
    )
    Spacer(Modifier.height(8.dp))

    Box(modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }) {
        MonteInput(
            value         = uiState.fechaNacimiento.ifBlank { "" },
            onValueChange = {},
            placeholder   = "FECHA DE NACIMIENTO * (SELECCIONAR)",
            leadingIcon   = Icons.Default.CalendarToday,
            enabled       = false,
            readOnly      = true
        )
    }
    Text(
        "Debes ser mayor de 18 años",
        fontSize = 10.sp,
        color    = MaterialTheme.colorScheme.outline,
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp, bottom = 8.dp)
    )

    HorizontalDivider(
        modifier  = Modifier.padding(vertical = 16.dp),
        thickness = 1.dp,
        color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    )

    // ════════════════════════════════════════
    // IDENTIFICACIÓN OFICIAL
    // ════════════════════════════════════════
    SectionLabel("IDENTIFICACIÓN OFICIAL *")

    var dropdownExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded         = dropdownExpanded,
        onExpandedChange = { dropdownExpanded = it },
        modifier         = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value         = uiState.tipoId.label,
            onValueChange = {},
            readOnly      = true,
            label         = { Text("TIPO DE IDENTIFICACIÓN") },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
            modifier      = Modifier.fillMaxWidth().menuAnchor(),
            shape         = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded         = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false }
        ) {
            TipoIdentificacion.entries.forEach { tipo ->
                DropdownMenuItem(
                    text    = { Text(tipo.label) },
                    onClick = {
                        onEvent(RegistroEvent.TipoIdChanged(tipo))
                        dropdownExpanded = false
                    }
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))

    MonteInput(
        value         = uiState.numeroId,
        onValueChange = { onEvent(RegistroEvent.NumeroIdChanged(Filtro.identificacion(it))) },
        placeholder   = "NÚMERO DE ${uiState.tipoId.label.uppercase()} *",
        leadingIcon   = Icons.Default.AssignmentInd
    )

    HorizontalDivider(
        modifier  = Modifier.padding(vertical = 16.dp),
        thickness = 1.dp,
        color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    )

    // ════════════════════════════════════════
    // DATOS DE ACCESO
    // ════════════════════════════════════════
    SectionLabel("DATOS DE ACCESO")

    MonteInput(
        value         = uiState.email,
        onValueChange = { onEvent(RegistroEvent.EmailChanged(Filtro.email(it))) },
        placeholder   = "CORREO ELECTRÓNICO *",
        leadingIcon   = Icons.Default.Email,
        keyboardType  = KeyboardType.Email
    )
    Spacer(Modifier.height(8.dp))

    var passVisible by remember { mutableStateOf(false) }
    MonteInput(
        value            = uiState.password,
        onValueChange    = { onEvent(RegistroEvent.PasswordChanged(Filtro.password(it))) },
        placeholder      = "CREAR CONTRASEÑA *",
        leadingIcon      = Icons.Default.Lock,
        isPassword       = true,
        passwordVisible  = passVisible,
        onPasswordToggle = { passVisible = !passVisible }
    )
    PasswordRequirements(password = uiState.password)

    Spacer(Modifier.height(32.dp))

    val habilitado = !uiState.isLoading && RegistroValidacion.formularioCompleto(uiState)

    Button(
        onClick  = { onEvent(RegistroEvent.RegistrarClicked) },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape    = RoundedCornerShape(16.dp),
        enabled  = habilitado
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
        } else {
            Text(
                "CREAR MI CUENTA",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black)
            )
        }
    }

    TextButton(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿YA TE REGISTRASTE? ",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.outline)
            Text("INICIA SESIÓN",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.primary)
        }
    }

    if (uiState.errorMessage != null) {
        Text(
            text     = uiState.errorMessage!!,
            color    = MaterialTheme.colorScheme.error,
            style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

// ══════════════════════════════════════════════════════════════════
// Campo con contador — gris → naranja → verde según avance
// ══════════════════════════════════════════════════════════════════
@Composable
private fun CampoConContador(
    valor:         String,
    limite:        Int,
    unidad:        String,
    onValueChange: (String) -> Unit,
    placeholder:   String,
    leadingIcon:   androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType:  KeyboardType = KeyboardType.Text
) {
    val completo = valor.length == limite
    val parcial  = valor.isNotEmpty() && !completo

    val colorContador = when {
        completo -> Color(0xFF10B981)
        parcial  -> Color(0xFFF59E0B)
        else     -> MaterialTheme.colorScheme.outline
    }

    val textoContador = when {
        completo -> "${limite}/${limite} $unidad ✓"
        parcial  -> "${valor.length}/${limite} $unidad — faltan ${limite - valor.length}"
        else     -> "0/${limite} $unidad"
    }

    Column {
        MonteInput(
            value         = valor,
            onValueChange = onValueChange,
            placeholder   = placeholder,
            leadingIcon   = leadingIcon,
            keyboardType  = keyboardType
        )
        Text(
            text       = textoContador,
            fontSize   = 10.sp,
            color      = colorContador,
            fontWeight = if (completo) FontWeight.Bold else FontWeight.Normal,
            modifier   = Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp)
        )
    }
}

// ══════════════════════════════════════════════════════════════════
// Requisitos de contraseña en tiempo real
// ══════════════════════════════════════════════════════════════════
@Composable
private fun PasswordRequirements(password: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Requisito("Mínimo 8 caracteres",          password.length >= 8)
        Requisito("Al menos una mayúscula",        password.any { it.isUpperCase() })
        Requisito("Al menos un número",            password.any { it.isDigit() })
        Requisito("Al menos un carácter especial", password.any { !it.isLetterOrDigit() })
    }
}

@Composable
private fun Requisito(texto: String, cumplido: Boolean) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector        = if (cumplido) Icons.Default.CheckCircle
            else          Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint               = if (cumplido) Color(0xFF10B981)
            else          MaterialTheme.colorScheme.outline,
            modifier           = Modifier.size(12.dp)
        )
        Text(
            text     = texto,
            fontSize = 10.sp,
            color    = if (cumplido) Color(0xFF10B981) else MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun SectionLabel(texto: String) {
    Text(
        text     = texto,
        style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )
}

