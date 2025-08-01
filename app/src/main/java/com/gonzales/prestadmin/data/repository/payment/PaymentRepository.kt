package com.gonzales.prestadmin.data.repository.payment

// File: com/gonzales/prestadmin/data/repository/PaymentRepository.kt

import com.gonzales.prestadmin.data.remote.SupabaseClient
import com.gonzales.prestadmin.domain.model.payment.Payment
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class PaymentRepository(
    private val postgrest: Postgrest
) {

    // Constructor secundario para usar el SupabaseClient global
    constructor() : this(SupabaseClient.client.postgrest)

    private val tableName = "payments"

    /**
     * Guarda un nuevo pago en la base de datos de Supabase.
     *
     * @param payment El objeto Payment a guardar.
     * @return El objeto Payment guardado con el ID generado por la base de datos.
     * @throws Exception Si ocurre un error durante la inserción.
     */
    suspend fun savePayment(payment: Payment): Payment {
        return try {
            val response = postgrest.from(tableName).insert(payment) {
                select(Columns.ALL)
            }
            response.decodeSingle<Payment>()
        } catch (e: RestException) {
            println("Supabase Rest Error saving payment: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error saving payment: ${e.message}")
            throw e
        }
    }
    suspend fun deletePayment(paymentId: Int): Boolean {
        return try {
            val response = postgrest.from(tableName).delete {
                filter {
                    eq("payment_id", paymentId)
                }
            }
            // Supabase devuelve una lista de los objetos eliminados. Si está vacía, no se eliminó nada.
            response.data.isNotEmpty() // Si hay datos en la respuesta, significa que se eliminó
        } catch (e: RestException) {
            println("Supabase Rest Error deleting payment: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error deleting payment: ${e.message}")
            throw e
        }
    }

    /**
     * Obtiene una lista de pagos asociados a un préstamo específico.
     * Los pagos se devuelven ordenados por fecha de creación.
     *
     * @param loanId El ID del préstamo.
     * @return Una lista de objetos Payment.
     * @throws Exception Si ocurre un error durante la consulta.
     */
    suspend fun getPaymentsForLoan(loanId: Int): List<Payment> {
        return try {
            postgrest.from(tableName).select {
                filter {
                    eq("loan_id", loanId)
                }
                order("created_at", Order.ASCENDING)
            }.decodeList<Payment>()
        } catch (e: RestException) {
            println("Supabase Rest Error getting payments for loan: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error getting payments for loan: ${e.message}")
            throw e
        }
    }

    /**
     * Obtiene un pago por su ID.
     *
     * @param paymentId El ID del pago a buscar.
     * @return El objeto Payment si se encuentra, o null si no existe.
     * @throws Exception Si ocurre un error durante la consulta.
     */
    suspend fun getPaymentById(paymentId: Int): Payment? {
        return try {
            postgrest.from(tableName).select {
                filter {
                    eq("payment_id", paymentId)
                }
            }.decodeSingleOrNull<Payment>()
        } catch (e: RestException) {
            println("Supabase Rest Error getting payment by ID: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error getting payment by ID: ${e.message}")
            throw e
        }
    }
}