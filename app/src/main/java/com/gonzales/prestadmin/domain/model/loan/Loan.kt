// File: com/gonzales/prestadmin/domain/model/loan/Loan.kt
package com.gonzales.prestadmin.domain.model.loan

import kotlinx.datetime.LocalDate

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlin.time.ExperimentalTime

@Serializable
data class Loan(
    @SerialName("loan_id") val id: Int? = null,
    @SerialName("client_id") val clientId: Int,
    @SerialName("capital") val capital: Double,
    @SerialName("interest_percentage") val interestPercentage: Double,
    @SerialName("modality") val modality: String,
    @SerialName("installments_term") val installmentsTerm: String,
    @SerialName("disbursement_date") val disbursementDate: LocalDate,
    @SerialName("day_of_week") val dayOfWeek: String? = null,
    @SerialName("first_installment_date") val firstInstallmentDate: LocalDate,
    @SerialName("total_debt") val totalDebt: Double,
    @SerialName("base_quota") val baseQuota: Double,
    @SerialName("due_date") val dueDate: LocalDate,
    @SerialName("observation") val observation: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("paid_amount") val paidAmount: Double = 0.0
) {
    // Propiedad calculada para el saldo pendiente. No se guarda en la base de datos.
    val remainingBalance: Double
        get() = totalDebt - paidAmount
}