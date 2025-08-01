package com.gonzales.prestadmin.presentation.ui.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gonzales.prestadmin.presentation.viewmodel.client.EvaluationViewModel

@Composable
fun EvaluationSection(
    viewModel: EvaluationViewModel = viewModel()
) {
    // Observa los estados del ViewModel
    val businessType by viewModel.businessType.collectAsState()
    val businessAddress by viewModel.businessAddress.collectAsState()
    val isSectionActive by viewModel.isSectionActive.collectAsState()

    // Observa los estados de error
    val businessTypeError by viewModel.businessTypeError.collectAsState()
    val businessAddressError by viewModel.businessAddressError.collectAsState()

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Control para activar/desactivar la sección
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Evaluar detalles del negocio", style = MaterialTheme.typography.titleMedium)
            Switch(
                checked = isSectionActive,
                onCheckedChange = { viewModel.toggleSectionActiveAndClear() }
            )
        }

        // Campos de entrada, habilitados solo si la sección está activa
        OutlinedTextField(
            value = businessType,
            onValueChange = { viewModel.updateBusinessType(it) },
            label = { Text("Tipo de negocio") },
            enabled = isSectionActive,
            isError = businessTypeError != null,
            supportingText = {
                businessTypeError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = businessAddress,
            onValueChange = { viewModel.updateBusinessAddress(it) },
            label = { Text("Dirección del negocio") },
            enabled = isSectionActive,
            isError = businessAddressError != null,
            supportingText = {
                businessAddressError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}