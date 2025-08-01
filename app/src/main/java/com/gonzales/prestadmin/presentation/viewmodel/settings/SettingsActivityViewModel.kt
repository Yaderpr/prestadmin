package com.gonzales.prestadmin.presentation.viewmodel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gonzales.prestadmin.data.repository.user.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


class SettingsActivityViewModel(userRepository: UserRepository) : ViewModel() {

    // Simplemente expone el userFlow del repositorio directamente.
    // Asumimos que MainViewModel (u otra parte) ya se encarga de poblar photoUrl si es necesario.
    val user: StateFlow<UserRepository.UserUiModel> =
        userRepository.userFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(500), // Usar 5000ms como en MainViewModel
                initialValue = UserRepository.UserUiModel(
                    userId = "",
                    fullName = "Cargando...",
                    username = "Cargando...",
                    role = "Cargando...",
                    photoUrl = null
                )
            )

    // Si Settings tiene lógica específica para actualizar el perfil (nombre, etc.),
    // esos métodos irían aquí.
    // Por ejemplo, para la foto, podrías tener un método como:
    /*
    fun updateProfilePicture(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            // Llama al repositorio para actualizar la foto.
            // El repositorio se encargará de subir, guardar la URL y actualizar la sesión.
            userRepository.updateProfilePicture(context, /* obtener el userId del user.value.userId */, imageUri)
            // No necesitas actualizar el user.value aquí, el userFlow del repositorio
            // debería emitir automáticamente el nuevo UserUiModel con la URL actualizada.
        }
    }
    */
    // Y si se permite cerrar sesión desde Settings, también lo manejaría aquí:
    /*
    fun logout() {
        viewModelScope.launch {
            userRepository.clearUserSession()
        }
    }
    */
}