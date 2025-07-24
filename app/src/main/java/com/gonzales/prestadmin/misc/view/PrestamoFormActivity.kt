package com.gonzales.prestadmin.view

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gonzales.prestadmin.data.DarkThemePreferences
import com.gonzales.prestadmin.model.Prestamo
import com.gonzales.prestadmin.viewmodel.Cliente
import com.gonzales.prestadmin.ui.theme.PrestAdminTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class LoanRequestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isDark = runBlocking { DarkThemePreferences(applicationContext).isDarkMode.first() }

        val clientes = listOf(
            Cliente("Juan", "Pérez", "88887777", "Managua", "001-123456-00000"),
            Cliente("Ana", "Gómez", "88112233", "León", "002-654321-11111"),
            Cliente("Luis", "Martínez", "88994455", "Chinandega", "003-000000-22222")
        )

        setContent {
            PrestAdminTheme(darkTheme = isDark) {
                LoanRequestScreen(
                    clientes = clientes,
                    onGuardar = { prestamo ->
                        // TODO: persistir el prestamo
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanRequestScreen(
    clientes: List<Cliente>,
    onGuardar: (Prestamo) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var search by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedCliente by remember { mutableStateOf<Cliente?>(null) }

    var descripcion by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var tasaInteres by remember { mutableStateOf("") }
    var tasaMulta by remember { mutableStateOf("") }
    var plazo by remember { mutableStateOf("Mensual") }
    var duracionDias by remember { mutableStateOf("") }  // ahora String

    val plazos = listOf("Mensual", "Semanal", "Diario")

    var totalPagar by remember { mutableStateOf(0.0) }
    var montoPlazo by remember { mutableStateOf(0.0) }
    var montoConMulta by remember { mutableStateOf(0.0) }

    val opciones = remember(search, clientes) {
        if (search.isBlank()) clientes
        else clientes.filter {
            "${it.nombres} ${it.apellidos}"
                .contains(search, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitud de Préstamo") },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cliente
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCliente?.let { "${it.nombres} ${it.apellidos}" } ?: search,
                    onValueChange = {
                        search = it
                        selectedCliente = null
                    },
                    label = { Text("Cliente") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    opciones.forEach { cliente ->
                        DropdownMenuItem(
                            text = { Text("${cliente.nombres} ${cliente.apellidos}") },
                            onClick = {
                                selectedCliente = cliente
                                search = "${cliente.nombres} ${cliente.apellidos}"
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            // Plan
            Text("Plan de préstamo", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                plazos.forEach { p ->
                    FilterChip(
                        selected = plazo == p,
                        onClick = { plazo = p },
                        label = { Text(p) }
                    )
                }
            }

            // Duración
            OutlinedTextField(
                value = duracionDias,
                onValueChange = { duracionDias = it.filter { ch -> ch.isDigit() } },
                label = { Text("Duración (días)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Monto
            OutlinedTextField(
                value = monto,
                onValueChange = { monto = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Monto principal") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Interés
            OutlinedTextField(
                value = tasaInteres,
                onValueChange = { tasaInteres = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Tasa de interés (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Multa
            OutlinedTextField(
                value = tasaMulta,
                onValueChange = { tasaMulta = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Tasa de multa (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Botones
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(onClick = {
                    // Cálculo
                    val P = monto.toDoubleOrNull() ?: 0.0
                    val i = tasaInteres.toDoubleOrNull()?.div(100) ?: 0.0
                    val m = tasaMulta.toDoubleOrNull()?.div(100) ?: 0.0
                    totalPagar = P * (1 + i)
                    montoPlazo = totalPagar
                    montoConMulta = totalPagar * (1 + m)
                }) {
                    Text("Calcular")
                }
                Button(onClick = {
                    // Empaquetar Prestamo
                    val P = monto.toDoubleOrNull() ?: return@Button
                    val i = tasaInteres.toDoubleOrNull() ?: return@Button
                    val m = tasaMulta.toDoubleOrNull() ?: return@Button
                    val days = duracionDias.toIntOrNull() ?: return@Button
                    val clienteName = selectedCliente?.let { "${it.nombres} ${it.apellidos}" } ?: ""
                    val id = "PR-${System.currentTimeMillis()}"

                    onGuardar(
                        Prestamo(
                            id = id,
                            clienteNombre = clienteName,
                            descripcion = descripcion,
                            monto = P,
                            plazo = plazo,
                            tasaInteres = i,
                            tasaMulta = m,
                            duracionDias = days
                        )
                    )
                    activity?.finish()
                }) {
                    Text("Guardar")
                }
            }

            // Resultados
            if (totalPagar > 0.0) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Total a pagar: %.2f".format(totalPagar))
                Text("A pagar ($plazo): %.2f".format(montoPlazo))
                Text("Con multa: %.2f".format(montoConMulta))
            }
        }
    }
}
