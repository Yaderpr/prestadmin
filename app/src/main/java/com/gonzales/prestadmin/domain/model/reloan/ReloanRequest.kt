// File: com/gonzales/prestadmin/domain/model/payment/ReloanRequest.kt

package com.gonzales.prestadmin.domain.model.reloan

import java.time.LocalDate

data class ReloanRequest(
    val clientId: Int?, // ID del cliente al que se le otorga el represtamo. ¡Crucial!
    val capital: Double,
    val interestPercentage: Int,
    val modality: String, // Ej: "Diario", "Semanal", "Mensual"
    val installmentsTerm: String, // Ej: "20 cuotas a 1 mes", "4 semanas a 1 mes"
    val disbursementDate: LocalDate,
    val dayOfWeek: String?, // Día de la semana para cuotas semanales/mensuales (ej. "Lunes", "Martes")
    val firstInstallmentDate: LocalDate,
    val totalDebt: Double,
    val dueDate: LocalDate,
    val observation: String? // Campo opcional
)