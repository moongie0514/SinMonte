package com.moon.casaprestamo.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moon.casaprestamo.ui.components.inputs.MonteInput
import com.moon.casaprestamo.ui.theme.CasaPrestamoTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(32.dp),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = uiState.step,
                    transitionSpec = {
                        slideInHorizontally { it } + fadeIn() with
                                slideOutHorizontally { -it } + fadeOut()
                    },
                    label = "PasswordRecoverySteps"
                ) { target ->
                    when (target) {
                        1 -> EmailStep(
                            value         = uiState.email,
                            onValueChange = viewModel::onEmailChange,
                            isLoading     = uiState.isLoading,
                            error         = uiState.error,
                            onNext        = viewModel::solicitarCodigo
                        )
                        2 -> OtpStep(
                            value         = uiState.codigo,
                            onValueChange = viewModel::onCodigoChange,
                            email         = uiState.email,
                            error         = uiState.error,
                            isLoading     = uiState.isLoading,
                            onNext        = viewModel::verificarCodigo,
                            onReenviar    = viewModel::reenviarCodigo
                        )
                        3 -> NewPasswordStep(
                            pass          = uiState.nuevaPassword,
                            conf          = uiState.confirmarPassword,
                            visible       = uiState.passwordVisible,
                            isLoading     = uiState.isLoading,
                            error         = uiState.error,
                            onPassChange  = viewModel::onNuevaPasswordChange,
                            onConfChange  = viewModel::onConfirmarPasswordChange,
                            onToggle      = viewModel::togglePasswordVisible,
                            onSuccess     = viewModel::cambiarPassword
                        )
                        4 -> SuccessStep(onFinished = onBackToLogin)
                    }
                }

                if (uiState.step < 4) {
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = onBackToLogin) {
                        Text(
                            "CANCELAR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                }
            }
        }
    }
}

// ── Paso 1: ingresar email ────────────────────────────────────────────────────
@Composable
fun EmailStep(
    value: String,
    onValueChange: (String) -> Unit,
    isLoading: Boolean,
    error: String?,
    onNext: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StepHeader(
            icon  = Icons.Default.Email,
            title = "RECUPERAR ACCESO",
            sub   = "Ingresa el correo asociado a tu cuenta."
        )

        MonteInput(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = "CORREO ELECTRÓNICO",
            leadingIcon   = Icons.Default.Email
        )

        ErrorText(error)

        Spacer(Modifier.height(24.dp))

        Button(
            onClick  = onNext,
            enabled  = value.contains("@") && value.contains(".") && !isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(16.dp)
        ) {
            LoadingOrText(isLoading, "ENVIAR CÓDIGO")
        }
    }
}

// ── Paso 2: ingresar código OTP ───────────────────────────────────────────────
@Composable
fun OtpStep(
    value: String,
    onValueChange: (String) -> Unit,
    email: String,
    error: String?,
    isLoading: Boolean,
    onNext: () -> Unit,
    onReenviar: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StepHeader(
            icon  = Icons.Default.Key,
            title = "VERIFICACIÓN",
            sub   = "Código enviado a\n$email"
        )

        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(16.dp),
            placeholder   = {
                Text(
                    "0 0 0 0 0 0",
                    modifier  = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                fontWeight    = FontWeight.Black,
                textAlign     = TextAlign.Center,
                letterSpacing = 8.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine      = true,
            isError         = error != null,
            colors          = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                errorBorderColor     = MaterialTheme.colorScheme.error
            )
        )

        ErrorText(error)

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = onReenviar, enabled = !isLoading) {
            Text(
                "¿No recibiste el código? Reenviar",
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick  = onNext,
            enabled  = value.length == 6 && !isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = Color.White
            )
        ) {
            LoadingOrText(isLoading, "VALIDAR CÓDIGO")
        }
    }
}

// ── Paso 3: nueva contraseña ──────────────────────────────────────────────────
@Composable
fun NewPasswordStep(
    pass: String,
    conf: String,
    visible: Boolean,
    isLoading: Boolean,
    error: String?,
    onPassChange: (String) -> Unit,
    onConfChange: (String) -> Unit,
    onToggle: () -> Unit,
    onSuccess: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StepHeader(
            icon  = Icons.Default.LockOpen,
            title = "NUEVA CLAVE",
            sub   = "Crea una contraseña segura."
        )

        MonteInput(
            value            = pass,
            onValueChange    = onPassChange,
            placeholder      = "NUEVA CONTRASEÑA",
            leadingIcon      = Icons.Default.Lock,
            isPassword       = true,
            passwordVisible  = visible,
            onPasswordToggle = onToggle
        )
        Spacer(Modifier.height(8.dp))
        MonteInput(
            value            = conf,
            onValueChange    = onConfChange,
            placeholder      = "CONFIRMAR CONTRASEÑA",
            leadingIcon      = Icons.Default.Shield,
            isPassword       = true,
            passwordVisible  = visible,
            onPasswordToggle = onToggle
        )

        ErrorText(error)

        Spacer(Modifier.height(24.dp))

        Button(
            onClick  = onSuccess,
            enabled  = pass.length >= 6 && pass == conf && !isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(16.dp)
        ) {
            LoadingOrText(isLoading, "ACTUALIZAR")
        }
    }
}

// ── Paso 4: éxito — auto-navega al login ─────────────────────────────────────
@Composable
fun SuccessStep(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500)
        onFinished()
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 40.dp)
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint     = Color(0xFF10B981)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "¡ÉXITO!",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
        )
        Text(
            "Tu contraseña ha sido actualizada.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

// ── Componentes reutilizables ─────────────────────────────────────────────────

@Composable
fun StepHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    sub: String
) {
    Icon(icon, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(16.dp))
    Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
    Text(
        sub,
        style     = MaterialTheme.typography.bodySmall,
        color     = MaterialTheme.colorScheme.outline,
        textAlign = TextAlign.Center,
        modifier  = Modifier.padding(bottom = 32.dp)
    )
}

@Composable
private fun ErrorText(error: String?) {
    if (error != null) {
        Spacer(Modifier.height(8.dp))
        Text(
            text      = error,
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LoadingOrText(isLoading: Boolean, label: String) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier    = Modifier.size(20.dp),
            color       = Color.White,
            strokeWidth = 2.dp
        )
    } else {
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black))
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreview() {
    CasaPrestamoTheme(darkTheme = true) {
        ForgotPasswordScreen(onBackToLogin = {})
    }
}