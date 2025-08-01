package com.gonzales.prestadmin.presentation.viewmodel.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gonzales.prestadmin.App
import com.gonzales.prestadmin.data.repository.evaluation.EvaluationRepository
import com.gonzales.prestadmin.domain.model.evaluation.Evaluation
// Asumiendo este modelo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class EvaluationViewModel(evaluationRepository: EvaluationRepository = App.evaluationRepository) : ViewModel() {

    // --- Estados para los campos de la evaluación del negocio ---
    val businessType = MutableStateFlow("")
    val businessAddress = MutableStateFlow("")

    // --- Estado para controlar si la sección está activa/habilitada ---
    val isSectionActive = MutableStateFlow(true) // Inicia activa por defecto

    // --- Estado para forzar la validación de todos los campos ---
    private val _forceValidation = MutableStateFlow(false)

    // --- Estados para errores de validación, usando el patrón 'combine' ---
    val businessTypeError: StateFlow<String?> = combine(
        businessType,
        isSectionActive,
        _forceValidation
    ) { type, isActive, force ->
        if (!isActive || (!force && type.isBlank())) {
            // Si la sección no está activa o no se ha forzado la validación, no hay error.
            return@combine null
        }
        validateBusinessType(type)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val businessAddressError: StateFlow<String?> = combine(
        businessAddress,
        isSectionActive,
        _forceValidation
    ) { address, isActive, force ->
        if (!isActive || (!force && address.isBlank())) {
            return@combine null
        }
        validateBusinessAddress(address)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    // --- Funciones de actualización de estados ---
    fun updateBusinessType(newType: String) {
        businessType.value = newType
    }

    fun updateBusinessAddress(newAddress: String) {
        businessAddress.value = newAddress
    }

    /**
     * Alterna el estado de la sección. Si se desactiva, limpia los datos.
     */
    fun toggleSectionActiveAndClear() {
        isSectionActive.value = !isSectionActive.value

        if (!isSectionActive.value) {
            businessType.value = ""
            businessAddress.value = ""
            _forceValidation.value = false // Reseteamos la validación forzada
        }
    }

    // --- Lógica de validación interna ---
    private fun validateBusinessType(v: String): String? {
        return if (v.isBlank()) "El tipo de negocio no puede estar vacío." else null
    }

    private fun validateBusinessAddress(v: String): String? {
        return if (v.isBlank()) "La dirección del negocio no puede estar vacía." else null
    }

    /**
     * Valida si todos los campos requeridos son válidos.
     * @return `true` si la sección es válida, `false` en caso contrario.
     */
    fun validateAllFields(): Boolean {
        _forceValidation.value = true

        // Si la sección no está activa, siempre es válida.
        if (!isSectionActive.value) {
            return true
        }

        // Si está activa, validamos que no haya errores
        return businessTypeError.value == null && businessAddressError.value == null
    }

    /**
     * Devuelve un objeto `Evaluation` listo para ser guardado.
     * Retorna `null` si la sección no está activa.
     */
    @OptIn(ExperimentalTime::class)
    fun getEvaluation(): Evaluation? {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return if (isSectionActive.value) {
            Evaluation(
                clientId = 0, // El ID será asignado en el servicio
                businessType = businessType.value,
                businessAddress = businessAddress.value,
                createdAt = today.toString()
            )
        } else {
            null // Si la sección está inactiva, no hay evaluación para guardar
        }
    }
}