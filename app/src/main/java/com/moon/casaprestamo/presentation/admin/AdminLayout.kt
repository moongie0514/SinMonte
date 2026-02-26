package com.moon.casaprestamo.presentation.admin

import androidx.compose.runtime.Composable
import com.moon.casaprestamo.ui.components.common.BaseLayout
import com.moon.casaprestamo.ui.components.navigation.UserRole

@Composable
fun AdminLayout(
    userName: String,
    vistaActiva: String,
    onVistaChange: (String) -> Unit,
    onLogout: () -> Unit,
    titleProvider: (String) -> String,
    content: @Composable () -> Unit
) {
    BaseLayout(
        userRole = UserRole.ADMIN,
        userName = userName,
        vistaActiva = vistaActiva,
        onVistaChange = onVistaChange,
        onLogout = onLogout,
        searchPlaceholder = "Escriba correo, CURP o Folio...",
        titleProvider = titleProvider,
        content = content
    )
}
