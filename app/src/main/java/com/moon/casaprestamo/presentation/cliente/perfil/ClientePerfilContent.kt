package com.moon.casaprestamo.presentation.cliente.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClientePerfilContent(
    uiState:                 ClientePerfilUiState,
    onNombreChange:          (String) -> Unit,
    onApellidoPaternoChange: (String) -> Unit,
    onApellidoMaternoChange: (String) -> Unit,
    onTelefonoChange:        (String) -> Unit,
    onDireccionChange:       (String) -> Unit,
    onGuardar:               () -> Unit,
    onLimpiarMensaje:        () -> Unit,
    modifier:                Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    val snackbarState = remember { SnackbarHostState() }

    // Mostrar mensaje de éxito o error en Snackbar
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarState.showSnackbar(it)
            onLimpiarMensaje()
            // Si guardó correctamente, salir del modo edición
            if (!uiState.esError) isEditing = false
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarState) }) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {

            // ── SECCIÓN CONTACTO Y UBICACIÓN ─────────────────────
            Text(
                text     = "Contacto y Ubicación",
                style    = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                color    = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            PerfilItem(
                label         = "Nombre(s)",
                value         = uiState.nombre,
                isEditing     = isEditing,
                onValueChange = onNombreChange
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            PerfilItem(
                label         = "Apellido Paterno",
                value         = uiState.apellidoPaterno,
                isEditing     = isEditing,
                onValueChange = onApellidoPaternoChange
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            PerfilItem(
                label         = "Apellido Materno",
                value         = uiState.apellidoMaterno,
                isEditing     = isEditing,
                onValueChange = onApellidoMaternoChange
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            PerfilItem(
                label         = "Correo Electrónico",
                value         = uiState.email,
                isProtected   = true,
                isEditing     = isEditing
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            PerfilItem(
                label         = "Teléfono Celular",
                value         = uiState.telefono,
                isEditing     = isEditing,
                onValueChange = onTelefonoChange
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            PerfilItem(
                label         = "Dirección Completa",
                value         = uiState.direccion,
                isEditing     = isEditing,
                onValueChange = onDireccionChange
            )

            Spacer(Modifier.height(32.dp))

            // ── SECCIÓN INFORMACIÓN PROTEGIDA ────────────────────
            Text(
                text     = "Información Personal",
                style    = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                color    = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            PerfilItem("CURP",                uiState.curp,            isProtected = true, isEditing = isEditing)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            PerfilItem("Número de INE",       uiState.numeroIne,       isProtected = true, isEditing = isEditing)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            PerfilItem("Fecha de Nacimiento", uiState.fechaNacimiento, isProtected = true, isEditing = isEditing)

            Spacer(Modifier.height(40.dp))

            // ── ADVERTENCIA Y BOTONES ────────────────────────────
            if (isEditing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.inverseSurface,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.inversePrimary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text  = "LOS CAMPOS DE CURP, IDENTIFICACIÓN Y NACIMIENTO ESTÁN PROTEGIDOS. PARA CAMBIOS CRÍTICOS ACUDE A SUCURSAL.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight    = FontWeight.Black,
                            fontSize      = 9.sp,
                            lineHeight    = 14.sp,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick  = { isEditing = false },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape    = RoundedCornerShape(12.dp),
                        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("CANCELAR", fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick  = onGuardar,
                        modifier = Modifier.weight(2f).height(56.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled  = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("GUARDAR", fontWeight = FontWeight.Black)
                        }
                    }
                }
            } else {
                Button(
                    onClick  = { isEditing = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("EDITAR PERFIL", fontWeight = FontWeight.Black)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun PerfilItem(
    label:         String,
    value:         String,
    isProtected:   Boolean = false,
    isEditing:     Boolean = false,
    onValueChange: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text  = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight    = FontWeight.Black,
                letterSpacing = 1.5.sp
            ),
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(Modifier.height(4.dp))

        if (isEditing && !isProtected) {
            OutlinedTextField(
                value         = value,
                onValueChange = onValueChange,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                textStyle     = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text  = value.ifBlank { "—" },
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (isProtected && isEditing)
                        MaterialTheme.colorScheme.outline
                    else
                        MaterialTheme.colorScheme.onBackground
                )
                if (isProtected) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector        = Icons.Default.Lock,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier           = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}