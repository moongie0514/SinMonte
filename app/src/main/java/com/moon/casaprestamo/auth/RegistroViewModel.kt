package com.moon.casaprestamo.auth

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import javax.inject.Inject
private const val TAG_REG = "REGISTRO_VM"
private const val TAG_VER = "VERIFICAR_EMAIL_VM"

// ══════════════════════════════════════════════════════════════════
// TIPOS DE IDENTIFICACIÓN
// ══════════════════════════════════════════════════════════════════

enum class TipoIdentificacion(val label: String, val prefijo: String) {
    INE("INE / IFE", "INE"),
    PASAPORTE("Pasaporte", "PASAPORTE"),
    CEDULA("Cédula Profesional", "CEDULA")
}

// ══════════════════════════════════════════════════════════════════
// EVENTS
// ══════════════════════════════════════════════════════════════════

sealed class RegistroEvent {
    data class NombreChanged(val valor: String)            : RegistroEvent()
    data class ApellidoPaternoChanged(val valor: String)   : RegistroEvent()
    data class ApellidoMaternoChanged(val valor: String)   : RegistroEvent()
    data class CurpChanged(val valor: String)              : RegistroEvent()
    data class TipoIdChanged(val tipo: TipoIdentificacion) : RegistroEvent()
    data class NumeroIdChanged(val valor: String)          : RegistroEvent()
    data class TelefonoChanged(val valor: String)          : RegistroEvent()
    data class EmailChanged(val valor: String)             : RegistroEvent()
    data class DireccionChanged(val valor: String)         : RegistroEvent()
    data class FechaNacimientoChanged(val valor: String)   : RegistroEvent()
    data class PasswordChanged(val valor: String)          : RegistroEvent()
    object RegistrarClicked                                : RegistroEvent()
    object DismissError                                    : RegistroEvent()
}

// ══════════════════════════════════════════════════════════════════
// VALIDACIONES
// ══════════════════════════════════════════════════════════════════

object RegistroValidacion {

    // CURP oficial mexicana — solo se valida en validarTodo(), no en el botón
    private val CURP_REGEX = Regex(
        "^[A-Z]{4}\\d{6}[HM][A-Z]{2}[BCDFGHJKLMNÑPQRSTVWXYZ]{3}[A-Z0-9]\\d$"
    )

    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

    fun validarCurp(curp: String): String? = when {
        curp.isBlank()        -> "La CURP es obligatoria"
        curp.length != 18     -> "La CURP debe tener exactamente 18 caracteres (${curp.length}/18)"
        !CURP_REGEX.matches(curp) -> "El formato de la CURP no es válido"
        else -> null
    }

    fun validarTelefono(tel: String): String? = when {
        tel.isBlank()              -> "El teléfono es obligatorio"
        tel.length != 10           -> "El teléfono debe tener exactamente 10 dígitos (${tel.length}/10)"
        !tel.all { it.isDigit() }  -> "El teléfono solo debe contener dígitos"
        else -> null
    }

    fun validarEmail(email: String): String? = when {
        email.isBlank()                -> "El correo es obligatorio"
        !EMAIL_REGEX.matches(email)    -> "El formato del correo no es válido"
        else -> null
    }

    fun validarPassword(pass: String): String? = when {
        pass.isBlank()                      -> "La contraseña es obligatoria"
        pass.length < 8                     -> "Mínimo 8 caracteres"
        !pass.any { it.isUpperCase() }      -> "Debe incluir al menos una mayúscula"
        !pass.any { it.isDigit() }          -> "Debe incluir al menos un número"
        !pass.any { !it.isLetterOrDigit() } -> "Debe incluir al menos un carácter especial (!@#\$...)"
        else -> null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun validarEdad(fechaNacimiento: String): String? {
        if (fechaNacimiento.isBlank()) return "La fecha de nacimiento es obligatoria"
        return try {
            val fecha = LocalDate.parse(fechaNacimiento, DateTimeFormatter.ISO_LOCAL_DATE)
            val edad  = Period.between(fecha, LocalDate.now()).years
            if (edad < 18) "Debes ser mayor de 18 años para registrarte" else null
        } catch (e: Exception) { "Fecha de nacimiento inválida" }
    }

    // Validación completa al presionar el botón — retorna el primer error
    @RequiresApi(Build.VERSION_CODES.O)
    fun validarTodo(state: RegistroUiState): String? {
        if (state.nombre.isBlank())          return "El nombre es obligatorio"
        if (state.apellidoPaterno.isBlank()) return "El apellido paterno es obligatorio"
        if (state.direccion.isBlank())       return "La dirección es obligatoria"
        if (state.numeroId.isBlank())        return "El número de identificación es obligatorio"
        validarCurp(state.curp)?.let            { return it }
        validarTelefono(state.telefono)?.let    { return it }
        validarEmail(state.email)?.let          { return it }
        validarPassword(state.password)?.let    { return it }
        validarEdad(state.fechaNacimiento)?.let { return it }
        return null
    }

    // ── Habilita el botón ─────────────────────────────────────────
    // Validación LAXA — solo verifica que los campos tienen contenido
    // con la longitud mínima esperada. Los formatos exactos se validan
    // al presionar el botón con validarTodo() para no frustrar al usuario.
    fun formularioCompleto(state: RegistroUiState): Boolean =
        state.nombre.isNotBlank()          &&
                state.apellidoPaterno.isNotBlank() &&
                state.curp.length == 18            &&
                state.telefono.length == 10        &&
                state.email.contains("@")          &&  // mínimo un @ — no regex completo
                state.password.length >= 8         &&
                state.password.any { it.isUpperCase() }      &&
                state.password.any { it.isDigit() }          &&
                state.password.any { !it.isLetterOrDigit() } &&
                state.fechaNacimiento.isNotBlank() &&
                state.numeroId.isNotBlank()        &&
                state.direccion.isNotBlank()
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun onEvent(event: RegistroEvent) {
        when (event) {
            is RegistroEvent.NombreChanged          -> _uiState.update { it.copy(nombre = event.valor) }
            is RegistroEvent.ApellidoPaternoChanged -> _uiState.update { it.copy(apellidoPaterno = event.valor) }
            is RegistroEvent.ApellidoMaternoChanged -> _uiState.update { it.copy(apellidoMaterno = event.valor) }
            is RegistroEvent.CurpChanged            -> _uiState.update { it.copy(curp = event.valor.uppercase()) }
            is RegistroEvent.TipoIdChanged          -> _uiState.update { it.copy(tipoId = event.tipo) }
            is RegistroEvent.NumeroIdChanged        -> _uiState.update { it.copy(numeroId = event.valor) }
            is RegistroEvent.TelefonoChanged        -> _uiState.update { it.copy(telefono = event.valor) }
            is RegistroEvent.EmailChanged           -> _uiState.update { it.copy(email = event.valor) }
            is RegistroEvent.DireccionChanged       -> _uiState.update { it.copy(direccion = event.valor) }
            is RegistroEvent.FechaNacimientoChanged -> _uiState.update { it.copy(fechaNacimiento = event.valor) }
            is RegistroEvent.PasswordChanged        -> _uiState.update { it.copy(password = event.valor) }
            RegistroEvent.DismissError              -> _uiState.update { it.copy(errorMessage = null) }
            RegistroEvent.RegistrarClicked          -> registrar()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun registrar() {
        val state = _uiState.value

        // Validación completa con mensajes descriptivos
        val error = RegistroValidacion.validarTodo(state)
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        // Prefijado: "INE: 1234567890" / "PASAPORTE: G12345" / "CEDULA: 87654321"
        val identificacion = "${state.tipoId.prefijo}: ${state.numeroId.trim()}"

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val request = RegistroRequest(
                    nombre            = state.nombre.trim(),
                    apellido_paterno  = state.apellidoPaterno.trim(),
                    apellido_materno  = state.apellidoMaterno.trim().ifBlank { null },
                    email             = state.email.trim(),
                    password          = state.password,
                    curp              = state.curp.uppercase(),
                    telefono          = state.telefono.trim(),
                    direccion         = state.direccion.trim(),
                    no_identificacion = identificacion,
                    fecha_nacimiento  = state.fechaNacimiento
                )

                Log.d(TAG_REG, "POST /registrar_cliente → email: ${request.email}")
                val response = apiService.registrarCliente(request)
                Log.d(TAG_REG, "HTTP ${response.code()}")

                if (response.isSuccessful && response.body()?.status == "success") {
                    _uiState.update {
                        it.copy(
                            isLoading          = false,
                            registroExitoso    = true,
                            emailParaVerificar = state.email.trim()
                        )
                    }
                } else {
                    val msg = if (response.isSuccessful)
                        response.body()?.message ?: "Error inesperado"
                    else
                        response.getErrorMessage()
                    _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                }
            } catch (e: Exception) {
                Log.e(TAG_REG, "💥 Excepción", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error de red: ${e.localizedMessage}") }
            }
        }
    }

    private fun <T> Response<T>.getErrorMessage(): String {
        return try { JSONObject(errorBody()?.string() ?: "").optString("detail", "Error en el servidor") }
        catch (e: Exception) { "Error en el servidor" }
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