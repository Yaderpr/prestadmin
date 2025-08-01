package com.gonzales.prestadmin.presentation.viewmodel.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gonzales.prestadmin.App
import com.gonzales.prestadmin.data.repository.guarantee.GuaranteeRepository
import com.gonzales.prestadmin.domain.model.guarantee.Guarantee
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class GuaranteeViewModel(
    private val guaranteeRepository: GuaranteeRepository = App.guaranteeRepository
) : ViewModel() {

    // --- Estado para controlar si la sección está activa/habilitada ---
    private val _isSectionActive = MutableStateFlow(true)
    val isSectionActive: StateFlow<Boolean> = _isSectionActive.asStateFlow()

    // Lista de garantías como StateFlow
    private val _guarantees = MutableStateFlow<List<Guarantee>>(emptyList())
    val guarantees: StateFlow<List<Guarantee>> = _guarantees.asStateFlow()

    // Un mapa para gestionar errores de validación por índice del elemento
    private val _guaranteeErrors = MutableStateFlow<Map<Int, Map<String, String>>>(emptyMap())
    val guaranteeErrors: StateFlow<Map<Int, Map<String, String>>> = _guaranteeErrors.asStateFlow()

    private val _isLoadingGuarantees = MutableStateFlow(false)
    val isLoadingGuarantees: StateFlow<Boolean> = _isLoadingGuarantees.asStateFlow()

    /**
     * Carga las garantías existentes para un cliente específico.
     * Esta es la única función que requiere el clientId para obtener los datos.
     * @param clientId El ID del cliente.
     */
    fun loadGuaranteesForClient(clientId: Int) {
        viewModelScope.launch {
            _isLoadingGuarantees.value = true
            try {
                val fetchedGuarantees = guaranteeRepository.getGuaranteesByClientId(clientId)
                _guarantees.value = fetchedGuarantees
                _guaranteeErrors.value = emptyMap()
                _isSectionActive.value = fetchedGuarantees.isNotEmpty() // Activa la sección si hay garantías
            } catch (e: Exception) {
                e.printStackTrace()
                _guarantees.value = emptyList()
            } finally {
                _isLoadingGuarantees.value = false
            }
        }
    }

    /**
     * Agrega un nuevo elemento de garantía vacío a la lista sin necesidad del clientId.
     * El clientId se asignará más tarde, en la capa de servicio.
     */
    @OptIn(ExperimentalTime::class)
    fun addGuarantee() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val newGuarantee = Guarantee(clientId = 0, description = "", estimatedPrice = 0.0, createdAt = today.toString())
        _guarantees.value = _guarantees.value + newGuarantee
    }

    /**
     * Remueve un elemento de la lista de garantías por su índice.
     * @param index El índice del elemento a remover.
     */
    fun removeGuarantee(index: Int) {
        if (index in _guarantees.value.indices) {
            _guarantees.value = _guarantees.value.toMutableList().apply { removeAt(index) }
            _guaranteeErrors.value = _guaranteeErrors.value.toMutableMap().apply { remove(index) }
        }
    }

    /**
     * Actualiza un elemento de garantía en la lista.
     * @param index El índice del elemento a actualizar.
     * @param updatedGuarantee El objeto de garantía con los datos actualizados.
     */
    fun updateGuaranteeItem(index: Int, updatedGuarantee: Guarantee) {
        if (index in _guarantees.value.indices) {
            _guarantees.value = _guarantees.value.toMutableList().apply {
                this[index] = updatedGuarantee
            }
            // La validación se encargará de los errores, pero podemos limpiar
            // el error de un campo si el usuario lo corrige
            val currentErrors = _guaranteeErrors.value.toMutableMap()
            val itemErrors = validateGuaranteeItem(updatedGuarantee).toMutableMap()

            if (itemErrors.isEmpty()) {
                currentErrors.remove(index)
            } else {
                currentErrors[index] = itemErrors
            }
            _guaranteeErrors.value = currentErrors
        }
    }

    /**
     * Alterna el estado de la sección. Si se desactiva, limpia los datos.
     */
    fun toggleSectionActiveAndClear() {
        _isSectionActive.value = !_isSectionActive.value

        if (!_isSectionActive.value) {
            _guarantees.value = emptyList()
            _guaranteeErrors.value = emptyMap()
        }
    }

    // Valida un solo elemento de garantía
    private fun validateGuaranteeItem(guarantee: Guarantee): Map<String, String> {
        val itemErrors = mutableMapOf<String, String>()
        if (guarantee.description.isBlank()) {
            itemErrors["description"] = "La descripción no debe estar vacía."
        }
        if (guarantee.estimatedPrice == 0.0) {
            itemErrors["estimatedPrice"] = "El precio estimado no debe estar vacío."
        } else {
            try {
                guarantee.estimatedPrice.toDouble()
            } catch (e: NumberFormatException) {
                itemErrors["estimatedPrice"] = "Formato de precio inválido."
            }
        }
        return itemErrors
    }

    /**
     * Valida toda la lista de garantías y actualiza el estado de errores.
     * @return true si todas las garantías son válidas, false en caso contrario.
     */
    fun validateGuarantees(): Boolean {
        // Si la sección no está activa, siempre es válida.
        if (!_isSectionActive.value) {
            return true
        }

        var isValid = true
        val currentErrors = mutableMapOf<Int, Map<String, String>>()

        _guarantees.value.forEachIndexed { index, guarantee ->
            val itemErrors = validateGuaranteeItem(guarantee)
            if (itemErrors.isNotEmpty()) {
                currentErrors[index] = itemErrors
                isValid = false
            }
        }
        _guaranteeErrors.value = currentErrors
        return isValid
    }

    /**
     * Retorna la lista de garantías para su uso final, por ejemplo, para guardar.
     * Retorna una lista vacía si la sección no está activa.
     */
    fun getGuaranteeList(): List<Guarantee> {
        return if (_isSectionActive.value) _guarantees.value else emptyList()
    }
}