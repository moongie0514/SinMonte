package com.moon.casaprestamo.presentation.cliente

import androidx.compose.runtime.Composable
import com.moon.casaprestamo.ui.components.common.BaseLayout
import com.moon.casaprestamo.ui.components.navigation.UserRole

@Composable
fun ClienteLayout(
    userName: String,
    vistaActiva: String,
    onVistaChange: (String) -> Unit,
    onLogout: () -> Unit,
    titleProvider: (String) -> String,
    content: @Composable () -> Unit
) {
    BaseLayout(
        userRole = UserRole.CLIENTE,
        userName = userName,
        vistaActiva = vistaActiva,
        onVistaChange = onVistaChange,
        onLogout = onLogout,
        // Cliente no utiliza buscador según diseño UX
        searchPlaceholder = null,
        titleProvider = titleProvider,
        content = content
    )
}

