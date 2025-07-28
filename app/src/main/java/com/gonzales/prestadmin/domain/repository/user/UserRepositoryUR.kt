package com.gonzales.prestadmin.domain.repository.user

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.gonzales.prestadmin.data.UsuarioPrefs
import com.gonzales.prestadmin.data.remote.SupabaseClient
import com.gonzales.prestadmin.domain.model.user.User
import com.gonzales.prestadmin.domain.model.user.UserUiModel
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.mindrot.jbcrypt.BCrypt
import java.util.prefs.Preferences

class UserRepository(
    private val usuarioPrefs: UsuarioPrefs,
    private val context: Context
) {


    val userFlow: Flow<UserUiModel> = combine(
        usuarioPrefs.nombre,
        usuarioPrefs.fotoUri
    ) { nombre, fotoUri ->
        UserUiModel(
            nombre = nombre,
            fotoUri = fotoUri,
            rol = "Administrador" // si decides guardar esto también, se puede adaptar
        )
    }


    suspend fun actualizarNombre(nombre: String) {
        usuarioPrefs?.guardarNombre(nombre)
    }

    suspend fun actualizarFoto(uri: String?) {
        usuarioPrefs?.guardarFotoUri(uri)
    }

    // En UserRepository.kt
    suspend fun actualizarPerfil(nombre: String, uri: String?) {
        usuarioPrefs?.guardarNombre(nombre)
        usuarioPrefs?.guardarFotoUri(uri)
    }

    suspend fun authenticateUser(username: String, passwordPlain: String): User? {
        return try {
            // 1. Buscamos el usuario por su 'username'
            val user = SupabaseClient.client.from("users")
                .select {
                    filter {
                        eq("username", username)
                    }
                }
                .decodeSingleOrNull<User>()

            // 2. Si no encontramos el usuario, la autenticación falla
            if (user == null) {
                println("Fallo en la autenticación para el usuario '$username'. Usuario no encontrado.")
                return null
            }

            // 3. Verificamos la contraseña en texto plano contra el hash almacenado
            val storedHash = user.passwordHash // El hash que obtuvimos de la DB
            val passwordIsValid = BCrypt.checkpw(passwordPlain, storedHash)

            // 4. Devolvemos el usuario si la contraseña es válida
            if (passwordIsValid) {
                println("Usuario '$username' autenticado con éxito.")
                return user
            } else {
                println("Fallo en la autenticación para el usuario '$username'. Contraseña inválida.")
                return null
            }
        } catch (e: Exception) {
            println("Error al autenticar usuario: ${e.message}")
            e.printStackTrace()
            null
        }
    }

}