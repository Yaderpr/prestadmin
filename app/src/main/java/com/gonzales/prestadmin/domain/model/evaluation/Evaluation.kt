package com.gonzales.prestadmin.domain.model.evaluation

// File: com/gonzales/prestadmin/domain/model/evaluation/Evaluation.kt Un paquete específico para el modelo de evaluación

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Evaluation(
    @SerialName("evaluation_id") val id: Int? = null, // ID autoincremental de la DB
    @SerialName("client_id") val clientId: Int, // FK al cliente, no puede ser nulo
    @SerialName("business_type") val businessType: String,
    @SerialName("business_address") val businessAddress: String,
    @SerialName("created_at") val createdAt: String? = null // La DB o el servidor puede generar este timestamp
)