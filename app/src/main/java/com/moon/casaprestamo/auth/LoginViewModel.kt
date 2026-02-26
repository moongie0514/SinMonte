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

private const val TAG = "LOGIN_VM"

sealed class LoginEvent {
    data class UsernameChanged(val username: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object LoginClicked : LoginEvent()
    object DismissError : LoginEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.UsernameChanged -> _uiState.update { it.copy(username = event.username) }
            is LoginEvent.PasswordChanged -> _uiState.update { it.copy(password = event.password) }
            LoginEvent.LoginClicked       -> login()
            LoginEvent.DismissError       -> _uiState.update { it.copy(loginResult = null) }
        }
    }

    private fun login() {
        val email    = _uiState.value.username.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(loginResult = LoginResult.Error("Ingresa usuario y contraseña")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loginResult = null) }
            try {
                Log.d(TAG, "POST /login → email: $email")
                val response = apiService.login(LoginRequest(email = email, password = password))
                Log.d(TAG, "HTTP ${response.code()} | body: ${response.body()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    val usuario = body?.usuario

                    when {
                        usuario == null -> {
                            _uiState.update {
                                it.copy(isLoading = false, loginResult = LoginResult.Error(body?.detail ?: "Error desconocido"))
                            }
                        }
                        !usuario.emailVerificado -> {
                            // El servidor también retorna 403, pero si por alguna razón llega 200
                            // con emailVerificado=false lo manejamos aquí
                            _uiState.update {
                                it.copy(isLoading = false, loginResult = LoginResult.Error("Email no verificado. Revisa tu correo."))
                            }
                        }
                        else -> {
                            Log.d(TAG, "✅ Login exitoso — rol: ${usuario.rol}, id: ${usuario.idUsuario}")
                            _uiState.update {
                                it.copy(isLoading = false, loginResult = LoginResult.Success(usuario))
                            }
                        }
                    }
                } else {
                    val msg = response.getErrorMessage()
                    Log.e(TAG, "❌ Error ${response.code()}: $msg")
                    _uiState.update { it.copy(isLoading = false, loginResult = LoginResult.Error(msg)) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Excepción", e)
                _uiState.update {
                    it.copy(isLoading = false, loginResult = LoginResult.Error("Error de red: ${e.localizedMessage}"))
                }
            }
        }
    }

    private fun <T> Response<T>.getErrorMessage(): String {
        return try {
            JSONObject(errorBody()?.string() ?: "").optString("detail", "Credenciales incorrectas")
        } catch (e: Exception) { "Error en el servidor" }
    }
}