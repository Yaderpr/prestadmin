package com.gonzales.prestadmin.presentation.viewmodel.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gonzales.prestadmin.data.repository.user.UserRepository // Importa el UserRepository
import com.gonzales.prestadmin.domain.model.user.User // Asegúrate de usar el modelo correcto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginActivityViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> get() = _loginState

    fun login(username: String, passwordPlain: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            // Llama a la función del repositorio que ahora encapsula toda la lógica
            val user = userRepository.authenticateUser(username, passwordPlain)

            // Gestiona el estado de la UI basándose en el resultado del repositorio
            if (user != null) {
                _loginState.value = LoginState.Success(user)
            } else {
                // El repositorio ya maneja los errores internos, por lo que podemos dar un mensaje genérico.
                // O podrías hacer que el repositorio devuelva un resultado más detallado.
                _loginState.value = LoginState.Error("Fallo en la autenticación. Revisa tus credenciales.")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}