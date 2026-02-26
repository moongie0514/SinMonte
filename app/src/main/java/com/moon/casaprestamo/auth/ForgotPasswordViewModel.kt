package com.moon.casaprestamo.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moon.casaprestamo.data.models.VerificarCodigoRequest
import com.moon.casaprestamo.data.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "FORGOT_PWD"

data class ForgotPasswordUiState(
    val step: Int = 1,
    val email: String = "",
    val codigo: String = "",
    val nuevaPassword: String = "",
    val confirmarPassword: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private fun extraerError(errorBody: okhttp3.ResponseBody?, fallback: String): String {
        return try {
            val raw = errorBody?.string() ?: ""
            Log.e(TAG, "errorBody raw: $raw")
            JSONObject(raw).optString("detail", fallback)
        } catch (e: Exception) {
            fallback
        }
    }

    // ── Paso 1 ────────────────────────────────────────────────────────────────
    fun solicitarCodigo() {
        val email = _uiState.value.email.trim()

        Log.d(TAG, "══════════════════════════════════")
        Log.d(TAG, "solicitarCodigo() → email: $email")

        if (!email.contains("@") || !email.contains(".")) {
            Log.w(TAG, "Email inválido, abortando")
            _uiState.update { it.copy(error = "Ingresa un correo válido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                Log.d(TAG, "Llamando POST /solicitar_codigo?email=$email")
                val response = apiService.solicitarCodigo(email)

                Log.d(TAG, "HTTP ${response.code()}")
                Log.d(TAG, "isSuccessful: ${response.isSuccessful}")
                Log.d(TAG, "body: ${response.body()}")

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Código solicitado — avanzando a paso 2")
                    _uiState.update { it.copy(isLoading = false, step = 2) }
                } else {
                    val msg = extraerError(response.errorBody(), "No se pudo enviar el código")
                    Log.e(TAG, "❌ Error del servidor: $msg")
                    _uiState.update { it.copy(isLoading = false, error = msg) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Excepción de red", e)
                _uiState.update { it.copy(isLoading = false, error = "Sin conexión: ${e.localizedMessage}") }
            }
        }
    }

    // ── Paso 2 ────────────────────────────────────────────────────────────────
    fun verificarCodigo() {
        val codigo = _uiState.value.codigo
        Log.d(TAG, "══════════════════════════════════")
        Log.d(TAG, "verificarCodigo() → longitud código: ${codigo.length}")

        if (codigo.length != 6) {
            Log.w(TAG, "Código incompleto")
            _uiState.update { it.copy(error = "El código debe tener 6 dígitos") }
            return
        }
        Log.d(TAG, "✅ Código válido localmente — avanzando a paso 3")
        _uiState.update { it.copy(error = null, step = 3) }
    }

    // ── Paso 3 ────────────────────────────────────────────────────────────────
    fun cambiarPassword() {
        val state = _uiState.value
        Log.d(TAG, "══════════════════════════════════")
        Log.d(TAG, "cambiarPassword() → email: ${state.email}, código: ${state.codigo}")

        when {
            state.nuevaPassword.length < 6 -> {
                Log.w(TAG, "Contraseña muy corta")
                _uiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
                return
            }
            state.nuevaPassword != state.confirmarPassword -> {
                Log.w(TAG, "Contraseñas no coinciden")
                _uiState.update { it.copy(error = "Las contraseñas no coinciden") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val request = VerificarCodigoRequest(
                    email          = state.email.trim(),
                    codigo         = state.codigo,
                    nueva_password = state.nuevaPassword
                )
                Log.d(TAG, "Llamando POST /verificar_codigo")
                Log.d(TAG, "Request: email=${request.email}, codigo=${request.codigo}")

                val response = apiService.verificarCodigo(request)

                Log.d(TAG, "HTTP ${response.code()}")
                Log.d(TAG, "isSuccessful: ${response.isSuccessful}")
                Log.d(TAG, "body: ${response.body()}")

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Contraseña actualizada — avanzando a paso 4")
                    _uiState.update { it.copy(isLoading = false, step = 4) }
                } else {
                    val msg = extraerError(response.errorBody(), "Código incorrecto o expirado")
                    Log.e(TAG, "❌ Error del servidor: $msg (HTTP ${response.code()})")
                    val volverAlPaso = response.code() == 401 || response.code() == 410
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            step      = if (volverAlPaso) 2 else it.step,
                            codigo    = if (volverAlPaso) "" else it.codigo,
                            error     = msg
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Excepción de red", e)
                _uiState.update { it.copy(isLoading = false, error = "Sin conexión: ${e.localizedMessage}") }
            }
        }
    }

    fun reenviarCodigo() {
        Log.d(TAG, "reenviarCodigo() → limpiando código y reintentando")
        _uiState.update { it.copy(codigo = "", error = null) }
        solicitarCodigo()
    }

    fun onEmailChange(value: String)             = _uiState.update { it.copy(email = value, error = null) }
    fun onCodigoChange(value: String)            { if (value.length <= 6) _uiState.update { it.copy(codigo = value, error = null) } }
    fun onNuevaPasswordChange(value: String)     = _uiState.update { it.copy(nuevaPassword = value, error = null) }
    fun onConfirmarPasswordChange(value: String) = _uiState.update { it.copy(confirmarPassword = value, error = null) }
    fun togglePasswordVisible()                  = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
}