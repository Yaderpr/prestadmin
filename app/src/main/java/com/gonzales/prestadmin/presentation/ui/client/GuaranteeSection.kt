package com.gonzales.prestadmin.presentation.ui.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gonzales.prestadmin.presentation.viewmodel.client.GuaranteeViewModel

@Composable
fun GuaranteeSection(
    viewModel: GuaranteeViewModel = viewModel()
) {
    // Observa los estados del ViewModel
    val guarantees by viewModel.guarantees.collectAsState()
    val guaranteeErrors by viewModel.guaranteeErrors.collectAsState()
    val isSectionActive by viewModel.isSectionActive.collectAsState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Control para activar/desactivar la sección
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Garantías (Opcional)", style = MaterialTheme.typography.titleMedium)
            Switch(
                checked = isSectionActive,
                onCheckedChange = { viewModel.toggleSectionActiveAndClear() }
            )
        }

        // El resto de la UI se muestra solo si la sección está activa
        if (isSectionActive) {
            guarantees.forEachIndexed { idx, guarantee ->
                val errorsForThisItem = guaranteeErrors[idx] ?: emptyMap()
                val descriptionError = errorsForThisItem["description"]
                val estimatedPriceError = errorsForThisItem["estimatedPrice"]

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = guarantee.description,
                        onValueChange = { newDescription ->
                            val updatedGuarantee = guarantee.copy(description = newDescription)
                            viewModel.updateGuaranteeItem(idx, updatedGuarantee)
                        },
                        label = { Text("Descripción") },
                        isError = descriptionError != null,
                        supportingText = {
                            descriptionError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))

                    OutlinedTextField(
                        value = if(guarantee.estimatedPrice == 0.0) "" else guarantee.estimatedPrice.toString(),
                        onValueChange = { newPrice ->
                            val updatedGuarantee = guarantee.copy(estimatedPrice = newPrice.toDoubleOrNull() ?: 0.0)
                            viewModel.updateGuaranteeItem(idx, updatedGuarantee)
                        },
                        label = { Text("Precio estimado") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = estimatedPriceError != null,
                        supportingText = {
                            estimatedPriceError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.width(120.dp)
                    )

                    IconButton(onClick = { viewModel.removeGuarantee(idx) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar garantía")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.addGuarantee() },
                modifier = Modifier.align(Alignment.End),
                enabled = isSectionActive
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar garantía")
                Spacer(Modifier.width(4.dp))
                Text("Agregar garantía")
            }
        }
    }
}