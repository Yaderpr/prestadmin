package com.gonzales.prestadmin.domain.model.user
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.sql.Timestamp

@Serializable
enum class UserRole {
    @SerialName("admin") ADMIN, // Mapea "admin" de la DB a ADMIN en Kotlin
    @SerialName("collector") COLLECTOR
}

@Serializable
enum class UserState {
    @SerialName("enabled") ENABLED,
    @SerialName("disabled") DISABLED
}

@Serializable
data class User(
    // user_id es autoincremental, la DB lo genera. Es Int? para lecturas.
    val user_id: Int? = null,
    val username: String? = null, // VARCHAR sin NOT NULL puede ser null
    val firstname: String,
    val lastname: String? = null, // VARCHAR sin NOT NULL puede ser null
    @SerialName("password_hash") val passwordHash: String, // Coincide con el nombre de la columna en la DB
    val role: UserRole, // Usa el enum class que creaste
    @SerialName("state_of_user") val stateOfUser: UserState, // Usa el enum class que creaste
    val timestamp: String // PostgreSQL TIMESTAMP puede ser una String ISO 8601 o Instant
    // NOTA: Para 'timestamp', si quieres usar kotlinx.datetime.Instant, necesitas el módulo kotlinx-datetime
    // val timestamp: Instant // Ejemplo: import kotlinx.datetime.Instant
)
