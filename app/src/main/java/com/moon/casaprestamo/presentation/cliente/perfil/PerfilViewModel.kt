package com.moon.casaprestamo.presentation.cliente.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moon.casaprestamo.data.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientePerfilUiState())
    val uiState: StateFlow<ClientePerfilUiState> = _uiState.asStateFlow()

    fun cargarPerfil(idCliente: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = apiService.obtenerPerfil(idCliente)
                val perfil = response.body()?.perfil
                if (response.isSuccessful && perfil != null) {
                    _uiState.update {
                        it.copy(
                            idCliente = perfil.idUsuario,
                            nombreCompleto = listOfNotNull(perfil.nombre, perfil.apellidoPaterno, perfil.apellidoMaterno)
                                .joinToString(" ")
                                .trim(),
                            curp = perfil.curp.orEmpty(),
                            numeroIne = perfil.noIdentificacion.orEmpty(),
                            fechaNacimiento = perfil.fechaNacimiento.orEmpty(),
                            telefono = perfil.telefono.orEmpty(),
                            email = perfil.email,
                            direccion = perfil.direccion.orEmpty(),
                            fechaRegistro = perfil.fechaRegistro.orEmpty(),
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
