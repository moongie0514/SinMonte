package com.moon.casaprestamo.presentation.empleado

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

private data class EmpleadoClientesUiState(
    val isLoading: Boolean = false,
    val clientes: List<UsuarioResumen> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class EmpleadoClientesViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(EmpleadoClientesUiState())
    val uiState: StateFlow<EmpleadoClientesUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val r = apiService.obtenerUsuarios(rol = "Cliente")
                if (r.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, clientes = r.body()?.usuarios.orEmpty()) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "HTTP ${r.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }
}

@Composable
fun EmpleadoClientesContent(
    viewModel: EmpleadoClientesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.cargar() }

    val filtrados = uiState.clientes.filter {
        query.isBlank() || listOf(it.nombre, it.apellidoPaterno, it.email).joinToString(" ").contains(query, true)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("CLIENTES", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Buscar por nombre/email") },
            leadingIcon = { Icon(Icons.Default.Search, null) }
        )

        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.error != null -> Text("Error: ${uiState.error}")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtrados) { c ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("${c.nombre} ${c.apellidoPaterno}", fontWeight = FontWeight.Bold)
                            Text(c.email)
                        }
                    }
                }
            }
        }
    }
}