package com.gonzales.prestadmin.presentation.ui.loan_payment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gonzales.prestadmin.presentation.viewmodel.loan_payment.PaymentViewModel
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSection(
    viewModel: PaymentViewModel = viewModel()
) {
    val clientName by viewModel.clientName.collectAsState()
    val loanCode by viewModel.loanCode.collectAsState()
    val paymentDate by viewModel.paymentDate.collectAsState()
    val baseQuota by viewModel.baseQuota.collectAsState()
    val amountPaidToday by viewModel.amountPaidToday.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val amountError by viewModel.amountError.collectAsState()

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Abonar Préstamo", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = clientName,
            onValueChange = { /* Solo lectura */ },
            label = { Text("Nombre del Cliente") },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = loanCode,
            onValueChange = { /* Solo lectura */ },
            label = { Text("Código de Préstamo") },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = paymentDate.toJavaLocalDate().format(dateFormatter),
            onValueChange = { /* Solo lectura */ },
            label = { Text("Fecha de Pago") },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = String.format("%.2f", baseQuota),
            onValueChange = { /* Solo lectura */ },
            label = { Text("Cuota Base") },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = String.format("%.2f", amountPaidToday),
            onValueChange = { /* Solo lectura */ },
            label = { Text("Monto Pagado del Día") },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { viewModel.updateAmount(it) },
            label = { Text("Monto a Pagar") },
            isError = amountError != null,
            supportingText = { amountError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}