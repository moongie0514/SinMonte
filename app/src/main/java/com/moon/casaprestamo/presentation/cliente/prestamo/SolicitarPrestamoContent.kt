package com.moon.casaprestamo.presentation.cliente.prestamo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moon.casaprestamo.ui.theme.CasaPrestamoTheme

// ✅ Importa el UiState desde el ViewModel (presentation), no desde data.models
// El UiState viejo en data.models puede eliminarse o dejarse si otros lugares lo usan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarPrestamoContent(
    state: SolicitarPrestamoUiState,
    onMontoChange: (String) -> Unit,
    onPlazoChange: (Int) -> Unit,       // ✅ antes: onMesesChange: (String)
    onEnviar: () -> Unit,
    onBack: () -> Unit
) {
    val colorScheme  = MaterialTheme.colorScheme
    val tasaInteres  = state.tasaConfigurada
    val montoNum     = state.monto.toDoubleOrNull() ?: 0.0

    // ✅ Cuota viene calculada por el ViewModel (cuotaEstimada), no calculada en la UI
    val pagoMensual  = state.cuotaEstimada ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // --- SECCIÓN CAPITAL ---
        Text(
            text         = "CAPITAL REQUERIDO",
            style        = MaterialTheme.typography.labelSmall,
            fontWeight   = FontWeight.Black,
            color        = colorScheme.outline,
            letterSpacing = 1.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text       = "$",
                fontSize   = 32.sp,
                fontWeight = FontWeight.Black,
                color      = colorScheme.primary
            )
            TextField(
                value         = state.monto,
                onValueChange = onMontoChange,
                modifier      = Modifier.fillMaxWidth(),
                textStyle     = LocalTextStyle.current.copy(
                    fontSize   = 40.sp,
                    fontWeight = FontWeight.Black,
                    color      = colorScheme.onBackground
                ),
                colors        = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor  = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                placeholder   = {
                    Text("0", fontSize = 40.sp, fontWeight = FontWeight.Black, color = colorScheme.outline.copy(alpha = 0.3f))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Text(
            text       = "LÍMITE: $${state.montoMinimoPermitido.toLong()} – $${state.montoMaximoPermitido.toLong()}",
            fontSize   = 11.sp,
            fontWeight = FontWeight.Bold,
            color      = colorScheme.primary.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- SECCIÓN PLAZOS ---
        Text(
            text          = "PLAZO DE DEVOLUCIÓN",
            style         = MaterialTheme.typography.labelSmall,
            fontWeight    = FontWeight.Black,
            color         = colorScheme.outline,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ plazosDisponibles son Int fijos (6,12,24,36,48), no String libres
        // ✅ isSelected compara plazoMeses (Int) directamente
        FlowRow(
            modifier                = Modifier.fillMaxWidth(),
            horizontalArrangement   = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow       = 3
        ) {
            listOf(6, 12, 24, 36, 48).forEach { plazo ->
                val isSelected = state.plazoMeses == plazo        // ✅ antes: state.meses == plazo.toString()
                Surface(
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .weight(1f, fill = false)
                        .height(52.dp)
                        .widthIn(min = 100.dp)
                        .clickable { onPlazoChange(plazo) },      // ✅ antes: onMesesChange(plazo.toString())
                    shape  = RoundedCornerShape(12.dp),
                    color  = if (isSelected) colorScheme.onBackground else colorScheme.surfaceVariant,
                    border = if (isSelected) null else BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text       = "$plazo MESES",
                            color      = if (isSelected) colorScheme.background else colorScheme.onSurface,
                            fontWeight = FontWeight.Black,
                            fontSize   = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- BANNER PAGO ESTIMADO ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color    = colorScheme.onBackground,
            shape    = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier              = Modifier.padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "PAGO MENSUAL",
                        color      = colorScheme.secondary,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        // ✅ Muestra cuota del ViewModel; "—" si aún no hay monto
                        if (pagoMensual > 0) "$${String.format("%,.0f", pagoMensual)}" else "—",
                        color      = colorScheme.background,
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Surface(
                    color = colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text       = "${tasaInteres}%",
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color      = colorScheme.onPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize   = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // --- BOTONES ---
        Row(
            modifier              = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick  = onBack,
                modifier = Modifier.weight(1f).height(60.dp),
                shape    = RoundedCornerShape(16.dp),
                border   = BorderStroke(2.dp, colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Text("CANCELAR", color = colorScheme.onBackground, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }

            Button(
                onClick  = onEnviar,
                modifier = Modifier.weight(1f).height(60.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                enabled  = !state.isLoading && state.monto.isNotEmpty()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = colorScheme.onPrimary, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                } else {
                    Text("SOLICITAR", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }
        }

        // Mensaje resultado / error
        state.resultado?.let {
            Text(
                text      = it,
                modifier  = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.Center,
                style     = MaterialTheme.typography.bodyMedium,
                color     = colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        state.error?.let {
            Text(
                text      = it,
                modifier  = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.Center,
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(name = "Solicitar Préstamo", showBackground = true)
@Composable
fun SolicitarPrestamoPreview() {
    CasaPrestamoTheme(darkTheme = false) {
        SolicitarPrestamoContent(
            state = SolicitarPrestamoUiState(monto = "5000", plazoMeses = 12),
            onMontoChange = {},
            onPlazoChange = {},
            onEnviar      = {},
            onBack        = {}
        )
    }
}