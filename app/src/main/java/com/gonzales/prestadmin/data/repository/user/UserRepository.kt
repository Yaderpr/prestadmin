// domain/repository/user/UserRepository.kt
package com.gonzales.prestadmin.data.repository.user

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.gonzales.prestadmin.App
import com.gonzales.prestadmin.data.local.datastore.SessionManager
import com.gonzales.prestadmin.data.remote.SupabaseClient
import com.gonzales.prestadmin.data.repository.document.DocumentRepository
import com.gonzales.prestadmin.domain.model.document.Document
import com.gonzales.prestadmin.domain.model.document.DocumentType
import com.gonzales.prestadmin.domain.model.user.User
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mindrot.jbcrypt.BCrypt
import java.io.IOException

class UserRepository(private val context: Context) {
    private val sessionManager: SessionManager = App.sessionManager
    private val documentRepository: DocumentRepository = App.documentRepository
    data class UserUiModel(
        val userId: String,
        val fullName: String,
        val username: String,
        val role: String,
        val photoUrl: String? = null
    )

    // ¡Aquí está el cambio clave!
    // userFlow ahora usa flatMapLatest para enriquecer con la photoUrl dentro del propio repositorio
    @OptIn(ExperimentalCoroutinesApi::class)
    val userFlow: Flow<UserUiModel> = sessionManager.sessionFlow.flatMapLatest { sessionData ->
        // Crear un UserUiModel base con los datos de la sesión
        val baseUserUiModel = UserUiModel(
            userId = sessionData.userId ?: "",
            fullName = sessionData.fullName ?: "Usuario desconocido",
            username = sessionData.username ?: "Usuario desconocido",
            role = sessionData.role ?: "Desconocido",
            photoUrl = sessionData.photoUrl // Si el SessionManager ya tiene la URL, úsala
        )

        // Si el userId no está vacío Y el SessionManager no proporcionó la photoUrl,
        // intentamos obtenerla de Supabase.
        if (baseUserUiModel.userId.isNotEmpty() && baseUserUiModel.photoUrl.isNullOrEmpty()) {
            flow {
                val fetchedPhotoUrl = getProfilePictureUrl(baseUserUiModel.userId)
                emit(baseUserUiModel.copy(photoUrl = fetchedPhotoUrl))
            }
        } else {
            // Si no hay userId, o si el SessionManager ya tiene la photoUrl,
            // simplemente emitimos el modelo base.
            flow { emit(baseUserUiModel) }
        }
    }

    suspend fun authenticateUser(username: String, passwordPlain: String): User? {
        // ... (Tu código actual para autenticación) ...
        return try {
            val user = SupabaseClient.client.from("users")
                .select {
                    filter {
                        eq("username", username)
                    }
                }
                .decodeSingleOrNull<User>()

            if (user == null) {
                println("Fallo en la autenticación para el usuario '$username'. Usuario no encontrado.")
                return null
            }

            val storedHash = user.passwordHash
            val passwordIsValid = BCrypt.checkpw(passwordPlain, storedHash)

            if (passwordIsValid) {
                // Modificación aquí: Si tu 'user' tiene un campo para la URL de la foto,
                // podrías intentar obtenerla aquí y pasarla a saveSession,
                // O dejar que el userFlow de arriba la resuelva.
                sessionManager.saveSession(user.user_id.toString(),
                    user.username as String,
                    (user.firstname + " " + user.lastname),
                    user.role.toString(),
                    // Si el usuario tiene una photoUrl al inicio de sesión, pásala aquí
                    // user.photoUrl // <-- Asumiendo que User tiene un campo photoUrl
                )
                println("Usuario '$username' autenticado con éxito y sesión guardada.")
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

    suspend fun clearUserSession() {
        sessionManager.clearSession()
    }

    // Estas propiedades pueden mantenerse si se usan directamente, pero userFlow es el principal ahora
    val userSession: Flow<SessionManager.SessionData>
        get() = sessionManager.sessionFlow

    val isLoggedIn: Flow<Boolean>
        get() = userSession.map { sessionData ->
            sessionData.userId != null
        }

    suspend fun getProfilePictureUrl(userId: String): String? {
        return try {
            val document = SupabaseClient.client.from("documents")
                .select {
                    filter {
                        eq("user_id", userId.toInt())
                        eq("document_type", "profile")
                    }
                    order("upload_date", order = Order.DESCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<Document>()

            document?.storageUrl
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateProfilePicture(
        userId: Int, // Cambiado a Int para que coincida con el modelo Document
        imageUri: Uri
    ): Result<Unit> {
        val bucketName = "profile-pictures" // Nombre del bucket para las fotos de perfil

        return try {
            // Usamos el repositorio para encontrar el documento existente de forma limpia
            val existingProfileDoc = documentRepository.getDocument(
                userId = userId,
                documentType = DocumentType.PROFILE
            )

            existingProfileDoc?.let { doc ->
                try {
                    // Obtenemos la ruta del archivo del URL para poder borrarlo
                    val storagePath = doc.storageUrl.substringAfterLast("$bucketName/")

                    // Usamos el repositorio para eliminar el documento y el registro de la DB en un solo paso
                    documentRepository.deleteDocument(
                        bucketName = bucketName,
                        storagePath = storagePath,
                        documentId = doc.id!!
                    )
                    println("Foto de perfil antigua eliminada de Storage y DB.")
                } catch (e: Exception) {
                    println("Advertencia: No se pudo eliminar la foto de perfil antigua: ${e.message}")
                    e.printStackTrace()
                }
            }

            val imageBytes = context.contentResolver.openInputStream(imageUri)?.use {
                it.readBytes()
            } ?: throw IOException("No se pudo leer la imagen desde la Uri proporcionada.")

            // Creamos un nombre de archivo único
            val filename = "profile_pic_${userId}_${System.currentTimeMillis()}.jpg"
            val storagePath = "profiles/$userId/$filename"

            // Usamos el repositorio para guardar el nuevo documento
            val newDocument = documentRepository.saveDocument(
                bucketName = bucketName,
                storagePath = storagePath,
                fileName = filename,
                fileBytes = imageBytes,
                documentType = DocumentType.PROFILE,
                userId = userId
            )

            print("Storage URL: ${newDocument.storageUrl}")
            sessionManager.updateUserPhoto(newDocument.storageUrl) // Actualiza el sessionManager

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}