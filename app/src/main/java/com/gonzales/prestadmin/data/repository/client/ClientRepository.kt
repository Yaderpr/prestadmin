package com.gonzales.prestadmin.data.repository.client

import com.gonzales.prestadmin.data.remote.SupabaseClient
import com.gonzales.prestadmin.domain.model.client.Client
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class ClientRepository(
    private val postgrest: Postgrest // Inyecta la instancia de Postgrest
) {

    // Constructor secundario para usar el SupabaseClient global
    constructor() : this(SupabaseClient.client.postgrest)

    private val tableName = "clients" // El nombre de tu tabla en Supabase

    /**
     * Guarda un nuevo cliente en la base de datos de Supabase.
     * Si el cliente ya tiene un ID, se consideraría una actualización (aunque saveClient está diseñado para inserciones aquí).
     *
     * @param client El objeto Client a guardar.
     * @return El objeto Client guardado con el ID generado por la base de datos.
     * @throws Exception Si ocurre un error durante la inserción.
     */
    suspend fun saveClient(client: Client): Client {
        return try {
            val response = postgrest.from(tableName).insert(client) {
                // Especifica que queremos el objeto insertado de vuelta
                select(Columns.ALL)
            }
            // Supabase devuelve una lista de objetos insertados. Tomamos el primero.
            response.decodeSingle<Client>()
        } catch (e: RestException) {
            // Error específico de Supabase REST
            println("Supabase Rest Error saving client: ${e.message}, Code: ${e.statusCode}")
            throw e // Re-lanza la excepción o envuelve en una más específica de dominio
        } catch (e: Exception) {
            // Otros errores
            println("Error saving client: ${e.message}")
            throw e
        }
    }

    /**
     * Obtiene un cliente por su ID.
     *
     * @param clientId El ID del cliente a buscar.
     * @return El objeto Client si se encuentra, o null si no existe.
     * @throws Exception Si ocurre un error durante la consulta.
     */
    suspend fun getClientById(clientId: Int): Client? {
        return try {
            val response = postgrest.from(tableName).select {
                filter {
                    eq("client_id", clientId) // Filtrar por la columna client_id
                }
            }
            // decodeSingleOrNull devuelve null si no hay resultados, o el objeto si hay uno
            response.decodeSingleOrNull<Client>()
        } catch (e: RestException) {
            println("Supabase Rest Error getting client by ID: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error getting client by ID: ${e.message}")
            throw e
        }
    }

    /**
     * Actualiza un cliente existente en la base de datos.
     * El cliente debe tener un ID válido.
     *
     * @param client El objeto Client con los datos actualizados y un ID válido.
     * @return El objeto Client actualizado.
     * @throws IllegalArgumentException Si el ID del cliente es nulo.
     * @throws Exception Si ocurre un error durante la actualización.
     */
    suspend fun updateClient(client: Client): Client {
        val clientId = client.id ?: throw IllegalArgumentException("Client ID cannot be null for update operation.")
        return try {
            val response = postgrest.from(tableName).update(client) {
                filter {
                    eq("client_id", clientId)
                }
                select(Columns.ALL) // Queremos el objeto actualizado de vuelta
            }
            response.decodeSingle<Client>()
        } catch (e: RestException) {
            println("Supabase Rest Error updating client: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error updating client: ${e.message}")
            throw e
        }
    }

    /**
     * Elimina un cliente de la base de datos por su ID.
     *
     * @param clientId El ID del cliente a eliminar.
     * @return true si el cliente fue eliminado exitosamente, false si no se encontró.
     * @throws Exception Si ocurre un error durante la eliminación.
     */
    suspend fun deleteClient(clientId: Int): Boolean {
        return try {
            val response = postgrest.from(tableName).delete {
                filter {
                    eq("client_id", clientId)
                }
            }
            // Supabase devuelve una lista de los objetos eliminados. Si está vacía, no se eliminó nada.
            response.data.isNotEmpty() // Si hay datos en la respuesta, significa que se eliminó
        } catch (e: RestException) {
            println("Supabase Rest Error deleting client: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error deleting client: ${e.message}")
            throw e
        }
    }
    suspend fun getAllClients(): List<Client> {
        return postgrest.from(tableName).select()
            .decodeList<Client>()
    }
}