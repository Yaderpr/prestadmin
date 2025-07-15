// UserPreferences.kt
package com.gonzales.prestadmin.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "user_prefs"

val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

object PreferencesKeys {
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val SESION_ACTIVA = booleanPreferencesKey("sesion_activa")
    val USERNAME = stringPreferencesKey("username")
    val NOMBRE_USUARIO = stringPreferencesKey("nombre_usuario")
    val FOTO_URI = stringPreferencesKey("foto_uri")
}
class DarkThemePreferences(private val context: Context) {
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[PreferencesKeys.DARK_MODE] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DARK_MODE] = enabled
        }
    }
}
class SessionPreferences(private val context: Context) {

    val sesionActivaFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[PreferencesKeys.SESION_ACTIVA] ?: false }

    val usernameFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[PreferencesKeys.USERNAME] ?: "" }

    suspend fun guardarSesionActiva(activa: Boolean, username: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SESION_ACTIVA] = activa
            prefs[PreferencesKeys.USERNAME] = username
        }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun estaSesionActiva(): Boolean {
        return sesionActivaFlow.first()
    }
}
class UsuarioPrefs(private val context: Context) {
    companion object {
        val KEY_NOMBRE = stringPreferencesKey("nombre_usuario")
        val KEY_FOTO_URI = stringPreferencesKey("foto_uri")
    }

    val nombre: Flow<String> = context.dataStore.data
        .map { it[KEY_NOMBRE] ?: "Usuario" }

    val fotoUri: Flow<String?> = context.dataStore.data
        .map { it[KEY_FOTO_URI] }

    suspend fun guardarNombre(nombre: String) {
        context.dataStore.edit { it[KEY_NOMBRE] = nombre }
    }

    suspend fun guardarFotoUri(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri != null) prefs[KEY_FOTO_URI] = uri
            else prefs.remove(KEY_FOTO_URI)
        }
    }
}


