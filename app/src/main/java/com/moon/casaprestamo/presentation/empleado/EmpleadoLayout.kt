package com.moon.casaprestamo.presentation.empleado

import androidx.compose.runtime.Composable
import com.moon.casaprestamo.ui.components.common.BaseLayout
import com.moon.casaprestamo.ui.components.navigation.UserRole

@Composable
fun EmpleadoLayout(
    userName: String,
    vistaActiva: String,
    onVistaChange: (String) -> Unit,
    onLogout: () -> Unit,
    titleProvider: (String) -> String,
    content: @Composable () -> Unit
) {
    BaseLayout(
        userRole = UserRole.EMPLEADO,
        userName = userName,
        vistaActiva = vistaActiva,
        onVistaChange = onVistaChange,
        onLogout = onLogout,
        searchPlaceholder = "Buscar cliente o folio...",
        titleProvider = titleProvider,
        content = content
    )
}
