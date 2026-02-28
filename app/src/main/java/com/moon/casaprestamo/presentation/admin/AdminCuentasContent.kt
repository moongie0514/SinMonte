package com.moon.casaprestamo.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moon.casaprestamo.data.models.UsuarioResumen
import com.moon.casaprestamo.data.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CuentasUiState(
    val isLoading: Boolean = false,
    val usuarios: List<UsuarioResumen> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AdminCuentasViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(CuentasUiState())
    val uiState: StateFlow<CuentasUiState> = _uiState.asStateFlow()

    fun cargar(rol: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.obtenerUsuarios(rol = rol)
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(isLoading = false, usuarios = response.body()?.usuarios.orEmpty())
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Error HTTP ${response.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }
}

@Composable
fun AdminCuentasContent(
    viewModel: AdminCuentasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }

    val rol = if (tab == 0) null else "Cliente"

    LaunchedEffect(tab) {
        viewModel.cargar(rol)
    }

    val filtrados = uiState.usuarios.filter {
        query.isBlank() || listOf(it.nombre, it.apellidoPaterno, it.email, it.rol)
            .joinToString(" ")
            .contains(query, true)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("GESTIÓN DE CUENTAS", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Personal") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Clientes") })
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Buscar por nombre o email") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.error != null -> Text("Error: ${uiState.error}")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtrados) { u ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("${u.nombre} ${u.apellidoPaterno}", fontWeight = FontWeight.Bold)
                            Text(u.email)
                            Text("Rol: ${u.rol} | Activo: ${if (u.activo) "Sí" else "No"}")
                        }
                    }
                }
            }
        }
    }
}
