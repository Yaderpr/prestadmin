package com.gonzales.prestadmin.presentation.viewmodel.loan_payment

import androidx.lifecycle.ViewModel
import com.gonzales.prestadmin.domain.model.client.Client
import com.gonzales.prestadmin.domain.model.loan.Loan
import com.gonzales.prestadmin.domain.model.payment.Payment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


class PaymentViewModel : ViewModel() {

    // --- Campos de solo lectura (se llenarán al inicializar) ---
    private val _loanId = MutableStateFlow<Int?>(null)
    private val _clientName = MutableStateFlow("Cargando...")
    val clientName: StateFlow<String> = _clientName.asStateFlow()

    private val _loanCode = MutableStateFlow("Cargando...")
    val loanCode: StateFlow<String> = _loanCode.asStateFlow()

    @OptIn(ExperimentalTime::class)
    private val _paymentDate = MutableStateFlow(
        Clock.System.todayIn(TimeZone.currentSystemDefault())
    )
    val paymentDate: StateFlow<LocalDate> = _paymentDate.asStateFlow()

    private val _baseQuota = MutableStateFlow(0.0)
    val baseQuota: StateFlow<Double> = _baseQuota.asStateFlow()

    private val _amountPaidToday = MutableStateFlow(0.0)
    val amountPaidToday: StateFlow<Double> = _amountPaidToday.asStateFlow()

    // --- Campo editable ---
    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    // --- Errores ---
    private val _amountError = MutableStateFlow<String?>(null)
    val amountError: StateFlow<String?> = _amountError.asStateFlow()

    /**
     * Inicializa el ViewModel con los datos del préstamo, el cliente y la lista de pagos.
     * Calcula la suma de los pagos hechos hoy.
     */
    @OptIn(ExperimentalTime::class)
    fun initializeWithData(client: Client, loan: Loan, payments: List<Payment>) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        // Calcula el total pagado hoy sumando los montos de los pagos de la fecha actual
        val totalPaidToday = payments.filter { it.paymentDate == today }.sumOf { it.amount }

        _loanId.value = loan.id
        _clientName.value = "${client.firstName} ${client.lastName}"
        _loanCode.value = loan.id.toString()
        _baseQuota.value = loan.baseQuota
        _amountPaidToday.value = totalPaidToday // Asignación corregida
    }

    // --- Funciones para actualizar campos ---
    fun updateAmount(newAmount: String) {
        _amount.value = newAmount
        if (newAmount.isNotEmpty() && _amountError.value != null) {
            _amountError.value = null
        }
    }

    // --- Función para crear y devolver la instancia del modelo Payment ---
    fun getPayment(): Payment? {
        val amountValue = _amount.value.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            _amountError.value = "Monto no válido o vacío."
            return null
        } else {
            _amountError.value = null
            return Payment(
                loanId = _loanId.value!!,
                amount = amountValue,
                paymentDate = _paymentDate.value
            )
        }
    }

    /**
     * Limpia el campo de monto de la UI.
     */
    fun clearAmountInput() {
        _amount.value = ""
    }
}