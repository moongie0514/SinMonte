package com.moon.casaprestamo.auth

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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moon.casaprestamo.data.models.*
import com.moon.casaprestamo.ui.components.inputs.MonteInput

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess:           (Usuario) -> Unit,
    onNavigateToRegister:     () -> Unit,
    onNavigateToForgot:       () -> Unit,
    // ── NUEVO: navega a VerificarEmailScreen con el email precargado ──────────
    onRequiereVerificacion:   (email: String) -> Unit
) {
    val uiState           by viewModel.uiState.collectAsState()
    val snackbarHostState  = remember { SnackbarHostState() }

    LaunchedEffect(uiState.loginResult) {
        when (val result = uiState.loginResult) {
            is LoginResult.Success -> {
                onLoginSuccess(result.usuario)
            }
            is LoginResult.RequiereVerificacion -> {
                // En lugar de mostrar el error, navegamos a la pantalla de verificación
                // El email ya viene precargado para que el usuario solo ingrese el código
                viewModel.onEvent(LoginEvent.DismissError)
                onRequiereVerificacion(result.email)
            }
            is LoginResult.Error -> {
                snackbarHostState.showSnackbar(result.message)
                viewModel.onEvent(LoginEvent.DismissError)
            }
            null -> {}
        }
    }

    val scrollState = rememberScrollState()
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.AccountBalance,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint     = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "NACIONAL MONTE SIN PIEDAD",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontStyle  = FontStyle.Italic
                    )
                )
                Spacer(Modifier.height(32.dp))
                LoginContentView(
                    uiState              = uiState,
                    onEvent              = viewModel::onEvent,
                    onNavigateToForgot   = onNavigateToForgot,
                    onNavigateToRegister = onNavigateToRegister
                )
            }
        }
    }
}

@Composable
fun LoginContentView(
    uiState:              LoginUiState,
    onEvent:              (LoginEvent) -> Unit,
    onNavigateToForgot:   () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = "INICIAR SESIÓN",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight    = FontWeight.Black,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(32.dp))

        MonteInput(
            value         = uiState.username,
            onValueChange = { onEvent(LoginEvent.UsernameChanged(it)) },
            placeholder   = "Correo Electrónico",
            leadingIcon   = Icons.Default.Mail
        )
        Spacer(Modifier.height(16.dp))

        var passVisible by remember { mutableStateOf(false) }
        MonteInput(
            value            = uiState.password,
            onValueChange    = { onEvent(LoginEvent.PasswordChanged(it)) },
            placeholder      = "Contraseña",
            leadingIcon      = Icons.Default.Lock,
            isPassword       = true,
            passwordVisible  = passVisible,
            onPasswordToggle = { passVisible = !passVisible }
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = onNavigateToForgot) {
                Text(
                    "¿OLVIDASTE TU CONTRASEÑA?",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize   = 9.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier    = Modifier.padding(vertical = 16.dp).size(32.dp),
                strokeWidth = 3.dp
            )
        }

        Button(
            onClick  = { onEvent(LoginEvent.LoginClicked) },
            enabled  = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = Color.White
            )
        ) {
            Text(
                if (uiState.isLoading) "CARGANDO..." else "INGRESAR",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            )
        }

        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "¿NO TIENES CUENTA? ",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                "REGÍSTRATE",
                modifier = Modifier.clickable { onNavigateToRegister() },
                style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                color    = MaterialTheme.colorScheme.primary
            )
        }
    }
}
