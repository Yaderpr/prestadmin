package com.gonzales.prestadmin.data.repository.evaluation // Nuevo paquete para repositorios de evaluación

import com.gonzales.prestadmin.data.remote.SupabaseClient
import com.gonzales.prestadmin.domain.model.evaluation.Evaluation // Importa tu modelo Evaluation
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.postgrest

class EvaluationRepository(
    private val postgrest: Postgrest
) {

    // Constructor secundario para usar el SupabaseClient global
    constructor() : this(SupabaseClient.client.postgrest)

    private val tableName = "evaluations" // El nombre de tu tabla en Supabase

    /**
     * Guarda una nueva evaluación en la base de datos de Supabase.
     *
     * @param evaluation El objeto Evaluation a guardar.
     * @return El objeto Evaluation guardado con el ID generado por la base de datos.
     * @throws Exception Si ocurre un error durante la inserción.
     */
    suspend fun saveEvaluation(evaluation: Evaluation): Evaluation {
        return try {
            val response = postgrest.from(tableName).insert(evaluation) {
                select(Columns.ALL) // Queremos el objeto insertado de vuelta
            }
            response.decodeSingle<Evaluation>()
        } catch (e: RestException) {
            println("Supabase Rest Error saving evaluation: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error saving evaluation: ${e.message}")
            throw e
        }
    }

    /**
     * Obtiene una evaluación por su ID.
     *
     * @param evaluationId El ID de la evaluación a buscar.
     * @return El objeto Evaluation si se encuentra, o null si no existe.
     * @throws Exception Si ocurre un error durante la consulta.
     */
    suspend fun getEvaluationById(evaluationId: Int): Evaluation? {
        return try {
            val response = postgrest.from(tableName).select {
                filter {
                    eq("evaluation_id", evaluationId)
                }
            }
            response.decodeSingleOrNull<Evaluation>()
        } catch (e: RestException) {
            println("Supabase Rest Error getting evaluation by ID: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error getting evaluation by ID: ${e.message}")
            throw e
        }
    }

    /**
     * Obtiene la evaluación asociada a un cliente específico.
     * Se espera que haya una o ninguna evaluación por cliente.
     *
     * @param clientId El ID del cliente.
     * @return El objeto Evaluation si se encuentra, o null si no existe.
     * @throws Exception Si ocurre un error durante la consulta.
     */
    suspend fun getEvaluationByClientId(clientId: Int): Evaluation? {
        return try {
            val response = postgrest.from(tableName).select {
                filter {
                    eq("client_id", clientId)
                }
            }
            response.decodeSingleOrNull<Evaluation>()
        } catch (e: RestException) {
            println("Supabase Rest Error getting evaluation by client ID: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error getting evaluation by client ID: ${e.message}")
            throw e
        }
    }

    /**
     * Actualiza una evaluación existente en la base de datos.
     * La evaluación debe tener un ID válido.
     *
     * @param evaluation El objeto Evaluation con los datos actualizados y un ID válido.
     * @return El objeto Evaluation actualizado.
     * @throws IllegalArgumentException Si el ID de la evaluación es nulo.
     * @throws Exception Si ocurre un error durante la actualización.
     */
    suspend fun updateEvaluation(evaluation: Evaluation): Evaluation {
        val evaluationId = evaluation.id ?: throw IllegalArgumentException("Evaluation ID cannot be null for update operation.")
        return try {
            val response = postgrest.from(tableName).update(evaluation) {
                filter {
                    eq("evaluation_id", evaluationId)
                }
                select(Columns.ALL) // Queremos el objeto actualizado de vuelta
            }
            response.decodeSingle<Evaluation>()
        } catch (e: RestException) {
            println("Supabase Rest Error updating evaluation: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error updating evaluation: ${e.message}")
            throw e
        }
    }

    /**
     * Elimina una evaluación de la base de datos por su ID.
     *
     * @param evaluationId El ID de la evaluación a eliminar.
     * @return true si la evaluación fue eliminada exitosamente, false si no se encontró.
     * @throws Exception Si ocurre un error durante la eliminación.
     */
    suspend fun deleteEvaluation(evaluationId: Int): Boolean {
        return try {
            val response = postgrest.from(tableName).delete {
                filter {
                    eq("evaluation_id", evaluationId)
                }
            }
            response.data.isNotEmpty() // Si hay datos en la respuesta, significa que se eliminó
        } catch (e: RestException) {
            println("Supabase Rest Error deleting evaluation: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error deleting evaluation: ${e.message}")
            throw e
        }
    }
}