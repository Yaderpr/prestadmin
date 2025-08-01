package com.gonzales.prestadmin.presentation.viewmodel.client

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gonzales.prestadmin.App
import com.gonzales.prestadmin.App.Companion.sessionManager
import com.gonzales.prestadmin.data.local.datastore.SessionManager
import com.gonzales.prestadmin.domain.model.client.Client
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ClientFormViewModel(
    // Inyectamos el repositorio aquí. Aunque el guardado final lo hará el servicio,
    // este repositorio podría ser usado para, por ejemplo, cargar datos existentes.
    sessionManager: SessionManager = App.sessionManager// Inyección de dependencia
) : ViewModel() {

    // --- Estados de los campos de texto ---
    val firstName = MutableStateFlow("")
    val lastName = MutableStateFlow("")
    val phoneNumber = MutableStateFlow("")
    val address = MutableStateFlow("")
    val identificationNumber = MutableStateFlow("")

    // --- Estados para las fotos del DNI ---
    val fotoFrontalBitmap = MutableStateFlow<Bitmap?>(null)
    val fotoReversaBitmap = MutableStateFlow<Bitmap?>(null)

    // --- Nombres de archivo (útiles para la UI) ---
    val nameFrontal = MutableStateFlow<String?>(null)
    val nameReversa = MutableStateFlow<String?>(null)

    // --- Estado para forzar la validación de todos los campos ---
    private val _forceValidation = MutableStateFlow(false)

    // --- Estados para errores de validación ---
    val errorFirstName: StateFlow<String?> = combine(firstName, _forceValidation) { name, force ->
        if (force || name.isNotBlank()) validateFirstName(name) else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val errorLastName: StateFlow<String?> = combine(lastName, _forceValidation) { name, force ->
        if (force || name.isNotBlank()) validateLastName(name) else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val errorPhoneNumber: StateFlow<String?> = combine(phoneNumber, _forceValidation) { phone, force ->
        if (force || phone.isNotBlank()) validatePhoneNumber(phone) else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val errorAddress: StateFlow<String?> = combine(address, _forceValidation) { address, force ->
        if (force || address.isNotBlank()) validateAddress(address) else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val errorIdentificationNumber: StateFlow<String?> = combine(identificationNumber, _forceValidation) { idNum, force ->
        if (force || idNum.isNotBlank()) validateIdentificationNumber(idNum) else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val errorDniFrontal: StateFlow<String?> = combine(fotoFrontalBitmap, _forceValidation) { bitmap, force ->
        if (force || bitmap != null) validateDniPhoto(bitmap) else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val errorDniReversa: StateFlow<String?> = combine(fotoReversaBitmap, _forceValidation) { bitmap, force ->
        if (force || bitmap != null) validateDniPhoto(bitmap) else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Funciones de actualización de estados ---
    fun updateFirstName(newValue: String) { firstName.value = newValue }
    fun updateLastName(newValue: String) { lastName.value = newValue }
    fun updatePhoneNumber(newValue: String) { phoneNumber.value = newValue }
    fun updateAddress(newValue: String) { address.value = newValue }
    fun updateIdentificationNumber(newValue: String) { identificationNumber.value = newValue }

    fun updateFotoFrontal(bitmap: Bitmap?, uri: Uri? = null) {
        fotoFrontalBitmap.value = bitmap
        nameFrontal.value = uri?.lastPathSegment ?: "No seleccionada"
    }

    fun updateFotoReversa(bitmap: Bitmap?, uri: Uri? = null) {
        fotoReversaBitmap.value = bitmap
        nameReversa.value = uri?.lastPathSegment ?: "No seleccionada"
    }

    // --- Lógica de validación ---
    private fun validateFirstName(v: String) = when {
        v.length < 3 -> "Mínimo 3 caracteres"
        !v.all { it.isLetter() || it.isWhitespace() } -> "Sólo letras y espacios"
        else -> null
    }

    private fun validateLastName(v: String) = validateFirstName(v)

    private fun validatePhoneNumber(v: String) = when {
        v.isEmpty() -> "El teléfono no puede estar vacío"
        v.length < 8 -> "Mínimo 8 dígitos"
        !v.all { it.isDigit() } -> "Sólo números"
        else -> null
    }

    private fun validateAddress(v: String) = when {
        v.isEmpty() -> "La dirección no puede estar vacía"
        v.length < 3 -> "Mínimo 3 caracteres"
        else -> null
    }

    private fun validateIdentificationNumber(v: String): String? {
        val regex = Regex("^\\d{3}-\\d{6}-\\d{4}$") // Regex simplificada, ajustar según el formato
        return when {
            v.isEmpty() -> "La cédula no puede estar vacía"
            !regex.matches(v) -> "Formato inválido (Ej: 000-000000-0000)"
            else -> null
        }
    }

    private fun validateDniPhoto(bitmap: Bitmap?): String? {
        return if (bitmap == null) "Debe seleccionar una foto" else null
    }

    /**
     * Devuelve un objeto Client listo para ser guardado por el servicio.
     * Es la única responsabilidad de este método.
     */
    suspend fun getClient(): Pair<Client, List<Bitmap?>> {
        // Obtener el ID del usuario de forma segura
        val userIdFromSession = sessionManager.sessionFlow.first().userId

        // Convertir el String a Int. Si es null, lanza una excepción clara.
        val intUserId = userIdFromSession?.toIntOrNull() ?: throw IllegalStateException("User must be logged in to create a client.")

        return Client(
            userId = intUserId,
            firstName = firstName.value,
            lastName = lastName.value,
            phoneNumber = phoneNumber.value,
            address = address.value,
            identificationNumber = identificationNumber.value,
            registrationTimestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()).format(Calendar.getInstance().time)
        ) to listOf(fotoFrontalBitmap.value, fotoReversaBitmap.value)
    }

    /**
     * Valida si todos los campos requeridos son válidos.
     * @return `true` si el formulario es válido, `false` en caso contrario.
     */
    fun validateAllFields(): Boolean {
        _forceValidation.value = true

        val isValid = errorFirstName.value == null &&
                errorLastName.value == null &&
                errorPhoneNumber.value == null &&
                errorAddress.value == null &&
                errorIdentificationNumber.value == null &&
                errorDniFrontal.value == null &&
                errorDniReversa.value == null

        return isValid
    }
}