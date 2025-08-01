package com.gonzales.prestadmin.presentation.viewmodel.loan_payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gonzales.prestadmin.App
import com.gonzales.prestadmin.data.repository.client.ClientRepository
import com.gonzales.prestadmin.data.repository.loan.LoanRepository
import com.gonzales.prestadmin.data.repository.payment.PaymentRepository
import com.gonzales.prestadmin.data.service.PaymentService
import com.gonzales.prestadmin.domain.model.client.Client
import com.gonzales.prestadmin.domain.model.loan.Loan
import com.gonzales.prestadmin.domain.model.payment.Payment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la operación de guardado
sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

// Clase de estado unificada para la pantalla
data class LoanPaymentUiState(
    val client: Client? = null,
    val loan: Loan? = null,
    val payments: List<Payment> = emptyList(),
    val isLoading: Boolean = false,
    val saveState: SaveState = SaveState.Idle
)

class LoanPaymentViewModel(
    private val clientRepository: ClientRepository = App.clientRepository,
    private val loanRepository: LoanRepository = App.loanRepository,
    private val paymentRepository: PaymentRepository = App.paymentRepository,
    private val paymentService: PaymentService = App.paymentService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoanPaymentUiState())
    val uiState: StateFlow<LoanPaymentUiState> = _uiState.asStateFlow()

    /**
     * Carga todos los datos del cliente y su préstamo principal.
     */
    fun loadInitialData(clientId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val client = clientRepository.getClientById(clientId)
                val loan = loanRepository.getLoansByClientId(clientId).firstOrNull()
                val payments = paymentRepository.getPaymentsForLoan(loan?.id ?: 1)
                if (client == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            saveState = SaveState.Error("Cliente no encontrado para id $clientId")
                        )
                    }
                    return@launch
                }

                if (loan == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            saveState = SaveState.Error("No se encontró préstamo para cliente $clientId")
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        client = client,
                        loan = loan,
                        payments = payments,
                        isLoading = false,
                        saveState = SaveState.Idle
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, saveState = SaveState.Error("Error al cargar: ${e.message}")) }
            }
        }
    }


    /**
     * Procesa un objeto Payment y lo guarda a través del servicio.
     * Esta función es llamada desde la UI después de que el PaymentSectionViewModel
     * ha validado los datos y creado la instancia del modelo.
     */
    fun processPayment(payment: Payment) {
        viewModelScope.launch {
            _uiState.update { it.copy(saveState = SaveState.Loading) }
            try {
                val success = paymentService.savePaymentAndUpdateLoan(payment)
                if (success) {
                    _uiState.update { it.copy(saveState = SaveState.Success) }
                } else {
                    _uiState.update { it.copy(saveState = SaveState.Error("Error al guardar el pago.")) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveState = SaveState.Error("Error de conexión: ${e.message}")) }
            }
        }
    }

    /**
     * Reinicia el estado de guardado, ideal para cuando se muestra un mensaje.
     */
    fun resetSaveState() {
        _uiState.update { it.copy(saveState = SaveState.Idle) }
    }
}