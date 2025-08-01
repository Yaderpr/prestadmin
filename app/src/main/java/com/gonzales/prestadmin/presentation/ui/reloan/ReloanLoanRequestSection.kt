package com.gonzales.prestadmin.presentation.ui.reloan

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gonzales.prestadmin.presentation.viewmodel.client.LoanRequestViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReloanLoanRequestSection(
    viewModel: LoanRequestViewModel = viewModel()
) {
    // Usando los nombres de propiedades correctos del ViewModel
    val clienteSearchInput by viewModel.clientSearchInput.collectAsState()
    val clientesExpanded by viewModel.clientesExpanded.collectAsState()
    val clientes by viewModel.clients.collectAsState()

    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // --- Consumiendo Estados del ViewModel (inputs) ---
    val dayOfWeekEditable by viewModel.dayOfWeekEditable.collectAsState()
    val dayOfWeekExpanded by viewModel.dayOfWeekExpanded.collectAsState()
    val capitalText by viewModel.capitalText.collectAsState()
    val interestPct by viewModel.interestPct.collectAsState()
    val modalityExpanded by viewModel.modalityExpanded.collectAsState()
    val modality by viewModel.modality.collectAsState()
    val termExpanded by viewModel.termExpanded.collectAsState()
    val term by viewModel.term.collectAsState()
    val disbursementDate by viewModel.disbursementDate.collectAsState()
    val showDatePicker by viewModel.showDatePicker.collectAsState()
    val observation by viewModel.observation.collectAsState()

    // --- Consumiendo ESTADOS DE ERROR del ViewModel ---
    val capitalError by viewModel.capitalError.collectAsState()
    val interestPctError by viewModel.interestPctError.collectAsState()
    val termError by viewModel.termError.collectAsState()
    val dayOfWeekError by viewModel.dayOfWeekError.collectAsState()

    // --- Consumiendo valores CALCULADOS del ViewModel ---
    val cuotaCalculated by viewModel.cuotaCalculated.collectAsState()
    val primeraCuotaCalculated by viewModel.primeraCuotaCalculated.collectAsState()
    val diaSemanaDisplayCalculated by viewModel.diaSemanaDisplayCalculated.collectAsState()
    val fechaVencimientoCalculated by viewModel.fechaVencimientoCalculated.collectAsState()
    val totalDeudaCalculated by viewModel.totalDeudaCalculated.collectAsState()

    // --- Opciones de Plazo (lógica de presentación local) ---
    val opcionesPlazo = when (modality) {
        "Diario" -> buildList {
            add("20 cuotas a 1 mes")
            add("30 cuotas a 1.5 meses")
            add("40 cuotas a 2 meses")
            add("50 cuotas a 2.5 meses")
            add("60 cuotas a 3 meses")
            add("70 cuotas a 3.5 meses")
            add("80 cuotas a 4 meses")
            add("90 cuotas a 4.5 meses")
            add("100 cuotas a 5 meses")
            add("110 cuotas a 5.5 meses")
            add("120 cuotas a 6 meses")
            add("365 cuotas a 12 meses")
        }
        "Semanal" -> buildList {
            add("4 semanas a 1 mes")
            add("6 semanas a 1.5 meses")
            add("8 semanas a 2 meses")
            add("10 semanas a 2.5 meses")
            add("12 semanas a 3 meses")
            add("14 semanas a 3.5 meses")
            add("16 semanas a 4 meses")
            add("18 semanas a 4.5 meses")
            add("20 semanas a 5 meses")
            add("22 semanas a 5.5 meses")
            add("24 semanas a 6 meses")
            add("52 semanas a 12 meses")
        }
        "Mensual" -> (1..12).map { "$it cuota${if (it > 1) "s" else ""} a $it mes${if (it > 1) "es" else ""}" }
        else -> emptyList()
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = disbursementDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        AlertDialog(
            onDismissRequest = { viewModel.updateShowDatePicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.updateDisbursementDate(selectedDate)
                    }
                    viewModel.updateShowDatePicker(false)
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.updateShowDatePicker(false) }) {
                    Text("Cancelar")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .heightIn(max = 580.dp)
                        .widthIn(max = 290.dp)
                ) {
                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sección de búsqueda y selección de cliente
        ExposedDropdownMenuBox(
            expanded = clientesExpanded,
            onExpandedChange = { viewModel.updateClientesExpanded(!clientesExpanded) }
        ) {
            OutlinedTextField(
                value = clienteSearchInput,
                onValueChange = {
                    viewModel.updateClientSearchInput(it)
                },
                label = { Text("Cliente") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientesExpanded)
                }
            )
            // Filtramos la lista de clientes en la UI
            val filteredClientes = clientes.filter {
                val fullName = "${it.firstName} ${it.lastName}"
                fullName.contains(clienteSearchInput, ignoreCase = true)
            }
            ExposedDropdownMenu(
                expanded = clientesExpanded && filteredClientes.isNotEmpty(),
                onDismissRequest = { viewModel.updateClientesExpanded(false) }
            ) {
                filteredClientes.forEach { cliente ->
                    val fullName = "${cliente.firstName} ${cliente.lastName}"
                    DropdownMenuItem(
                        text = { Text(fullName) },
                        onClick = {
                            viewModel.selectClient(cliente)
                            viewModel.updateClientesExpanded(false)
                        }
                    )
                }
            }
        }

        // Capital
        OutlinedTextField(
            value = capitalText,
            onValueChange = { viewModel.updateCapitalText(it) },
            label = { Text("Capital") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = capitalError != null,
            supportingText = { capitalError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
        )

        // Interés
        Text("Interés (%)")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = interestPct.toFloatOrNull() ?: 0f,
                onValueChange = { viewModel.updateInterestPct(it.toInt().toString()) },
                valueRange = 0f..50f,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = interestPct,
                onValueChange = {
                    val newValue = it.filter { char -> char.isDigit() }
                    viewModel.updateInterestPct(newValue.toIntOrNull()?.coerceIn(0, 50)?.toString() ?: "")
                },
                label = { Text("Interés") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = interestPctError != null,
                supportingText = { interestPctError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.width(80.dp)
            )
        }

        // Modalidad
        ExposedDropdownMenuBox(
            expanded = modalityExpanded,
            onExpandedChange = { viewModel.updateModalityExpanded(!modalityExpanded) }
        ) {
            OutlinedTextField(
                value = modality,
                onValueChange = {},
                readOnly = true,
                label = { Text("Modalidad") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modalityExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = modalityExpanded,
                onDismissRequest = { viewModel.updateModalityExpanded(false) }
            ) {
                viewModel.modalities.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            viewModel.updateModality(it)
                            viewModel.updateModalityExpanded(false)
                            viewModel.updateTerm("")
                        }
                    )
                }
            }
        }

        // Plazo
        ExposedDropdownMenuBox(
            expanded = termExpanded,
            onExpandedChange = { viewModel.updateTermExpanded(!termExpanded) }
        ) {
            OutlinedTextField(
                value = term,
                onValueChange = {},
                readOnly = true,
                label = { Text("Plazo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = termExpanded) },
                isError = termError != null,
                supportingText = { termError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = termExpanded,
                onDismissRequest = { viewModel.updateTermExpanded(false) }
            ) {
                opcionesPlazo.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            viewModel.updateTerm(it)
                            viewModel.updateTermExpanded(false)
                        }
                    )
                }
            }
        }

        // Cuota
        OutlinedTextField(
            value = if (cuotaCalculated > 0) "C$ %.2f".format(cuotaCalculated) else "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Cuota") },
            modifier = Modifier.fillMaxWidth()
        )

        // Fecha de desembolso
        OutlinedTextField(
            value = disbursementDate.format(fmt),
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha de desembolso") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { viewModel.updateShowDatePicker(true) }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Seleccionar fecha")
                }
            }
        )

        // Primera cuota
        OutlinedTextField(
            // FIX: Formatear la fecha si no es nula, de lo contrario usar una cadena vacía.
            value = primeraCuotaCalculated?.format(fmt) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Primera cuota") },
            modifier = Modifier.fillMaxWidth()
        )

        // Día de la semana
        if (modality == "Semanal" || modality == "Mensual") {
            ExposedDropdownMenuBox(
                expanded = dayOfWeekExpanded,
                onExpandedChange = { viewModel.updateDayOfWeekExpanded(!dayOfWeekExpanded) }
            ) {
                OutlinedTextField(
                    value = diaSemanaDisplayCalculated ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Día de la semana") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayOfWeekExpanded) },
                    isError = dayOfWeekError != null,
                    supportingText = { dayOfWeekError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = dayOfWeekExpanded,
                    onDismissRequest = { viewModel.updateDayOfWeekExpanded(false) }
                ) {
                    val diasDeLaSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
                    diasDeLaSemana.forEach { dia ->
                        DropdownMenuItem(
                            text = { Text(dia) },
                            onClick = {
                                viewModel.updateDayOfWeekEditable(dia)
                                viewModel.updateDayOfWeekExpanded(false)
                            }
                        )
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = diaSemanaDisplayCalculated ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Día de la semana") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Fecha de vencimiento
        OutlinedTextField(
            // FIX: Formatear la fecha si no es nula, de lo contrario usar una cadena vacía.
            value = fechaVencimientoCalculated?.format(fmt) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha de vencimiento") },
            modifier = Modifier.fillMaxWidth()
        )

        // Deuda Total
        OutlinedTextField(
            value = if (totalDeudaCalculated > 0) "C$ %.2f".format(totalDeudaCalculated) else "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Deuda Total") },
            modifier = Modifier.fillMaxWidth()
        )

        // Observación
        OutlinedTextField(
            value = observation,
            onValueChange = { viewModel.updateObservation(it) },
            label = { Text("Observación") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )

        Spacer(Modifier.height(16.dp))
    }
}