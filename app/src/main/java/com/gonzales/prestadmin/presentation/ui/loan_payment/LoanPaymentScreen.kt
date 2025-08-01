package com.gonzales.prestadmin.presentation.ui.loan_payment

import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gonzales.prestadmin.presentation.viewmodel.loan_payment.LoanPaymentViewModel
import com.gonzales.prestadmin.presentation.viewmodel.loan_payment.PaymentViewModel
import com.gonzales.prestadmin.presentation.viewmodel.loan_payment.SaveState
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanPaymentScreen(
    clientId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context  as? ComponentActivity
    val coroutineScope = rememberCoroutineScope()
    var selectedSection by remember { mutableIntStateOf(0) }
    val sections = listOf("Abonar", "Detalles del Préstamo")

    val orchestratorViewModel: LoanPaymentViewModel = viewModel()
    val paymentSectionViewModel: PaymentViewModel = viewModel()

    val uiState by orchestratorViewModel.uiState.collectAsState()
    val saveState = uiState.saveState
    val loanId = uiState.loan?.id
    LaunchedEffect(clientId) {
        orchestratorViewModel.loadInitialData(clientId)
    }
    LaunchedEffect(uiState.loan, uiState.client) {
        val client = uiState.client
        val loan = uiState.loan
        val payments = uiState.payments
        if (client != null && loan != null) {
            paymentSectionViewModel.initializeWithData(client, loan, payments)
        }
    }

    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.Success -> {
                Toast.makeText(activity, "Abono guardado con éxito.", Toast.LENGTH_SHORT).show()
                paymentSectionViewModel.clearAmountInput()
                orchestratorViewModel.loadInitialData(clientId)
            }
            is SaveState.Error -> {
                Toast.makeText(activity, "Error: ${(saveState as SaveState.Error).message}", Toast.LENGTH_SHORT).show()
                orchestratorViewModel.resetSaveState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Préstamo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // El botón de Guardar ahora pertenece a este contenedor
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        val payment = loanId?.let { id -> paymentSectionViewModel.getPayment() }
                        if (payment != null) {
                            coroutineScope.launch {
                                orchestratorViewModel.processPayment(payment)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedSection == 0 && loanId != null && saveState !is SaveState.Loading
                ) {
                    when (saveState) {
                        is SaveState.Loading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.height(24.dp))
                        is SaveState.Success -> Text("Guardado!")
                        is SaveState.Error -> Text("Error")
                        else -> Text("Procesar Abono")
                    }
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sections.forEachIndexed { index, title ->
                    FilterChip(
                        selected = selectedSection == index,
                        onClick = { selectedSection = index },
                        label = { Text(title) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedSection) {
                    0 -> PaymentSection(
                        viewModel = paymentSectionViewModel
                    )

                    1 -> LoanDetailsSection(
                        client = uiState.client,
                        loan = uiState.loan
                    )
                }
            }
        }
    }
}