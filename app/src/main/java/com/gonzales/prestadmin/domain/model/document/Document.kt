package com.gonzales.prestadmin.domain.model.user.document
import java.time.LocalDateTime

data class Document(
    val id: Int,
    val clientId: Int?, // FK a ClientDetails.id (puede ser nulo si es una foto de usuario)
    val userId: Int?, // FK a User.id_usuario (puede ser nulo si es una foto de cliente)
    val fileName: String,
    val storageUrl: String,
    val documentType: String, // Ej: "CEDULA_FRONTAL", "FOTO_PERFIL_USUARIO", "COMPROBANTE_DOMICILIO", etc.
    val mimeType: String?,
    val sizeBytes: Long,
    val uploadDate: LocalDateTime
)