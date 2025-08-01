package com.gonzales.prestadmin.domain.model.guarantee

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Guarantee(
    @SerialName("guarantee_id") val id: Int? = null, // ID autoincremental de la DB
    @SerialName("client_id") val clientId: Int, // FK al cliente, no puede ser nulo
    @SerialName("description") val description: String,
    @SerialName("estimated_price") val estimatedPrice: Double, // Mantengo como String para el ViewModel, la DB lo convertirá a numeric
    @SerialName("created_at") val createdAt: String? = null // La DB o el servidor puede generar este timestamp
)