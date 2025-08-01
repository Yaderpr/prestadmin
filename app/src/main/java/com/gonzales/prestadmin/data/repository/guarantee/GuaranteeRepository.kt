package com.gonzales.prestadmin.data.repository.guarantee

import com.gonzales.prestadmin.data.remote.SupabaseClient
import com.gonzales.prestadmin.domain.model.guarantee.Guarantee
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class GuaranteeRepository(
    private val postgrest: Postgrest
) {

    // Constructor secundario para usar el SupabaseClient global
    constructor() : this(SupabaseClient.client.postgrest)

    private val tableName = "guarantees" // El nombre de tu tabla en Supabase

    /**
     * Guarda una nueva garantía en la base de datos de Supabase.
     *
     * @param guarantee El objeto Guarantee a guardar.
     * @return El objeto Guarantee guardado con el ID generado por la base de datos.
     * @throws Exception Si ocurre un error durante la inserción.
     */
    suspend fun saveGuarantee(guarantee: Guarantee): Guarantee {
        return try {
            // Nota: 'estimatedPrice' es String en el modelo. Aquí se asume que Supabase Postgrest
            // puede manejar la conversión automática a NUMERIC si el valor es numérico.
            // Si hay problemas, se necesitará convertir a Double antes de enviarlo.
            // Ejemplo: val guaranteeToSend = guarantee.copy(estimatedPrice = guarantee.estimatedPrice.toDouble().toString())

            val response = postgrest.from(tableName).insert(guarantee) {
                select(Columns.ALL) // Queremos el objeto insertado de vuelta
            }
            response.decodeSingle<Guarantee>()
        } catch (e: RestException) {
            println("Supabase Rest Error saving guarantee: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error saving guarantee: ${e.message}")
            throw e
        }
    }

    /**
     * Obtiene una garantía por su ID.
     *
     * @param guaranteeId El ID de la garantía a buscar.
     * @return El objeto Guarantee si se encuentra, o null si no existe.
     * @throws Exception Si ocurre un error durante la consulta.
     */
    suspend fun getGuaranteeById(guaranteeId: Int): Guarantee? {
        return try {
            val response = postgrest.from(tableName).select {
                filter {
                    eq("guarantee_id", guaranteeId)
                }
            }
            response.decodeSingleOrNull<Guarantee>()
        } catch (e: RestException) {
            println("Supabase Rest Error getting guarantee by ID: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error getting guarantee by ID: ${e.message}")
            throw e
        }
    }

    /**
     * Obtiene una lista de garantías asociadas a un cliente específico.
     * Esto es crucial para la funcionalidad de múltiples garantías por cliente.
     *
     * @param clientId El ID del cliente.
     * @return Una lista de objetos Guarantee.
     * @throws Exception Si ocurre un error durante la consulta.
     */
    suspend fun getGuaranteesByClientId(clientId: Int): List<Guarantee> {
        return try {
            val response = postgrest.from(tableName).select {
                filter {
                    eq("client_id", clientId)
                }
            }
            response.decodeList<Guarantee>()
        } catch (e: RestException) {
            println("Supabase Rest Error getting guarantees by client ID: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error getting guarantees by client ID: ${e.message}")
            throw e
        }
    }

    /**
     * Actualiza una garantía existente en la base de datos.
     * La garantía debe tener un ID válido.
     *
     * @param guarantee El objeto Guarantee con los datos actualizados y un ID válido.
     * @return El objeto Guarantee actualizado.
     * @throws IllegalArgumentException Si el ID de la garantía es nulo.
     * @throws Exception Si ocurre un error durante la actualización.
     */
    suspend fun updateGuarantee(guarantee: Guarantee): Guarantee {
        val guaranteeId = guarantee.id
        return try {
            val response = postgrest.from(tableName).update(guarantee) {
                filter {
                    eq("guarantee_id", guaranteeId as Any)
                }
                select(Columns.ALL) // Queremos el objeto actualizado de vuelta
            }
            response.decodeSingle<Guarantee>()
        } catch (e: RestException) {
            println("Supabase Rest Error updating guarantee: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error updating guarantee: ${e.message}")
            throw e
        }
    }

    /**
     * Elimina una garantía de la base de datos por su ID.
     *
     * @param guaranteeId El ID de la garantía a eliminar.
     * @return true si la garantía fue eliminada exitosamente, false si no se encontró.
     * @throws Exception Si ocurre un error durante la eliminación.
     */
    suspend fun deleteGuarantee(guaranteeId: Int): Boolean {
        return try {
            val response = postgrest.from(tableName).delete {
                filter {
                    eq("guarantee_id", guaranteeId)
                }
            }
            response.data.isNotEmpty() // Si hay datos en la respuesta, significa que se eliminó
        } catch (e: RestException) {
            println("Supabase Rest Error deleting guarantee: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error deleting guarantee: ${e.message}")
            throw e
        }
    }
}