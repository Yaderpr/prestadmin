package com.gonzales.prestadmin.presentation.viewmodel.client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gonzales.prestadmin.App
import com.gonzales.prestadmin.data.repository.client.ClientRepository
import com.gonzales.prestadmin.data.repository.loan.LoanRepository
import com.gonzales.prestadmin.domain.model.client.Client
import com.gonzales.prestadmin.domain.model.loan.Loan
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.todayIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@RequiresApi(Build.VERSION_CODES.O)
class LoanRequestViewModel(
    private val loanRepository: LoanRepository = App.loanRepository,
    private val clientRepository: ClientRepository = App.clientRepository
) : ViewModel() {

    // --- Estados para la selección de cliente ---
    private val _selectedClient = MutableStateFlow<Client?>(null)
    val selectedClient: StateFlow<Client?> = _selectedClient.asStateFlow()
    private val _clientSearchInput = MutableStateFlow("")
    val clientSearchInput: StateFlow<String> = _clientSearchInput.asStateFlow()
    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = _clients.asStateFlow()
    val clientesExpanded = MutableStateFlow(false)

    init {
        loadClients()
    }

    private fun loadClients() {
        viewModelScope.launch {
            try {
                _clients.value = clientRepository.getAllClients()
            } catch (e: Exception) {
                println("Error loading clients: ${e.message}")
            }
        }
    }

    // --- Métodos de actualización para la sección del cliente ---
    fun updateClientSearchInput(input: String) { _clientSearchInput.value = input }
    fun selectClient(client: Client) {
        _selectedClient.value = client
        _clientSearchInput.value = client.firstName + " " + client.lastName
    }
    fun updateClientesExpanded(expanded: Boolean) { clientesExpanded.value = expanded }


    // --- Estados para los inputs del préstamo ---
    val capitalText = MutableStateFlow("")
    val interestPct = MutableStateFlow("15") // Valor por defecto
    val term = MutableStateFlow("")
    val observation = MutableStateFlow("")

    val modalities = listOf("Diario", "Semanal", "Mensual")
    val modality = MutableStateFlow(modalities.first())
    val dayOfWeekEditable = MutableStateFlow("")

    val disbursementDate = MutableStateFlow(LocalDate.now())

    // --- Estados de la UI (expansión de menús, etc.) ---
    val modalityExpanded = MutableStateFlow(false)
    val termExpanded = MutableStateFlow(false)
    val dayOfWeekExpanded = MutableStateFlow(false)
    val showDatePicker = MutableStateFlow(false)

    // --- Estado para forzar la validación de todos los campos ---
    private val _forceValidation = MutableStateFlow(false)

    // --- Métodos de actualización para los inputs del préstamo ---
    fun updateCapitalText(input: String) { capitalText.value = input }
    fun updateInterestPct(input: String) { interestPct.value = input }
    fun updateModality(selectedModality: String) { modality.value = selectedModality }
    fun updateTerm(selectedTerm: String) { term.value = selectedTerm }
    fun updateObservation(input: String) { observation.value = input }
    fun updateDayOfWeekEditable(selectedDay: String) { dayOfWeekEditable.value = selectedDay }
    fun updateDisbursementDate(date: LocalDate) { disbursementDate.value = date }

    // --- Métodos de actualización para la UI ---
    fun updateModalityExpanded(expanded: Boolean) { modalityExpanded.value = expanded }
    fun updateTermExpanded(expanded: Boolean) { termExpanded.value = expanded }
    fun updateDayOfWeekExpanded(expanded: Boolean) { dayOfWeekExpanded.value = expanded }
    fun updateShowDatePicker(show: Boolean) { showDatePicker.value = show }

    // --- Lógica de validación reactiva (ahora con el flag de validación) ---
    val capitalError: StateFlow<String?> = combine(capitalText, _forceValidation) { capital, force ->
        if (!force && capital.isBlank()) null else validateCapital(capital)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val interestPctError: StateFlow<String?> = combine(interestPct, _forceValidation) { interest, force ->
        if (!force && interest.isBlank()) null else validateInterest(interest)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val termError: StateFlow<String?> = combine(term, _forceValidation) { term, force ->
        if (!force && term.isBlank()) null else validateTerm(term)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dayOfWeekError: StateFlow<String?> = combine(dayOfWeekEditable, modality, _forceValidation) { day, mod, force ->
        if (mod == "Diario") {
            null
        } else if (!force && day.isBlank()) {
            null
        } else {
            validateDayOfWeek(day)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Lógica de cálculo reactiva ---
    val totalDeudaCalculated: StateFlow<Double> = combine(capitalText, interestPct) { capital, interest ->
        val capitalVal = capital.toDoubleOrNull() ?: 0.0
        val interestVal = interest.toDoubleOrNull() ?: 0.0
        capitalVal * (1 + interestVal / 100.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cuotaCalculated: StateFlow<Double> = combine(totalDeudaCalculated, term) { total, termStr ->
        val cuotasCount = termStr.substringBefore(" ").toIntOrNull() ?: 0
        if (cuotasCount > 0) total / cuotasCount else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val primeraCuotaCalculated: StateFlow<LocalDate?> = combine(disbursementDate, modality, dayOfWeekEditable) { date, mod, day ->
        when (mod) {
            "Diario" -> date.plusDays(1)
            "Semanal", "Mensual" -> {
                // FIX: Lógica corregida para el cálculo por defecto.
                val nextDate = date.plusDays(1)

                if (day.isBlank()) {
                    // Si el usuario no ha seleccionado un día, calcular la primera cuota en el siguiente día de la semana.
                    return@combine nextDate
                } else {
                    // Si el usuario ha seleccionado un día, usarlo para calcular la primera cuota.
                    val dayOfWeekVal = DayOfWeek.of(
                        listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
                            .indexOf(day) + 1
                    )

                    if (dayOfWeekVal == null) return@combine null

                    var dateToReturn = nextDate
                    while(dateToReturn.dayOfWeek != dayOfWeekVal) {
                        dateToReturn = dateToReturn.plusDays(1)
                    }
                    return@combine dateToReturn
                }
            }
            else -> date.plusDays(1)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val diaSemanaDisplayCalculated: StateFlow<String?> = combine(dayOfWeekEditable, modality, primeraCuotaCalculated) { day, mod, firstInstallment ->
        when (mod) {
            "Diario" -> "Diario"
            "Semanal", "Mensual" -> day.ifEmpty {
                firstInstallment?.dayOfWeek?.getDisplayName(TextStyle.FULL, Locale.getDefault())
            }
            else -> null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val fechaVencimientoCalculated: StateFlow<LocalDate?> = combine(primeraCuotaCalculated, modality, term) { firstDate, mod, termStr ->
        if (firstDate == null) return@combine null

        val cuotasCount = termStr.substringBefore(" ").toIntOrNull() ?: 0
        when (mod) {
            "Diario" -> calculateBusinessDays(firstDate, cuotasCount)
            "Semanal" -> firstDate.plusWeeks((cuotasCount - 1).toLong())
            "Mensual" -> firstDate.plusMonths((cuotasCount - 1).toLong())
            else -> firstDate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    // --- Funciones de validación ---
    private fun validateCapital(capital: String): String? {
        val value = capital.toDoubleOrNull()
        return when {
            capital.isBlank() -> "El capital no puede estar vacío"
            value == null || value <= 0 -> "Debe ser un número positivo"
            else -> null
        }
    }

    private fun validateInterest(interest: String): String? {
        val value = interest.toDoubleOrNull()
        return when {
            interest.isBlank() -> "El interés no puede estar vacío"
            value == null || value < 0 -> "Debe ser un número no negativo"
            else -> null
        }
    }

    private fun validateTerm(term: String): String? {
        val value = term.substringBefore(" ").toIntOrNull()
        return when {
            term.isBlank() -> "El plazo no puede estar vacío"
            value == null || value <= 0 -> "Debe ser un número entero positivo"
            else -> null
        }
    }

    private fun validateDayOfWeek(day: String): String? {
        return if (day.isBlank()) "Debe seleccionar un día" else null
    }

    /**
     * Valida si todos los campos requeridos del préstamo son válidos.
     */
    fun validateAllFields(): Boolean {
        _forceValidation.value = true
        return capitalError.value == null &&
                interestPctError.value == null &&
                termError.value == null &&
                dayOfWeekError.value == null
    }

    @OptIn(ExperimentalTime::class)
    fun getLoan(): Loan {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        return Loan(
            clientId = 0,
            capital = capitalText.value.toDoubleOrNull() ?: 0.0,
            interestPercentage = interestPct.value.toDoubleOrNull() ?: 0.0,
            modality = modality.value,
            installmentsTerm = term.value,
            disbursementDate = disbursementDate.value.toKotlinLocalDate(),
            dayOfWeek = diaSemanaDisplayCalculated.value,
            firstInstallmentDate = primeraCuotaCalculated.value?.toKotlinLocalDate() ?: today,
            totalDebt = totalDeudaCalculated.value,
            dueDate = fechaVencimientoCalculated.value?.toKotlinLocalDate() ?: today,
            baseQuota = cuotaCalculated.value,
            observation = observation.value,
            paidAmount = 0.0,
            createdAt = today.toString()
        )
    }

    private fun calculateBusinessDays(startDate: LocalDate, cuotas: Int): LocalDate {
        var currentDate = startDate
        var remainingCuotas = cuotas - 1
        while (remainingCuotas > 0) {
            currentDate = currentDate.plusDays(1)
            if (currentDate.dayOfWeek != DayOfWeek.SATURDAY && currentDate.dayOfWeek != DayOfWeek.SUNDAY) {
                remainingCuotas--
            }
        }
        return currentDate
    }
}