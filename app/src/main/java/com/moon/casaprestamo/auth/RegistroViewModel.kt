package com.moon.casaprestamo.auth

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
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

private const val TAG_REG = "REGISTRO_VM"
private const val TAG_VER = "VERIFICAR_EMAIL_VM"

// ══════════════════════════════════════════════════════════════════
// EVENTS
// ══════════════════════════════════════════════════════════════════

sealed class RegistroEvent {
    data class NombreChanged(val valor: String)           : RegistroEvent()
    data class ApellidoPaternoChanged(val valor: String)  : RegistroEvent()
    data class ApellidoMaternoChanged(val valor: String)  : RegistroEvent()
    data class CurpChanged(val valor: String)             : RegistroEvent()
    data class IneChanged(val valor: String)              : RegistroEvent()
    data class TelefonoChanged(val valor: String)         : RegistroEvent()
    data class EmailChanged(val valor: String)            : RegistroEvent()
    data class DireccionChanged(val valor: String)        : RegistroEvent()
    data class FechaNacimientoChanged(val valor: String)  : RegistroEvent()
    data class PasswordChanged(val valor: String)         : RegistroEvent()
    object RegistrarClicked : RegistroEvent()
    object DismissError : RegistroEvent()
}

// ══════════════════════════════════════════════════════════════════
// REGISTRO VIEWMODEL
// ══════════════════════════════════════════════════════════════════

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun onEvent(event: RegistroEvent) {
        when (event) {
            is RegistroEvent.NombreChanged          -> _uiState.update { it.copy(nombre = event.valor) }
            is RegistroEvent.ApellidoPaternoChanged -> _uiState.update { it.copy(apellidoPaterno = event.valor) }
            is RegistroEvent.ApellidoMaternoChanged -> _uiState.update { it.copy(apellidoMaterno = event.valor) }
            is RegistroEvent.CurpChanged            -> _uiState.update { it.copy(curp = event.valor.uppercase()) }
            is RegistroEvent.IneChanged             -> _uiState.update { it.copy(ine = event.valor) }
            is RegistroEvent.TelefonoChanged        -> _uiState.update { it.copy(telefono = event.valor) }
            is RegistroEvent.EmailChanged           -> _uiState.update { it.copy(email = event.valor) }
            is RegistroEvent.DireccionChanged       -> _uiState.update { it.copy(direccion = event.valor) }
            is RegistroEvent.FechaNacimientoChanged -> _uiState.update { it.copy(fechaNacimiento = event.valor) }
            is RegistroEvent.PasswordChanged        -> _uiState.update { it.copy(password = event.valor) }
            RegistroEvent.DismissError              -> _uiState.update { it.copy(errorMessage = null) }
            RegistroEvent.RegistrarClicked          -> registrar()
        }
    }

    private fun registrar() {
        val state = _uiState.value

        if (state.nombre.isBlank() || state.apellidoPaterno.isBlank() ||
            state.email.isBlank() || state.password.isBlank()
        ) {
            _uiState.update { it.copy(errorMessage = "Completa los campos obligatorios") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // El nuevo RegistroRequest usa email como identificador principal (no username)
                val request = RegistroRequest(
                    nombre            = state.nombre.trim(),
                    apellido_paterno  = state.apellidoPaterno.trim(),
                    apellido_materno  = state.apellidoMaterno.trim(),
                    email             = state.email.trim(),
                    password          = state.password,
                    curp              = state.curp.trim().uppercase().ifBlank { null },
                    telefono          = state.telefono.trim().ifBlank { null },
                    direccion         = state.direccion.trim().ifBlank { null },
                    no_identificacion = state.ine.trim().ifBlank { null }
                )

                Log.d(TAG_REG, "POST /registrar_cliente → email: ${request.email}")
                val response = apiService.registrarCliente(request)
                Log.d(TAG_REG, "HTTP ${response.code()} | body: ${response.body()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        // Registro OK → navegar a pantalla de verificación de email
                        _uiState.update {
                            it.copy(
                                isLoading         = false,
                                registroExitoso   = true,
                                emailParaVerificar = state.email.trim()
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = body?.message ?: "Error inesperado")
                        }
                    }
                } else {
                    val msg = response.getErrorMessage()
                    Log.e(TAG_REG, "❌ Error ${response.code()}: $msg")
                    _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                }
            } catch (e: Exception) {
                Log.e(TAG_REG, "💥 Excepción", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error de red: ${e.localizedMessage}") }
            }
        }
    }

    private fun <T> Response<T>.getErrorMessage(): String {
        return try {
            JSONObject(errorBody()?.string() ?: "").optString("detail", "Error en el servidor")
        } catch (e: Exception) { "Error en el servidor" }
    }
}

// ══════════════════════════════════════════════════════════════════
// VERIFICAR EMAIL VIEWMODEL (nuevo flujo post-registro)
// ══════════════════════════════════════════════════════════════════

@HiltViewModel
class VerificarEmailViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerificarEmailUiState())
    val uiState: StateFlow<VerificarEmailUiState> = _uiState.asStateFlow()

    fun setEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onCodigoChange(value: String) {
        if (value.length <= 6) _uiState.update { it.copy(codigo = value, errorMessage = null) }
    }

    fun verificar() {
        val state = _uiState.value
        if (state.codigo.length != 6) {
            _uiState.update { it.copy(errorMessage = "El código debe tener 6 dígitos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                Log.d(TAG_VER, "POST /verificar_email → email: ${state.email}")
                val response = apiService.verificarEmail(
                    VerificarEmailRequest(email = state.email, codigo = state.codigo)
                )
                Log.d(TAG_VER, "HTTP ${response.code()} | body: ${response.body()}")

                if (response.isSuccessful) {
                    Log.d(TAG_VER, "✅ Email verificado")
                    _uiState.update { it.copy(isLoading = false, verificado = true) }
                } else {
                    val msg = response.getErrorMessage()
                    Log.e(TAG_VER, "❌ Error ${response.code()}: $msg")
                    // 410 expirado → limpiamos código para que reingrese
                    val limpiar = response.code() == 410
                    _uiState.update {
                        it.copy(
                            isLoading    = false,
                            codigo       = if (limpiar) "" else it.codigo,
                            errorMessage = msg
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG_VER, "💥 Excepción", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error de red: ${e.localizedMessage}") }
            }
        }
    }

    fun reenviarCodigo() {
        val email = _uiState.value.email
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, codigo = "") }
            try {
                Log.d(TAG_VER, "POST /reenviar_codigo_verificacion → email: $email")
                val response = apiService.reenviarCodigoVerificacion(email)
                Log.d(TAG_VER, "HTTP ${response.code()}")
                val msg = if (response.isSuccessful) null else response.getErrorMessage()
                _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error de red: ${e.localizedMessage}") }
            }
        }
    }

    private fun <T> Response<T>.getErrorMessage(): String {
        return try {
            JSONObject(errorBody()?.string() ?: "").optString("detail", "Error en el servidor")
        } catch (e: Exception) { "Error en el servidor" }
    }
}