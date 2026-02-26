package com.moon.casaprestamo.presentation.cliente.perfil

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ClientePerfilUiState(
    val idCliente: Int = 0,
    val nombreCompleto: String = "",
    val curp: String = "",
    val numeroIne: String = "",
    val fechaNacimiento: String = "",
    val telefono: String = "",
    val email: String = "",
    val direccion: String = "",
    val fechaRegistro: String = "",
    val isLoading: Boolean = false
)

@HiltViewModel
class PerfilViewModel @Inject constructor(
    // private val apiService: ApiService  // ← Descomentar cuando conectes
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientePerfilUiState())
    val uiState: StateFlow<ClientePerfilUiState> = _uiState.asStateFlow()

    init {
        cargarPerfilMock()
    }

    private fun cargarPerfilMock() {
        _uiState.value = ClientePerfilUiState(
            idCliente = 1,
            nombreCompleto = "Juan Pérez García",
            curp = "PEGJ900101HDFRNN09",
            numeroIne = "1234567890123",
            fechaNacimiento = "01 de Enero, 1990",
            telefono = "81 1234 5678",
            email = "juan.perez@mail.com",
            direccion = "Av. Constitución 123, Col. Centro, Monterrey, NL",
            fechaRegistro = "15 de Enero, 2026",
            isLoading = false
        )
    }

    // TODO: Función real cuando se conecte el backend
    /*
    fun cargarPerfil(idCliente: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = apiService.obtenerPerfilCliente(idCliente)
                if (response.isSuccessful && response.body() != null) {
                    val perfil = response.body()!!
                    _uiState.update { it.copy(
                        idCliente = perfil.id,
                        nombreCompleto = perfil.nombre,
                        curp = perfil.curp,
                        numeroIne = perfil.ine,
                        // ... resto de datos
                        isLoading = false
                    )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    */
}