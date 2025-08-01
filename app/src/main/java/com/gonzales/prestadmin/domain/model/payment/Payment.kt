package com.gonzales.prestadmin.domain.model.payment


import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    @SerialName("payment_id") val id: Int? = null,
    @SerialName("loan_id") val loanId: Int,
    @SerialName("payment_date") val paymentDate: LocalDate,
    @SerialName("amount") val amount: Double,
    @SerialName("created_at") val createdAt: String? = null
)