package com.gonzales.prestadmin.domain.model.document
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Document(
    val id: Int? = null, // Autoincremental, la DB lo genera
    @SerialName("client_id") val clientId: Int? = null, // Puede ser nulo
    @SerialName("user_id") val userId: Int? = null, // Puede ser nulo (FK), pero necesario para vincular
    val filename: String,
    @SerialName("storage_url") val storageUrl: String,
    @SerialName("document_type") val documentType: DocumentType? = null, // Puede ser nulo
    @SerialName("size_bytes") val sizeBytes: Int,
    @SerialName("upload_date") val uploadDate: String // TIMESTAMP puede ser String ISO 8601 o Instant
)
@Serializable
enum class DocumentType {
    @SerialName("dni") DNI,
    @SerialName("profile") PROFILE
}