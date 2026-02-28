package com.moon.casaprestamo.presentation.empleado

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moon.casaprestamo.data.models.PrestamoData
import com.moon.casaprestamo.data.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrestamosEmpleadoUiState(
    val isLoading: Boolean = false,
    val prestamos: List<PrestamoData> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class EmpleadoPrestamosViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(PrestamosEmpleadoUiState())
    val uiState: StateFlow<PrestamosEmpleadoUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val clientesResp = apiService.obtenerUsuarios("Cliente")
                if (!clientesResp.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, error = "No se pudieron cargar clientes") }
                    return@launch
                }
                val todos = mutableListOf<PrestamoData>()
                clientesResp.body()?.usuarios.orEmpty().forEach { c ->
                    val pResp = apiService.obtenerPrestamosCliente(c.idUsuario)
                    if (pResp.isSuccessful) todos += pResp.body().orEmpty()
                }
                _uiState.update { it.copy(isLoading = false, prestamos = todos) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }
}

@Composable
fun EmpleadoPrestamosContent(
    viewModel: EmpleadoPrestamosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var estado by remember { mutableStateOf("TODOS") }

    LaunchedEffect(Unit) { viewModel.cargar() }

    val filtrados = uiState.prestamos.filter {
        estado == "TODOS" || it.estado.equals(estado, true)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("PRÉSTAMOS", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("TODOS", "PENDIENTE", "ACTIVO", "LIQUIDADO", "RECHAZADO").forEach { e ->
                FilterChip(selected = estado == e, onClick = { estado = e }, label = { Text(e) })
            }
        }

        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.error != null -> Text("Error: ${uiState.error}")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtrados) { p ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(p.folio ?: "MSP-${p.idPrestamo}", fontWeight = FontWeight.Bold)
                            Text("Monto: $${String.format("%,.2f", p.montoTotal)}")
                            Text("Estado: ${p.estado}")
                        }
                    }
                }
            }
        }
    }
}