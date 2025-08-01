package com.gonzales.prestadmin.presentation.viewmodel.client

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gonzales.prestadmin.App
import com.gonzales.prestadmin.data.service.ClientFormData
import com.gonzales.prestadmin.data.service.ClientFormService
import com.gonzales.prestadmin.domain.model.client.Client
import com.gonzales.prestadmin.domain.model.evaluation.Evaluation
import com.gonzales.prestadmin.domain.model.guarantee.Guarantee
import com.gonzales.prestadmin.domain.model.loan.Loan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

class ClientFormScreenViewModel(
    private val clientFormService: ClientFormService = App.clientFormService
) : ViewModel() {

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState = _saveState.asStateFlow()

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    fun saveFullClientForm(
        clientAndDniPhotos: Pair<Client, List<Bitmap?>>,
        loan: Loan,
        evaluation: Evaluation?,
        guarantees: List<Guarantee>?
    ) {
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            try {
                val formData = ClientFormData(
                    client = clientAndDniPhotos.first,
                    loan = loan,
                    evaluation = evaluation,
                    guarantees = guarantees,
                    dniFrontImage = clientAndDniPhotos.second[0],
                    dniBackImage = clientAndDniPhotos.second[1],
                )
                val savedClient = clientFormService.saveClientForm(formData)
                if (savedClient != null) {
                    _saveState.value = SaveState.Success
                } else {
                    _saveState.value = SaveState.Error("Error al guardar, el cliente es nulo.")
                }
            } catch (e: Exception) {
                println("Error saving client form: ${e.message}")
                _saveState.value = SaveState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}