package com.gonzales.prestadmin.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    companion object {
        private val USER_ID = stringPreferencesKey("user_id")
        private val USERNAME = stringPreferencesKey("username")
        private val FULL_NAME = stringPreferencesKey("full_name")
        private val ROLE = stringPreferencesKey("role")
        private val PHOTO_URL = stringPreferencesKey("photo_url")
    }

    suspend fun saveSession(
        userId: String,
        username: String,
        fullName: String,
        role: String,
        photoUrl: String? = ""
    ) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = userId
            prefs[USERNAME] = username
            prefs[FULL_NAME] = fullName
            prefs[ROLE] = role
            prefs[PHOTO_URL] = photoUrl ?: ""
        }
    }

    suspend fun updateUserPhoto(photoUrl: String) {
        context.dataStore.edit { prefs ->
            prefs[PHOTO_URL] = photoUrl
        }
    }

    data class SessionData(
        val userId: String?,
        val username: String?,
        val fullName: String?,
        val role: String?,
        val photoUrl: String?
    )

    val sessionFlow: Flow<SessionData> = context.dataStore.data.map { prefs ->
        SessionData(
            userId = prefs[USER_ID],
            username = prefs[USERNAME],
            fullName = prefs[FULL_NAME],
            role = prefs[ROLE],
            photoUrl = prefs[PHOTO_URL]
        )
    }

    // Nuevo flujo para indicar si hay sesión activa
    val isSessionActiveFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        val userId = prefs[USER_ID]
        !userId.isNullOrBlank()
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
