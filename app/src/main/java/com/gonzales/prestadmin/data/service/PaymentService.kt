package com.gonzales.prestadmin.data.service

import com.gonzales.prestadmin.App
import com.gonzales.prestadmin.data.repository.loan.LoanRepository
import com.gonzales.prestadmin.data.repository.payment.PaymentRepository
import com.gonzales.prestadmin.domain.model.payment.Payment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentService(
    private val paymentRepository: PaymentRepository = App.paymentRepository,
    private val loanRepository: LoanRepository = App.loanRepository
) {
    suspend fun savePaymentAndUpdateLoan(payment: Payment): Boolean {
        return withContext(Dispatchers.IO) {
            var savedPaymentId: Int? = null

            try {
                val loan = loanRepository.getLoanById(payment.loanId)
                    ?: throw IllegalStateException("Préstamo con ID ${payment.loanId} no encontrado.")

                val savedPayment = paymentRepository.savePayment(payment)
                savedPaymentId = savedPayment.id

                val newPaidAmount = loan.paidAmount + payment.amount

                // ¡Cambio aquí! Ahora solo actualizamos el "paid_amount"
                loanRepository.updateLoanPaidAmount(
                    loanId = loan.id as Int,
                    newPaidAmount = newPaidAmount
                )

                true

            } catch (e: Exception) {
                println("Error al guardar el pago y actualizar el préstamo. Iniciando rollback...")
                e.printStackTrace()

                savedPaymentId?.let { id ->
                    try {
                        paymentRepository.deletePayment(id)
                        println("Rollback exitoso: Pago con ID $id eliminado.")
                    } catch (rollbackError: Exception) {
                        println("Error en el rollback: No se pudo eliminar el pago con ID $id. Error: ${rollbackError.message}")
                    }
                }
                false
            }
        }
    }
}