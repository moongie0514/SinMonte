package com.moon.casaprestamo.ui.components.common

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.moon.casaprestamo.ui.theme.CasaPrestamoTheme

data class Notificacion(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val tipo: TipoNotificacion,
    val leida: Boolean = false,
    val fecha: String,
    val metadata: NotificacionMetadata? = null
)

data class NotificacionMetadata(
    val itemId: String? = null,
    val action: String? = null
)

enum class TipoNotificacion {
    PAGO_VENCIDO,
    NUEVO_PRESTAMO,
    SOLICITUD_PENDIENTE,
    PAGO_EXITOSO,
    ALERTA_SISTEMA,
    INFORMACION
}

/**
 * NotificationPopover - Componente de notificaciones estilo "Modern Bubble"
 * Usado en: AdminDashboard, EmpleadoDashboard, ClienteDashboard
 */
@Composable
fun NotificationPopover(
    isOpen: Boolean,
    notificaciones: List<Notificacion>,
    onDismiss: () -> Unit,
    onNotificationClick: (Notificacion) -> Unit,
    onMarkAllRead: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (isOpen) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = modifier
                    .width(360.dp)
                    .heightIn(max = 500.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 16.dp
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onBackground,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NOTIFICACIONES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp,
                                    letterSpacing = 2.sp
                                ),
                                color = Color.White
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (notificaciones.any { !it.leida }) {
                                    TextButton(
                                        onClick = onMarkAllRead,
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "MARCAR TODO",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 8.sp
                                            ),
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Cerrar",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Lista de notificaciones
                    if (notificaciones.isEmpty()) {
                        // Estado vacío
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.NotificationsNone,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "No hay notificaciones",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                        ) {
                            items(notificaciones) { notificacion ->
                                NotificationItem(
                                    notificacion = notificacion,
                                    onClick = { onNotificationClick(notificacion) }
                                )
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notificacion: Notificacion,
    onClick: () -> Unit
) {
    val backgroundColor = if (notificacion.leida) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Indicador de tipo
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = getNotificationColor(notificacion.tipo).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getNotificationIcon(notificacion.tipo),
                    contentDescription = null,
                    tint = getNotificationColor(notificacion.tipo),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Contenido
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notificacion.titulo.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notificacion.leida) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }
                Text(
                    text = notificacion.descripcion,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = notificacion.fecha,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun getNotificationIcon(tipo: TipoNotificacion) = when (tipo) {
    TipoNotificacion.PAGO_VENCIDO -> Icons.Default.Warning
    TipoNotificacion.NUEVO_PRESTAMO -> Icons.Default.AccountBalance
    TipoNotificacion.SOLICITUD_PENDIENTE -> Icons.Default.HourglassEmpty
    TipoNotificacion.PAGO_EXITOSO -> Icons.Default.CheckCircle
    TipoNotificacion.ALERTA_SISTEMA -> Icons.Default.Notifications
    TipoNotificacion.INFORMACION -> Icons.Default.Info
}

private fun getNotificationColor(tipo: TipoNotificacion) = when (tipo) {
    TipoNotificacion.PAGO_VENCIDO -> Color(0xFFDC2626)
    TipoNotificacion.NUEVO_PRESTAMO -> Color(0xFF2563EB)
    TipoNotificacion.SOLICITUD_PENDIENTE -> Color(0xFFF59E0B)
    TipoNotificacion.PAGO_EXITOSO -> Color(0xFF059669)
    TipoNotificacion.ALERTA_SISTEMA -> Color(0xFFA6032F)
    TipoNotificacion.INFORMACION -> Color(0xFF6366F1)
}
