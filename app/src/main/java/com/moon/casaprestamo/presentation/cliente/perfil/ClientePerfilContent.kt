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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moon.casaprestamo.ui.theme.CasaPrestamoTheme

@Composable
fun ClientePerfilContent(
    uiState: ClientePerfilUiState,
    onEditarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Fondo dinámico
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // --- SECCIÓN DE CONTACTO ---
        Text(
            text = "Contacto y Ubicación",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.primary, // Color rojo del tema
            modifier = Modifier.padding(bottom = 16.dp)
        )

        PerfilItem("Nombre Completo", uiState.nombreCompleto, isEditing = isEditing)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        PerfilItem("Correo Electrónico", uiState.email, isEditing = isEditing)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        PerfilItem("Teléfono", uiState.telefono, isEditing = isEditing)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        PerfilItem("Dirección Completa", uiState.direccion, isEditing = isEditing)

        Spacer(modifier = Modifier.height(32.dp))

        // --- SECCIÓN DE INFORMACIÓN PROTEGIDA ---
        Text(
            text = "Información Personal",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        PerfilItem("CURP", uiState.curp, isProtected = true, isEditing = isEditing)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        PerfilItem("Número de INE", uiState.numeroIne, isProtected = true, isEditing = isEditing)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        PerfilItem("Fecha de Nacimiento", uiState.fechaNacimiento, isProtected = true, isEditing = isEditing)

        Spacer(modifier = Modifier.height(40.dp))

        if (isEditing) {
            // ADVERTENCIA DINÁMICA: Usa InverseSurface para que sea oscura en Light y clara en Dark
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.inverseSurface, // Cambia según el tema
                        RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.inversePrimary // Contraste dinámico
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "LOS CAMPOS DE CURP, IDENTIFICACIÓN Y NACIMIENTO ESTÁN PROTEGIDOS. PARA CAMBIOS CRÍTICOS ACUDE A SUCURSAL.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp,
                        lineHeight = 14.sp,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { isEditing = false },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline) // Borde dinámico
                ) {
                    Text("CANCELAR", fontWeight = FontWeight.Black)
                }
                Button(
                    onClick = { isEditing = false },
                    modifier = Modifier.weight(2f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("GUARDAR", fontWeight = FontWeight.Black)
                }
            }
        } else {
            Button(
                onClick = { isEditing = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("EDITAR", fontWeight = FontWeight.Black)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PerfilItem(
    label: String,
    value: String,
    isProtected: Boolean = false,
    isEditing: Boolean = false,
    onValueChange: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            ),
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (isEditing && !isProtected) {
            // Campo editable cuando se activa la edición
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )
        } else {
            // Vista normal o campo protegido
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isProtected && isEditing)
                        MaterialTheme.colorScheme.outline
                    else MaterialTheme.colorScheme.onBackground
                )
                if (isProtected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewClientePerfilContent() {
    CasaPrestamoTheme (darkTheme = true){
        ClientePerfilContent(
            uiState = ClientePerfilUiState(
                idCliente = 1,
                nombreCompleto = "Juan Pérez García",
                curp = "PEGJ900101HDFRNN09",
                numeroIne = "1234567890123",
                fechaNacimiento = "01 de Enero, 1990",
                telefono = "81 1234 5678",
                email = "juan.perez@mail.com",
                direccion = "Av. Constitución 123, Monterrey",
                fechaRegistro = "15 de Enero, 2026"
            ),
            modifier = Modifier.padding(24.dp),
            onEditarClick = {}
        )
    }
}