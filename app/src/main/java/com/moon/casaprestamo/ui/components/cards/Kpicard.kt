package com.moon.casaprestamo.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moon.casaprestamo.ui.theme.CasaPrestamoTheme

/**
 * KPICard - Componente reutilizable para mostrar métricas clave
 * Usado en: AdminDashboard, ClienteDashboard, Reportes
 */
@Composable
fun KPICard(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    iconBackgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icono
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = iconBackgroundColor,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Label
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    letterSpacing = 1.5.sp
                ),
                color = MaterialTheme.colorScheme.outline
            )

            // Value
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun KPICardPreview() {
    CasaPrestamoTheme (darkTheme = false)  {
        // Contenedor con padding para apreciar la elevación y bordes
        Surface(
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Ejemplo 1: Capital Total
                KPICard(
                    label = "Capital en Préstamos",
                    value = "$1,250,400",
                    icon = Icons.Default.AccountBalanceWallet,
                    iconColor = Color(0xFF10B981), // Verde
                    iconBackgroundColor = Color(0xFF10B981).copy(alpha = 0.1f)
                )

                // Ejemplo 2: Clientes Activos
                KPICard(
                    label = "Clientes Registrados",
                    value = "1,248",
                    icon = Icons.Default.Group,
                    iconColor = MaterialTheme.colorScheme.primary, // Guinda
                    iconBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )

                // Fila de KPIs (Simulando Dashboard)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KPICard(
                        label = "Intereses",
                        value = "+12%",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconColor = Color(0xFF3B82F6), // Azul
                        iconBackgroundColor = Color(0xFF3B82F6).copy(alpha = 0.1f),
                        modifier = Modifier.weight(1f)
                    )

                    // KPI vacío o de prueba
                    KPICard(
                        label = "Tickets",
                        value = "85",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = Color(0xFFF59E0B), // Ámbar
                        iconBackgroundColor = Color(0xFFF59E0B).copy(alpha = 0.1f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}