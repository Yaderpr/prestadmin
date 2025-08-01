package com.gonzales.prestadmin.data.service

import android.graphics.Bitmap
import com.gonzales.prestadmin.App
import com.gonzales.prestadmin.data.repository.client.ClientRepository
import com.gonzales.prestadmin.data.repository.document.DocumentRepository
import com.gonzales.prestadmin.data.repository.evaluation.EvaluationRepository
import com.gonzales.prestadmin.data.repository.guarantee.GuaranteeRepository
import com.gonzales.prestadmin.data.repository.loan.LoanRepository
import com.gonzales.prestadmin.domain.model.client.Client
import com.gonzales.prestadmin.domain.model.document.DocumentType
import com.gonzales.prestadmin.domain.model.evaluation.Evaluation
import com.gonzales.prestadmin.domain.model.guarantee.Guarantee
import com.gonzales.prestadmin.domain.model.loan.Loan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class ClientFormService(
    private val clientRepository: ClientRepository = App.clientRepository,
    private val loanRepository: LoanRepository = App.loanRepository,
    private val guaranteeRepository: GuaranteeRepository = App.guaranteeRepository,
    private val evaluationRepository: EvaluationRepository = App.evaluationRepository,
    private val documentRepository: DocumentRepository = App.documentRepository
) {

    suspend fun saveClientForm(formData: ClientFormData): Client? {
        return withContext(Dispatchers.IO) {
            var savedClient: Client? = null
            var savedLoan: Loan? = null
            var savedEvaluation: Evaluation? = null
            var savedGuarantees: List<Guarantee>? = null

            try {
                // 1. Guardar el cliente principal (obligatorio)
                savedClient = clientRepository.saveClient(formData.client)
                val clientId = savedClient.id ?: throw IllegalStateException("Client ID not returned from DB.")

                // 2. Guardar el préstamo (obligatorio)
                val loanToSave = formData.loan.copy(clientId = clientId)
                savedLoan = loanRepository.saveLoan(loanToSave)

                // 3. Guardar la evaluación (opcional)
                if (formData.evaluation != null) {
                    val evaluationToSave = formData.evaluation.copy(clientId = clientId)
                    savedEvaluation = evaluationRepository.saveEvaluation(evaluationToSave)
                }

                // 4. Guardar las garantías (opcionales)
                if (formData.guarantees?.isNotEmpty() == true) {
                    val guaranteesToSave = formData.guarantees.map { it.copy(clientId = clientId) }
                    savedGuarantees = guaranteesToSave.map { guaranteeRepository.saveGuarantee(it) }
                }

                // 5. Subir y guardar los metadatos de las fotos del DNI (opcional)
                val bucketName = "dni-pictures"

                formData.dniFrontImage?.let { bitmap ->
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                    val dniFrontalName = "dni_frontal_${clientId}.jpg"
                    documentRepository.saveDocument(
                        bucketName = bucketName,
                        storagePath = "clients/$clientId/$dniFrontalName",
                        fileName = dniFrontalName,
                        fileBytes = stream.toByteArray(),
                        documentType = DocumentType.DNI,
                        clientId = clientId
                    )
                }

                formData.dniBackImage?.let { bitmap ->
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                    val dniReversaName = "dni_reverso_${clientId}.jpg"
                    documentRepository.saveDocument(
                        bucketName = bucketName,
                        storagePath = "clients/$clientId/$dniReversaName",
                        fileName = dniReversaName,
                        fileBytes = stream.toByteArray(),
                        documentType = DocumentType.DNI,
                        clientId = clientId
                    )
                }

                // Si todo fue exitoso, retornamos el cliente guardado
                savedClient

            } catch (e: Exception) {
                // Lógica de rollback si falla algo
                println("Error saving client form. Initiating rollback...")
                e.printStackTrace()

                // Intentar revertir los cambios en el orden inverso
                savedGuarantees?.forEach { guarantee ->
                    guarantee.id.let { id ->
                        try { guaranteeRepository.deleteGuarantee(id as Int) } catch (err: Exception) { println("Failed to rollback guarantee: $id") }
                    }
                }
                savedEvaluation?.id?.let { id ->
                    try { evaluationRepository.deleteEvaluation(id) } catch (err: Exception) { println("Failed to rollback evaluation: $id") }
                }
                savedLoan?.id?.let { id ->
                    try { loanRepository.deleteLoan(id) } catch (err: Exception) { println("Failed to rollback loan: $id") }
                }
                savedClient?.id?.let { id ->
                    try { clientRepository.deleteClient(id) } catch (err: Exception) { println("Failed to rollback client: $id") }
                }

                // Nota: El rollback de los documentos podría ser más complejo, ya que se habrían subido al Storage.
                // Se podría implementar una función de borrado de archivos por client_id en DocumentRepository

                null // Retornar null para indicar que la operación falló
            }
        }
    }
}