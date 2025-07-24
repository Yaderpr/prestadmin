package com.gonzales.prestadmin.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gonzales.prestadmin.data.DarkThemePreferences
import com.gonzales.prestadmin.ui.theme.PrestAdminTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class PrestamoDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tema oscuro/claro
        val isDark = runBlocking {
            DarkThemePreferences(applicationContext).isDarkMode.first()
        }

        // Recuperar extras, incluido duracionDias
        val cliente        = intent.getStringExtra("cliente")       ?: "Cliente desconocido"
        val descripcion    = intent.getStringExtra("descripcion")   ?: ""
        val monto          = intent.getDoubleExtra("monto", 0.0)
        val plazo          = intent.getStringExtra("plazo")         ?: "Mensual"
        val interes        = intent.getDoubleExtra("interes", 0.0)
        val multa          = intent.getDoubleExtra("multa", 0.0)
        val duracionDias   = intent.getIntExtra("duracionDias", 0)

        setContent {
            PrestAdminTheme(darkTheme = isDark) {
                PrestamoDetailScreen(
                    cliente      = cliente,
                    descripcion  = descripcion,
                    monto        = monto,
                    plazo        = plazo,
                    interesPct   = interes,
                    multaPct     = multa,
                    duracionDias = duracionDias
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrestamoDetailScreen(
    cliente: String,
    descripcion: String,
    monto: Double,
    plazo: String,
    interesPct: Double,
    multaPct: Double,
    duracionDias: Int
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // Cálculos usando duración
    val i = interesPct / 100.0
    val total = monto * (1 + i)
    // Cuota diaria base
    val cuotaBase = if (duracionDias > 0) total / duracionDias else total
    val cuotaConMulta = cuotaBase * (1 + multaPct / 100.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Préstamo") },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            InfoItem("Cliente", cliente)
            InfoItem("Descripción", descripcion)
            InfoItem("Monto solicitado", "C$ %.2f".format(monto))
            InfoItem("Plazo", plazo)
            InfoItem("Duración (días)", duracionDias.toString())
            InfoItem("Interés", "%.2f%%".format(interesPct))
            InfoItem("Multa por mora", "%.2f%%".format(multaPct))

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            InfoItem("Monto total a pagar", "C$ %.2f".format(total))
            InfoItem("Cuota diaria base", "C$ %.2f".format(cuotaBase))
            InfoItem("Cuota con multa", "C$ %.2f".format(cuotaConMulta))
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}
