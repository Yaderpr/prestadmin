package com.gonzales.prestadmin.presentation.viewmodel.theme
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gonzales.prestadmin.data.local.datastore.ThemeManager
import kotlinx.coroutines.launch


class ThemeViewModel(
    private val themeManager: ThemeManager,
    private val systemDark: Boolean
) : ViewModel() {

    // Estado único que la UI leerá
    var isDarkMode = mutableStateOf(false)
        private set

    init {
        // 1) Arranca con el modo del sistema
        isDarkMode.value = systemDark

        // 2) Si el usuario ya guardó algo, lo leemos sincrónicamente
        themeManager.getUserSelectedDarkModeSynchronously()?.let { pref ->
            isDarkMode.value = pref
        }
    }

    /** Alterna el tema y lo persiste */
    fun toggleDarkMode() {
        val next = !isDarkMode.value
        isDarkMode.value = next
        viewModelScope.launch {
            themeManager.setUserSelectedDarkMode(next)
        }
    }

    /** Vuelve a seguir el tema del sistema */
    fun resetToSystem() {
        isDarkMode.value = systemDark
        viewModelScope.launch {
            themeManager.resetUserSelection()
        }
    }
}
