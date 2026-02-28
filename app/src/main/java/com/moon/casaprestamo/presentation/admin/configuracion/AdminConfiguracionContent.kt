package com.moon.casaprestamo.presentation.admin.configuracion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moon.casaprestamo.data.models.ConfigAdminUiState

@Composable
fun AdminConfiguracionContent(
    state: ConfigAdminUiState,
    onTasaChange: (String) -> Unit,
    onPlazoChange: (Int) -> Unit,
    onMinimoChange: (String) -> Unit,
    onMaximoChange: (String) -> Unit,
    onGuardar: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ConfigField(
                    label = "Tasa de Interés Mensual (%)",
                    value = state.tasaInteres,
                    onValueChange = onTasaChange,
                    icon = Icons.Default.Percent,
                    iconColor = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ConfigField(
                        label = "Monto Mínimo",
                        value = state.montoMinimo,
                        onValueChange = onMinimoChange,
                        icon = Icons.Default.RemoveCircleOutline,
                        iconColor = Color(0xFF059669),
                        modifier = Modifier.weight(1f)
                    )
                    ConfigField(
                        label = "Monto Máximo",
                        value = state.montoMaximo,
                        onValueChange = onMaximoChange,
                        icon = Icons.Default.AddCircleOutline,
                        iconColor = Color(0xFF059669),
                        modifier = Modifier.weight(1f)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LabelText("Límite de Plazo (Meses)")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(12, 24, 36, 48, 60).forEach { m ->
                            val selected = state.plazoMaximo == m.toString()
                            Button(
                                onClick = { onPlazoChange(m) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected)
                                        MaterialTheme.colorScheme.onBackground
                                    else
                                        MaterialTheme.colorScheme.background,
                                    contentColor = if (selected)
                                        MaterialTheme.colorScheme.surface
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                ),
                                border = if (!selected)
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                else null
                            ) {
                                Text(
                                    "$m M",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onGuardar,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground
                    ),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "APLICAR CONFIGURACIÓN",
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Check,
                            null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                state.mensaje?.let { mensaje ->
                    Text(
                        text = mensaje,
                        color = if (state.esError)
                            MaterialTheme.colorScheme.error
                        else
                            Color(0xFF10B981),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfigField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LabelText(label)

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            singleLine = true
        )
    }
}

@Composable
private fun LabelText(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.outline
    )
}