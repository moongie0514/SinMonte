package com.moon.casaprestamo.ui.components.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class UserRole {
    ADMIN, EMPLEADO, CLIENTE
}

data class NavItemData(
    val id: String,
    val label: String,
    val icon: ImageVector
)


/**
 * Sidebar - Componente reutilizable de navegación lateral
 * Usado en: AdminDashboard, EmpleadoDashboard, ClienteDashboard
 */
@Composable
fun Sidebar(
    userRole: UserRole,
    userName: String,
    vistaActiva: String,
    onVistaChange: (String) -> Unit,
    onLogout: () -> Unit,
    isOpen: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sidebarWidth by animateDpAsState(
        targetValue = if (isOpen) 288.dp else 96.dp,
        label = "sidebar_width"
    )

    // Definir items según el rol
    val navItems = when (userRole) {
        UserRole.ADMIN -> listOf(
            NavItemData(Routes.ADMIN_REPORTES, "Reportes", Icons.Default.BarChart),
            NavItemData(Routes.ADMIN_SUPERVISION, "Supervisión", Icons.Default.Assessment),
            NavItemData(Routes.ADMIN_CUENTAS, "Cuentas", Icons.Default.People),
            NavItemData(Routes.ADMIN_CONFIGURACION, "Ajustes", Icons.Default.Settings)
        )
        UserRole.EMPLEADO -> listOf(
            NavItemData(Routes.EMPLEADO_COBRANZA, "Cobranza", Icons.Default.Receipt),
            NavItemData(Routes.EMPLEADO_CLIENTES, "Clientes", Icons.Default.People),
            NavItemData(Routes.EMPLEADO_PRESTAMOS, "Préstamos", Icons.Default.AccountBalance)
        )
        UserRole.CLIENTE -> listOf(
            NavItemData(Routes.CLIENTE_CARTERA, "Mi Cartera", Icons.Default.Dashboard),
            NavItemData(Routes.CLIENTE_SOLICITAR, "Nuevo Crédito", Icons.Default.AccountBalance),
            NavItemData(Routes.CLIENTE_PERFIL, "Perfil", Icons.Default.Person)
        )
    }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(sidebarWidth),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Toggle Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, end = 12.dp),
                contentAlignment = if (isOpen) Alignment.CenterEnd else Alignment.Center
            ) {
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isOpen) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                        contentDescription = if (isOpen) "Cerrar" else "Abrir",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Header con logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isOpen) Arrangement.Start else Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (isOpen) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "MONTE SIN PIEDAD",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = userName.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Navigation Items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                navItems.forEach { item ->
                    NavItem(
                        item = item,
                        isActive = vistaActiva == item.id,
                        isOpen = isOpen,
                        onClick = { onVistaChange(item.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer - Logout
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLogout() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (isOpen) Arrangement.Start else Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Cerrar sesión",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                    if (isOpen) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "CERRAR SESIÓN",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    item: NavItemData,
    isActive: Boolean,
    isOpen: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = if (isActive) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isOpen) Arrangement.Start else Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (isActive) Color.White else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
            if (isOpen) {
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item.label.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = if (isActive) Color.White else MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
