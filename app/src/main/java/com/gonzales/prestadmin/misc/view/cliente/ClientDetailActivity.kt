package com.gonzales.prestadmin.view.cliente

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gonzales.prestadmin.data.DarkThemePreferences
import com.gonzales.prestadmin.ui.theme.PrestAdminTheme
import com.gonzales.prestadmin.viewmodel.Cliente
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ClientDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isDarkMode = runBlocking {
            DarkThemePreferences(applicationContext).isDarkMode.first()
        }

        // Simulación de recepción del cliente (esto luego vendrá por Intent o ViewModel)
        var cliente = Cliente(
            nombres = "Juan",
            apellidos = "Pérez",
            telefono = "88887777",
            direccion = "Managua, Nicaragua",
            cedula = "001-123456-00000"
        )
        cliente = Cliente(
            nombres = intent.getStringExtra("cliente_nombres") ?: "",
            apellidos = intent.getStringExtra("cliente_apellidos") ?: "",
            telefono = intent.getStringExtra("cliente_telefono") ?: "",
            direccion = intent.getStringExtra("cliente_direccion") ?: "",
            cedula = intent.getStringExtra("cliente_cedula") ?: ""
        )


        setContent {
            PrestAdminTheme(darkTheme = isDarkMode) {
                ClientDetailScreen(cliente = cliente)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(cliente: Cliente) {
    val activity = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Cliente") },
                navigationIcon = {
                    IconButton(onClick = { (activity as? ComponentActivity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "${cliente.nombres} ${cliente.apellidos}",
                style = MaterialTheme.typography.headlineMedium
            )

            InfoRow(label = "Teléfono:", value = cliente.telefono)
            InfoRow(label = "Cédula:", value = cliente.cedula)
            InfoRow(label = "Dirección:", value = cliente.direccion)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}
