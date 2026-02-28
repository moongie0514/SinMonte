package com.moon.casaprestamo.ui.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moon.casaprestamo.ui.components.navigation.Sidebar
import com.moon.casaprestamo.ui.components.navigation.TopBar
import com.moon.casaprestamo.ui.components.navigation.UserRole
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseLayout(
    userRole: UserRole,
    userName: String,
    vistaActiva: String,
    onVistaChange: (String) -> Unit,
    onLogout: () -> Unit,
    searchPlaceholder: String?,
    titleProvider: (String) -> String,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // 🔹 Control real del colapsado del sidebar
    var sidebarOpen by rememberSaveable { mutableStateOf(true) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Sidebar(
                userRole = userRole,
                userName = userName,
                vistaActiva = vistaActiva,
                onVistaChange = {
                    onVistaChange(it)
                    scope.launch { drawerState.close() }
                },
                onLogout = onLogout,
                isOpen = sidebarOpen,
                onToggle = { sidebarOpen = !sidebarOpen }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    searchPlaceholder = searchPlaceholder,
                    onSearch = { /* listo para usar después */ },
                    notificationCount = 1,
                    onNotificationClick = {},
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
            // ... (mismo código de drawer y scaffold)
        ) { paddingValues ->

            // CAMBIO: De LazyColumn a Column
            val screenTitle = titleProvider(vistaActiva).trim()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (screenTitle.isNotEmpty()) {
                    Text(
                        text = screenTitle,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // El content() ahora puede tener su propio LazyColumn sin problemas
                // porque este Column padre no tiene scroll propio.
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                }
            }
        }
    }
}
