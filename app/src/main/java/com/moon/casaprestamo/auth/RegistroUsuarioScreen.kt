package com.moon.casaprestamo.auth

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
import android.app.DatePickerDialog
import android.widget.DatePicker
import com.moon.casaprestamo.ui.components.inputs.MonteInput
import com.moon.casaprestamo.ui.theme.CasaPrestamoTheme
import java.util.Calendar

@Composable
fun RegistroUsuarioScreen(
    onBack: () -> Unit,
    onRegistroExitoso: (email: String) -> Unit,  // ✅ nuevo: navega a verificar email
    viewModel: RegistroViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()

    // ✅ Antes navegaba a onBack(). Ahora lleva el email a la pantalla de verificación
    LaunchedEffect(uiState.registroExitoso) {
        if (uiState.registroExitoso) {
            onRegistroExitoso(uiState.emailParaVerificar)
        }
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

@Composable
fun RegisterContentView(
    uiState: com.moon.casaprestamo.data.models.RegistroUiState,
    onEvent: (RegistroEvent) -> Unit,
    onBack: () -> Unit
) {
    val context    = LocalContext.current
    val calendar   = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val monthStr = if (month + 1 < 10) "0${month + 1}" else "${month + 1}"
            val dayStr   = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
            onEvent(RegistroEvent.FechaNacimientoChanged("$year-$monthStr-$dayStr"))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Text(
        text  = "REGISTRO DE CUENTA",
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight    = FontWeight.Black,
            letterSpacing = (-0.5).sp
        ),
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(Modifier.height(24.dp))

    Text(
        text     = "DATOS PERSONALES",
        style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    MonteInput(
        value         = uiState.nombre,
        onValueChange = { onEvent(RegistroEvent.NombreChanged(it)) },
        placeholder   = "NOMBRE(S) *",
        leadingIcon   = Icons.Default.Person
    )
    Spacer(Modifier.height(8.dp))

    MonteInput(
        value         = uiState.apellidoPaterno,
        onValueChange = { onEvent(RegistroEvent.ApellidoPaternoChanged(it)) },
        placeholder   = "APELLIDO PATERNO *",
        leadingIcon   = Icons.Default.Person
    )
    Spacer(Modifier.height(8.dp))

    MonteInput(
        value         = uiState.apellidoMaterno,
        onValueChange = { onEvent(RegistroEvent.ApellidoMaternoChanged(it)) },
        placeholder   = "APELLIDO MATERNO",
        leadingIcon   = Icons.Default.Person
    )
    Spacer(Modifier.height(8.dp))

    MonteInput(
        value         = uiState.curp,
        onValueChange = { if (it.length <= 18) onEvent(RegistroEvent.CurpChanged(it.uppercase())) },
        placeholder   = "CURP (18 CARACTERES) *",
        leadingIcon   = Icons.Default.Fingerprint
    )
    Spacer(Modifier.height(8.dp))

    MonteInput(
        value         = uiState.ine,
        onValueChange = { onEvent(RegistroEvent.IneChanged(it)) },
        placeholder   = "NÚMERO DE INE",
        leadingIcon   = Icons.Default.AssignmentInd
    )
    Spacer(Modifier.height(8.dp))

    MonteInput(
        value         = uiState.telefono,
        onValueChange = { onEvent(RegistroEvent.TelefonoChanged(it)) },
        placeholder   = "TELÉFONO CELULAR",
        leadingIcon   = Icons.Default.Phone,
        keyboardType  = KeyboardType.Phone
    )
    Spacer(Modifier.height(8.dp))

    MonteInput(
        value         = uiState.direccion,
        onValueChange = { onEvent(RegistroEvent.DireccionChanged(it)) },
        placeholder   = "DIRECCIÓN COMPLETA",
        leadingIcon   = Icons.Default.Home
    )
    Spacer(Modifier.height(8.dp))

    Box(modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }) {
        MonteInput(
            value         = uiState.fechaNacimiento,
            onValueChange = {},
            placeholder   = "FECHA DE NACIMIENTO (SELECCIONAR) *",
            leadingIcon   = Icons.Default.CalendarToday,
            enabled       = false,
            readOnly      = true
        )
    }

    HorizontalDivider(
        modifier  = Modifier.padding(vertical = 24.dp),
        thickness = 1.dp,
        color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    )

    Text(
        text     = "DATOS DE ACCESO",
        style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    MonteInput(
        value         = uiState.email,
        onValueChange = { onEvent(RegistroEvent.EmailChanged(it)) },
        placeholder   = "CORREO ELECTRÓNICO *",
        leadingIcon   = Icons.Default.Email,
        keyboardType  = KeyboardType.Email
    )
    Spacer(Modifier.height(8.dp))

    var passVisible by remember { mutableStateOf(false) }
    MonteInput(
        value            = uiState.password,
        onValueChange    = { onEvent(RegistroEvent.PasswordChanged(it)) },
        placeholder      = "CREAR CONTRASEÑA *",
        leadingIcon      = Icons.Default.Lock,
        isPassword       = true,
        passwordVisible  = passVisible,
        onPasswordToggle = { passVisible = !passVisible }
    )

    Spacer(Modifier.height(32.dp))

    Button(
        onClick  = { onEvent(RegistroEvent.RegistrarClicked) },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        enabled  = !uiState.isLoading
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
            Text(
                "¿YA TE REGISTRASTE? ",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                "INICIA SESIÓN",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.primary
            )
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegistroScreenPreview() {
    CasaPrestamoTheme(darkTheme = true) {
        RegistroUsuarioScreen(
            onBack            = {},
            onRegistroExitoso = {}
        )
    }
}