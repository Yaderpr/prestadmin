package com.gonzales.prestadmin.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

// Extensión para obtener la instancia de DataStore
private val Context.themeDataStore by preferencesDataStore(name = "theme")

/**
 * Implementación de ThemeRepository que utiliza DataStore para persistir el estado del tema.
 */
class ThemeManager(private val context: Context) {

    private object PreferencesKeys {
        // Esta clave guarda si el usuario *ha seleccionado* un tema (true si Dark, false si Light).
        // Si no existe, significa que el usuario no ha tocado el switch y se usa el tema del sistema.
        val USER_SELECTED_DARK_MODE = booleanPreferencesKey("user_selected_dark_mode")
    }

    val userSelectedDarkModePreference: Flow<Boolean?> = context.themeDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_SELECTED_DARK_MODE] // Si no existe, es null
        }

    suspend fun setUserSelectedDarkMode(enabled: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_SELECTED_DARK_MODE] = enabled
        }
    }

    suspend fun resetUserSelection() {
        context.themeDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USER_SELECTED_DARK_MODE)
        }
    }

    /**
     * Método para obtener la preferencia del usuario de forma síncrona.
     * Útil para inicialización, pero no reactivo.
     */
    fun getUserSelectedDarkModeSynchronously(): Boolean? {
        // runBlocking para acceder a DataStore de forma síncrona
        return runBlocking {
            context.themeDataStore.data.first()[PreferencesKeys.USER_SELECTED_DARK_MODE]
        }
    }
}