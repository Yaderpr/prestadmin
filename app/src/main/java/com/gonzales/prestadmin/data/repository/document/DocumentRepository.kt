// File: com/gonzales/prestadmin/data/repository/document/DocumentRepository.kt
package com.gonzales.prestadmin.data.repository.document

import com.gonzales.prestadmin.data.remote.SupabaseClient
import com.gonzales.prestadmin.domain.model.document.Document
import com.gonzales.prestadmin.domain.model.document.DocumentType
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class DocumentRepository(
    private val postgrest: Postgrest,
    private val storage: Storage
) {
    // Constructor secundario para usar los clientes globales
    constructor() : this(SupabaseClient.client.postgrest, SupabaseClient.client.storage)

    private val documentsTableName = "documents"

    /**
     * Sube un archivo a Supabase Storage y guarda sus metadatos en la tabla 'documents'.
     *
     * @param bucketName El nombre del bucket de Storage (ej. "dni_docs" o "profile-pictures").
     * @param storagePath La ruta completa dentro del bucket donde se guardará el archivo.
     * @param fileName El nombre del archivo a guardar.
     * @param fileBytes El contenido del archivo en formato ByteArray.
     * @param documentType El tipo de documento (DNI, PROFILE, etc.).
     * @param clientId El ID del cliente asociado (opcional).
     * @param userId El ID del usuario asociado (opcional).
     * @return El objeto Document guardado con el ID de la base de datos.
     * @throws Exception Si ocurre un error en la subida o en la inserción de metadatos.
     */
    @OptIn(ExperimentalTime::class)
    suspend fun saveDocument(
        bucketName: String,
        storagePath: String,
        fileName: String,
        fileBytes: ByteArray,
        documentType: DocumentType,
        clientId: Int? = null,
        userId: Int? = null
    ): Document {
        return try {
            // 1. Subir el archivo al bucket de Storage
            storage.from(bucketName).upload(storagePath, fileBytes)
            val fileUrl = storage.from(bucketName).publicUrl(storagePath)

            // 2. Guardar los metadatos en la tabla 'documents' de Postgrest
            val documentToSave = Document(
                clientId = clientId,
                userId = userId,
                filename = fileName,
                storageUrl = fileUrl,
                documentType = documentType,
                sizeBytes = fileBytes.size,
                uploadDate = Clock.System.now().toString()
            )

            val response = postgrest.from(documentsTableName).insert(documentToSave) {
                select()
            }
            response.decodeSingle<Document>()
        } catch (e: RestException) {
            println("Supabase Rest Error saving document: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error saving document: ${e.message}")
            throw e
        }
    }

    /**
     * Elimina un documento de Supabase Storage y su registro de la base de datos.
     *
     * @param bucketName El nombre del bucket donde se encuentra el archivo.
     * @param storagePath La ruta completa del archivo en el bucket.
     * @param documentId El ID del registro en la tabla 'documents' a eliminar.
     * @throws Exception Si ocurre un error durante la eliminación.
     */
    suspend fun deleteDocument(bucketName: String, storagePath: String, documentId: Int) {
        try {
            storage.from(bucketName).delete(storagePath)
            postgrest.from(documentsTableName).delete {
                filter { eq("id", documentId) }
            }
        } catch (e: RestException) {
            println("Supabase Rest Error deleting document: ${e.message}, Code: ${e.statusCode}")
            throw e
        } catch (e: Exception) {
            println("Error deleting document: ${e.message}")
            throw e
        }
    }

    /**
     * Obtiene un documento de un cliente/usuario por su tipo.
     * Se espera que haya solo uno o ninguno del mismo tipo (ej. 1 foto de perfil por usuario).
     *
     * @param clientId El ID del cliente (opcional).
     * @param userId El ID del usuario (opcional).
     * @param documentType El tipo de documento a buscar.
     * @return El objeto Document si se encuentra, o null.
     */
    suspend fun getDocument(clientId: Int? = null, userId: Int? = null, documentType: DocumentType): Document? {
        return try {
            val response = postgrest.from(documentsTableName).select {
                filter {
                    eq("document_type", documentType.name.lowercase())
                    clientId?.let { eq("client_id", it) }
                    userId?.let { eq("user_id", it) }
                }
                order("upload_date", Order.DESCENDING) // Traer el más reciente
                limit(1)
            }
            response.decodeSingleOrNull<Document>()
        } catch (e: Exception) {
            println("Error getting document: ${e.message}")
            null
        }
    }
}