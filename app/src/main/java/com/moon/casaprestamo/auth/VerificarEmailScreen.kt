package com.moon.casaprestamo.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val Oscuro = Color(0xFF0F172A)

@Composable
fun VerificarEmailScreen(
    email: String,
    onVerificado: () -> Unit,               // navega al login
    onBackToLogin: () -> Unit,
    viewModel: VerificarEmailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Inyectar email desde la navegación
    LaunchedEffect(email) {
        viewModel.setEmail(email)
    }

    // Navegar al login cuando se verifica
    LaunchedEffect(uiState.verificado) {
        if (uiState.verificado) onVerificado()
    }

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
            colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint     = MaterialTheme.colorScheme.primary
                )

                Text(
                    "VERIFICA TU EMAIL",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                )

                Text(
                    "Enviamos un código de 6 dígitos a\n${uiState.email}",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // Campo código
                OutlinedTextField(
                    value         = uiState.codigo,
                    onValueChange = viewModel::onCodigoChange,
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
                    isError         = uiState.errorMessage != null,
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        errorBorderColor     = MaterialTheme.colorScheme.error
                    )
                )

                // Error
                AnimatedVisibility(visible = uiState.errorMessage != null) {
                    Text(
                        text      = uiState.errorMessage ?: "",
                        style     = MaterialTheme.typography.bodySmall,
                        color     = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth()
                    )
                }

                // Botón verificar
                Button(
                    onClick  = viewModel::verificar,
                    enabled  = uiState.codigo.length == 6 && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Oscuro)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "VERIFICAR",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }

                // Reenviar
                TextButton(
                    onClick  = viewModel::reenviarCodigo,
                    enabled  = !uiState.isLoading
                ) {
                    Text(
                        "¿No recibiste el código? Reenviar",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                TextButton(onClick = onBackToLogin) {
                    Text(
                        "VOLVER AL LOGIN",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                    )
                }
            }
        }
    }
}