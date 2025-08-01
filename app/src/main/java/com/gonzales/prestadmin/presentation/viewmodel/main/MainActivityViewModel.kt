package com.gonzales.prestadmin.presentation.viewmodel.main



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gonzales.prestadmin.data.repository.user.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivityViewModel(private val userRepository: UserRepository) : ViewModel() {

    // Exponemos el userFlow del repositorio directamente como StateFlow
    @OptIn(ExperimentalCoroutinesApi::class)
    val user: StateFlow<UserRepository.UserUiModel> = userRepository.userFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(500), // Mantiene el flow activo mientras haya suscriptores
            initialValue = UserRepository.UserUiModel(
                userId = "0", // O un valor por defecto que indique "cargando"
                fullName = "Cargando...",
                username = "Cargando...",
                role = "Cargando...",
                photoUrl = null // Importante: el valor inicial de la URL de la foto
            )
        )

    fun logout() {
        viewModelScope.launch {
            userRepository.clearUserSession()
        }
    }
}