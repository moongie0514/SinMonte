package com.moon.casaprestamo.presentation.admin.cuentas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moon.casaprestamo.data.models.*
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
    val usuarios:  List<UsuarioResumen> = emptyList(),
    val error:     String? = null,
    val mensaje:   String? = null,
    val esError:   Boolean = false
)

@HiltViewModel
class CuentasViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CuentasUiState())
    val uiState: StateFlow<CuentasUiState> = _uiState.asStateFlow()

    fun cargar(rol: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, mensaje = null) }
            try {
                val response = apiService.obtenerUsuarios(rol = rol)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, usuarios = response.body()?.usuarios.orEmpty()) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Error HTTP ${response.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Error de red") }
            }
        }
    }

    fun crearEmpleado(nombre: String, apellidoPaterno: String, apellidoMaterno: String,
                      email: String, telefono: String, password: String, rol: String) {
        viewModelScope.launch {
            try {
                val r = apiService.crearEmpleado(
                    CrearEmpleadoRequest(
                        nombre          = nombre,
                        apellidoPaterno = apellidoPaterno,
                        apellidoMaterno = apellidoMaterno.ifBlank { null },
                        email           = email,
                        password        = password,
                        telefono        = telefono.ifBlank { null },
                        rol             = rol
                    )
                )
                Log.d("CUENTAS_VM", "crearEmpleado HTTP ${r.code()}")
                _uiState.update { it.copy(mensaje = if (r.isSuccessful) "✅ Empleado creado" else "No se pudo crear", esError = !r.isSuccessful) }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = "Error al crear", esError = true) }
            }
        }
    }

    fun crearCliente(nombre: String, apellidoPaterno: String, apellidoMaterno: String,
                     email: String, telefono: String, curp: String, ine: String, password: String) {
        viewModelScope.launch {
            try {
                val r = apiService.registrarCliente(
                    RegistroRequest(
                        nombre            = nombre,
                        apellido_paterno  = apellidoPaterno,
                        apellido_materno  = apellidoMaterno.ifBlank { null },
                        email             = email,
                        password          = password,
                        curp              = curp.ifBlank { null },
                        telefono          = telefono.ifBlank { null },
                        direccion         = null,
                        no_identificacion = ine.ifBlank { null },
                        fecha_nacimiento  = null
                    )
                )
                Log.d("CUENTAS_VM", "crearCliente HTTP ${r.code()}")
                _uiState.update { it.copy(mensaje = if (r.isSuccessful) "✅ Cliente creado" else "No se pudo crear", esError = !r.isSuccessful) }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = "Error al crear", esError = true) }
            }
        }
    }

    fun editarUsuario(idUsuario: Int, nombre: String, apellidoPaterno: String,
                      apellidoMaterno: String, telefono: String, curp: String, ine: String) {
        viewModelScope.launch {
            try {
                val r = apiService.editarUsuarioAdmin(
                    idUsuario,
                    EditarUsuarioAdminRequest(
                        nombre          = nombre,
                        apellidoPaterno = apellidoPaterno,
                        apellidoMaterno = apellidoMaterno.ifBlank { null },
                        telefono        = telefono.ifBlank { null },
                        direccion       = null,
                        curp            = curp.ifBlank { null },
                        noIdentificacion = ine.ifBlank { null }
                    )
                )
                _uiState.update { it.copy(mensaje = if (r.isSuccessful) "✅ Usuario actualizado" else "No se pudo actualizar", esError = !r.isSuccessful) }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = "Error al actualizar", esError = true) }
            }
        }
    }

    fun cambiarEstado(idUsuario: Int, activo: Boolean) {
        viewModelScope.launch {
            try {
                val r = apiService.cambiarEstadoUsuario(idUsuario, activo)
                _uiState.update { it.copy(mensaje = if (r.isSuccessful) "✅ Estado actualizado" else "No se pudo actualizar estado", esError = !r.isSuccessful) }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = "Error al cambiar estado", esError = true) }
            }
        }
    }
}