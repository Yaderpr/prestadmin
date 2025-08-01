package com.gonzales.prestadmin.presentation.viewmodel.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gonzales.prestadmin.data.repository.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class EditProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    // Cargar los datos iniciales del usuario
    init {
        viewModelScope.launch {
            val user = userRepository.userFlow.first()
            print("Url actual: ${user.photoUrl}\n")
            _uiState.value = _uiState.value.copy(

                nombre = user.fullName,
                fotoUri = user.photoUrl?.toUri()
            )
        }
    }

    fun onNameChange(newName: String) {
        _uiState.value = _uiState.value.copy(nombre = newName)
    }

    fun onPhotoSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(fotoUri = uri)
    }

    fun onSaveChanges(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            var result: Result<Unit>? = null
            val currentUiState = _uiState.value
            val user = userRepository.userFlow.first()

            // Lógica de guardado para la foto
            val photoUriToUpload = currentUiState.fotoUri
            if (photoUriToUpload != null && photoUriToUpload.toString() != user.photoUrl) {
                // Asumimos que el ID de usuario ya está guardado en la sesión
                val userId = user.userId
                result = userRepository.updateProfilePicture(userId.toInt(), photoUriToUpload)
            }

            // Lógica de guardado para el nombre
            if (currentUiState.nombre != user.fullName) {
                //userRepository.updateUserName(user.userId, currentUiState.nombre)
            }
            if (result?.isSuccess == true) {
                _uiState.value = _uiState.value.copy(successMessage = "Perfil actualizado con éxito")
                // Potentially navigate back or show a SnackBar
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = result?.exceptionOrNull()?.message ?: "Error desconocido")
            }
            _uiState.value = _uiState.value.copy(isSaving = false)
            // TODO: Añadir lógica de feedback (éxito/error)
        }
    }
}

data class EditProfileUiState(
    val nombre: String = "Cargando...",
    val fotoUri: Uri? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)