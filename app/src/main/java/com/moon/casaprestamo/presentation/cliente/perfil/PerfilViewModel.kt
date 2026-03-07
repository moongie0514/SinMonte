package com.moon.casaprestamo.presentation.cliente.perfil

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moon.casaprestamo.data.models.ActualizarPerfilRequest
import com.moon.casaprestamo.data.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "PERFIL_VM"

data class ClientePerfilUiState(
    val idCliente:      Int     = 0,
    val nombreCompleto: String  = "",
    // Campos editables separados para poder modificarlos sin tocar el original
    val nombre:         String  = "",
    val apellidoPaterno:String  = "",
    val apellidoMaterno:String  = "",
    val telefono:       String  = "",
    val direccion:      String  = "",
    // Campos de solo lectura
    val email:          String  = "",
    val curp:           String  = "",
    val numeroIne:      String  = "",
    val fechaNacimiento:String  = "",
    val fechaRegistro:  String  = "",
    // Control de estado
    val isLoading:      Boolean = false,
    val isSaving:       Boolean = false,
    val mensaje:        String? = null,
    val esError:        Boolean = false
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
                val perfil   = response.body()?.perfil

                if (response.isSuccessful && perfil != null) {
                    Log.d(TAG, "✅ Perfil cargado: ${perfil.nombre} ${perfil.apellidoPaterno}")
                    _uiState.update {
                        it.copy(
                            idCliente       = perfil.idUsuario,
                            nombreCompleto  = listOfNotNull(perfil.nombre, perfil.apellidoPaterno, perfil.apellidoMaterno)
                                .joinToString(" ").trim(),
                            nombre          = perfil.nombre.orEmpty(),
                            apellidoPaterno = perfil.apellidoPaterno.orEmpty(),
                            apellidoMaterno = perfil.apellidoMaterno.orEmpty(),
                            telefono        = perfil.telefono.orEmpty(),
                            direccion       = perfil.direccion.orEmpty(),
                            email           = perfil.email,
                            curp            = perfil.curp.orEmpty(),
                            numeroIne       = perfil.noIdentificacion.orEmpty(),
                            fechaNacimiento = perfil.fechaNacimiento.orEmpty(),
                            fechaRegistro   = perfil.fechaRegistro.orEmpty(),
                            isLoading       = false,
                            mensaje         = null
                        )
                    }
                } else {
                    Log.e(TAG, "❌ Error cargando perfil: HTTP ${response.code()}")
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 ${e.localizedMessage}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun guardarPerfil() {
        val state = _uiState.value
        if (state.nombre.isBlank() || state.apellidoPaterno.isBlank()) {
            _uiState.update { it.copy(mensaje = "El nombre y apellido paterno son obligatorios", esError = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, mensaje = null) }
            try {
                Log.d(TAG, "PUT /cliente/${state.idCliente}/perfil")
                val request = ActualizarPerfilRequest(
                    nombre           = state.nombre.trim(),
                    apellido_paterno = state.apellidoPaterno.trim(),
                    apellido_materno = state.apellidoMaterno.trim().ifBlank { null },
                    telefono         = state.telefono.trim().ifBlank { null },
                    direccion        = state.direccion.trim().ifBlank { null }
                )
                val response = apiService.actualizarPerfil(state.idCliente, request)

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Perfil actualizado")
                    // Actualizar nombreCompleto con los nuevos datos
                    val nuevoNombre = listOfNotNull(
                        state.nombre.trim(),
                        state.apellidoPaterno.trim(),
                        state.apellidoMaterno.trim().ifBlank { null }
                    ).joinToString(" ")
                    _uiState.update {
                        it.copy(
                            isSaving       = false,
                            nombreCompleto = nuevoNombre,
                            mensaje        = "✅ Perfil actualizado correctamente",
                            esError        = false
                        )
                    }
                } else {
                    val errorMsg = try {
                        JSONObject(response.errorBody()?.string() ?: "")
                            .optString("detail", "Error al guardar")
                    } catch (e: Exception) { "Error al guardar" }
                    Log.e(TAG, "❌ Error ${response.code()}: $errorMsg")
                    _uiState.update { it.copy(isSaving = false, mensaje = errorMsg, esError = true) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 ${e.localizedMessage}")
                _uiState.update { it.copy(isSaving = false, mensaje = "Error de red: ${e.localizedMessage}", esError = true) }
            }
        }
    }

    // ── Cambios en campos editables ──────────────────────────────
    fun onNombreChange(v: String)         = _uiState.update { it.copy(nombre = v, mensaje = null) }
    fun onApellidoPaternoChange(v: String) = _uiState.update { it.copy(apellidoPaterno = v, mensaje = null) }
    fun onApellidoMaternoChange(v: String) = _uiState.update { it.copy(apellidoMaterno = v, mensaje = null) }
    fun onTelefonoChange(v: String)        = _uiState.update { it.copy(telefono = v, mensaje = null) }
    fun onDireccionChange(v: String)       = _uiState.update { it.copy(direccion = v, mensaje = null) }
    fun limpiarMensaje()                   = _uiState.update { it.copy(mensaje = null) }
}