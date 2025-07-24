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
import com.gonzales.prestadmin.ui.theme.PrestAdminTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class EditPrestamoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isDark = runBlocking {
            DarkThemePreferences(applicationContext)
                .isDarkMode.first()
        }

        // Recuperamos todos los extras, incluido el nuevo duracionDias
        val prestamoId      = intent.getStringExtra("prestamo_id")    ?: ""
        val cliente         = intent.getStringExtra("cliente")        ?: ""
        val descripcion     = intent.getStringExtra("descripcion")    ?: ""
        val montoInicial    = intent.getDoubleExtra("monto", 0.0)
        val plazoInicial    = intent.getStringExtra("plazo")          ?: "Mensual"
        val interesInicial  = intent.getDoubleExtra("interes", 0.0)
        val multaInicial    = intent.getDoubleExtra("multa", 0.0)
        val duracionInicial = intent.getIntExtra("duracionDias", 0)

        setContent {
            PrestAdminTheme(darkTheme = isDark) {
                EditarPrestamoScreen(
                    prestamoId        = prestamoId,
                    cliente           = cliente,
                    descripcionInicial= descripcion,
                    montoInicial      = montoInicial,
                    plazoInicial      = plazoInicial,
                    interesInicial    = interesInicial,
                    multaInicial      = multaInicial.toDouble(),
                    duracionInicial   = duracionInicial
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPrestamoScreen(
    prestamoId: String,
    cliente: String,
    descripcionInicial: String,
    montoInicial: Double,
    plazoInicial: String,
    interesInicial: Double,
    multaInicial: Double,
    duracionInicial: Int          // ← nuevo parámetro
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var descripcion by remember { mutableStateOf(descripcionInicial) }
    var monto       by remember { mutableStateOf(montoInicial.toString()) }
    var plazo       by remember { mutableStateOf(plazoInicial) }
    var interes     by remember { mutableStateOf(interesInicial.toString()) }
    var multa       by remember { mutableStateOf(multaInicial.toString()) }
    var duracionDias by remember { mutableStateOf(duracionInicial.toString()) } // ← estado nuevo

    var totalAPagar    by remember { mutableStateOf<Float?>(null) }
    var cuota          by remember { mutableStateOf<Float?>(null) }
    var cuotaConMulta  by remember { mutableStateOf<Float?>(null) }

    val plazos = listOf("Mensual", "Semanal", "Diario")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Préstamo") },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {

            Text("Cliente: $cliente", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = monto,
                onValueChange = { monto = it.filter { ch->ch.isDigit()||ch=='.'} },
                label = { Text("Monto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Text("Plazo", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                plazos.forEach { p ->
                    FilterChip(
                        selected = plazo == p,
                        onClick  = { plazo = p },
                        label    = { Text(p) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            // — NUEVO CAMPO DURACIÓN —
            OutlinedTextField(
                value = duracionDias,
                onValueChange = { duracionDias = it.filter{ ch-> ch.isDigit() } },
                label = { Text("Duración (días)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = interes,
                onValueChange = { interes = it.filter { ch->ch.isDigit()||ch=='.'} },
                label = { Text("Interés %") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = multa,
                onValueChange = { multa = it.filter { ch->ch.isDigit()||ch=='.'} },
                label = { Text("Multa %") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Button(onClick = {
                val base = monto.toFloatOrNull() ?: return@Button
                val i    = interes.toFloatOrNull() ?: 0f
                val m    = multa.toFloatOrNull()   ?: 0f
                val days = duracionDias.toIntOrNull() ?: 1

                // cálculo
                val interesTotal = base * (i / 100)
                val montoFinal   = base + interesTotal
                val cuotaBase    = when (plazo) {
                    "Mensual" -> montoFinal / days * 30f
                    "Semanal" -> montoFinal / days * 7f
                    "Diario"  -> montoFinal / days * 1f
                    else      -> montoFinal
                }

                totalAPagar   = montoFinal
                cuota         = cuotaBase
                cuotaConMulta = cuotaBase + (cuotaBase * (m / 100))
            },
                modifier = Modifier.fillMaxWidth()) {
                Text("Calcular")
            }
            Spacer(Modifier.height(8.dp))

            totalAPagar?.let {
                Text("Monto total a pagar: C$ %.2f".format(it))
                Text("Cuota por período: C$ %.2f".format(cuota ?: 0f))
                Text("Cuota con multa: C$ %.2f".format(cuotaConMulta ?: 0f))
            }
            Spacer(Modifier.height(16.dp))

            Button(onClick = {
                // Empaquetar y devolver el Prestamo actualizado
                // TODO: persistir o devolver via ViewModel
                activity?.finish()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar cambios")
            }
        }
    }
}
