package com.gonzales.prestadmin.presentation.ui.loan_payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gonzales.prestadmin.domain.model.client.Client
import com.gonzales.prestadmin.domain.model.loan.Loan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailsSection(
    client: Client?,
    loan: Loan?
) {
    if (client == null || loan == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Cargando detalles...")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Detalles del Cliente", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = "${client.firstName} ${client.lastName}",
            onValueChange = { /* Solo lectura */ },
            label = { Text("Nombre del Cliente") },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = client.identificationNumber,
            onValueChange = { /* Solo lectura */ },
            label = { Text("DNI") },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = client.phoneNumber,
            onValueChange = { /* Solo lectura */ },
            label = { Text("Teléfono") },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = client.address,
            onValueChange = { /* Solo lectura */ },
            label = { Text("Dirección") },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Detalles del Préstamo", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = loan.id.toString(),
            onValueChange = { /* Solo lectura */ },
            label = { Text("Código de Préstamo") },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = String.format("%.2f", loan.totalDebt),
            onValueChange = { /* Solo lectura */ },
            label = { Text("Deuda Total") },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = String.format("%.2f", loan.paidAmount),
            onValueChange = { /* Solo lectura */ },
            label = { Text("Monto Pagado") },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = String.format("%.2f", loan.remainingBalance),
            onValueChange = { /* Solo lectura */ },
            label = { Text("Saldo Pendiente") },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = loan.installmentsTerm,
            onValueChange = { /* Solo lectura */ },
            label = { Text("Número de Cuotas") },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
    }
}