package com.gonzales.prestadmin.presentation.viewmodel.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gonzales.prestadmin.App
import com.gonzales.prestadmin.data.repository.client.ClientRepository
import com.gonzales.prestadmin.data.repository.loan.LoanRepository
import com.gonzales.prestadmin.domain.model.client.ClienteUI
import com.gonzales.prestadmin.domain.model.client.mapClientAndLoanToClienteUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class ActiveClientListSectionViewModel(
    private val clientRepository: ClientRepository = App.clientRepository,
    private val loanRepository: LoanRepository = App.loanRepository // Añadimos el repositorio de préstamos
) : ViewModel() {

    private var allClientsWithLoans: List<ClienteUI> = emptyList()

    private val _clients = MutableStateFlow<List<ClienteUI>>(emptyList())
    val clients: StateFlow<List<ClienteUI>> = _clients.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadClientsWithLoans()
    }

    fun loadClientsWithLoans() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Paso 1: Obtener todos los clientes y préstamos
                val fetchedClients = clientRepository.getAllClients()
                val fetchedLoans = loanRepository.getAllLoans() // Asumo que tienes un método así

                // Paso 2: Unir los datos para crear la lista de UI
                allClientsWithLoans = fetchedClients.flatMap { client ->
                    fetchedLoans.filter { it.clientId == client.id }.map { loan ->
                        mapClientAndLoanToClienteUI(client, loan)
                    }
                }
                _clients.value = allClientsWithLoans
            } catch (e: Exception) {
                println("Error fetching clients with loans: ${e.message}")
                _clients.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchClients(query: String) {
        val filteredList = allClientsWithLoans.filter {
            it.abonado.contains(query, ignoreCase = true) ||
                    it.nombre.contains(query, ignoreCase = true) ||
                    it.apellido.contains(query, ignoreCase = true)
        }
        _clients.value = filteredList
    }
}