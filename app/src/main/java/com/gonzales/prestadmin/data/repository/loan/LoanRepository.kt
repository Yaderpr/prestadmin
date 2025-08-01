package com.gonzales.prestadmin.data.repository.loan

import com.gonzales.prestadmin.data.remote.SupabaseClient
import com.gonzales.prestadmin.domain.model.loan.Loan
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class LoanRepository(
    private val postgrest: Postgrest
) {

    constructor() : this(SupabaseClient.client.postgrest)

    private val tableName = "loans"

    suspend fun saveLoan(loan: Loan): Loan {
        return try {
            val response = postgrest.from(tableName).insert(loan) {
                select(Columns.ALL)
            }
            response.decodeSingle<Loan>()
        } catch (e: RestException) {
            println("Supabase Rest Error saving loan: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error saving loan: ${e.message}")
            throw e
        }
    }

    suspend fun getLoanById(loanId: Int): Loan? {
        return try {
            val response = postgrest.from(tableName).select {
                filter {
                    eq("loan_id", loanId)
                }
            }
            response.decodeSingleOrNull<Loan>()
        } catch (e: RestException) {
            println("Supabase Rest Error getting loan by ID: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error getting loan by ID: ${e.message}")
            throw e
        }
    }

    suspend fun getLoansByClientId(clientId: Int): List<Loan> {
        return try {
            val response = postgrest.from(tableName).select {
                filter {
                    eq("client_id", clientId)
                }
            }
            response.decodeList<Loan>()
        } catch (e: RestException) {
            println("Supabase Rest Error getting loans by client ID: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error getting loans by client ID: ${e.message}")
            throw e
        }
    }

    suspend fun updateLoan(loan: Loan): Loan {
        val loanId = loan.id ?: throw IllegalArgumentException("Loan ID cannot be null for update operation.")
        return try {
            val response = postgrest.from(tableName).update(loan) {
                filter {
                    eq("loan_id", loanId)
                }
                select(Columns.ALL)
            }
            response.decodeSingle<Loan>()
        } catch (e: RestException) {
            println("Supabase Rest Error updating loan: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error updating loan: ${e.message}")
            throw e
        }
    }

    suspend fun deleteLoan(loanId: Int): Boolean {
        return try {
            val response = postgrest.from(tableName).delete {
                filter {
                    eq("loan_id", loanId)
                }
            }
            response.data.isNotEmpty()
        } catch (e: RestException) {
            println("Supabase Rest Error deleting loan: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error deleting loan: ${e.message}")
            throw e
        }
    }

    // --- FUNCIONALIDAD AÑADIDA PARA SOLUCIONAR EL ERROR ---

    /**
     * Actualiza solo el monto pagado de un préstamo, evitando el error de "GENERATED ALWAYS".
     *
     * @param loanId El ID del préstamo a actualizar.
     * @param newPaidAmount El nuevo monto total pagado.
     */
    suspend fun updateLoanPaidAmount(loanId: Int, newPaidAmount: Double) {
        // Objeto que solo contiene el campo a actualizar
        val updateData = LoanPaidAmountDto(
            paidAmount = newPaidAmount
        )
        try {
            postgrest.from(tableName).update(updateData) {
                filter {
                    eq("loan_id", loanId)
                }
            }
        } catch (e: RestException) {
            println("Supabase Rest Error updating loan paid amount: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error updating loan paid amount: ${e.message}")
            throw e
        }
    }
    suspend fun getAllLoans(): List<Loan> {
        return try {
            postgrest.from(tableName).select()
                .decodeList<Loan>()
        } catch (e: RestException) {
            println("Supabase Rest Error getting all loans: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error getting all loans: ${e.message}")
            throw e
        }
    }
}

/**
 * DTO para la actualización del monto pagado del préstamo.
 * Corresponde al campo "paid_amount" en la base de datos.
 */
@Serializable
data class LoanPaidAmountDto(
    @SerialName("paid_amount") val paidAmount: Double
)