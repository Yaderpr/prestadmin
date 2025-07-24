package com.gonzales.prestadmin.view.cliente

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gonzales.prestadmin.data.DarkThemePreferences
import com.gonzales.prestadmin.ui.theme.PrestAdminTheme
import com.gonzales.prestadmin.viewmodel.Cliente
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ClientListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fakeClientes = listOf(
            Cliente("Juan", "Pérez", "88887777", "Managua, Nicaragua", "001-123456-00000"),
            Cliente("Ana", "Gómez", "88112233", "León, Nicaragua", "002-654321-11111"),
            Cliente("Luis", "Martínez", "88994455", "Chinandega", "003-000000-22222")
        )

        val themePrefs = DarkThemePreferences(applicationContext)
        val isDark = runBlocking { themePrefs.isDarkMode.first() }

        setContent {
            PrestAdminTheme(darkTheme = isDark) {
                ClientListScreen(
                    clientes = fakeClientes,
                    onDetail = { cliente ->
                        startActivity(Intent(this, ClientDetailActivity::class.java).apply {
                            putExtra("cliente_nombres", cliente.nombres)
                            putExtra("cliente_apellidos", cliente.apellidos)
                            putExtra("cliente_telefono", cliente.telefono)
                            putExtra("cliente_direccion", cliente.direccion)
                            putExtra("cliente_cedula", cliente.cedula)
                        })
                    },
                    onEdit = { cliente ->
                        startActivity(Intent(this, EditClientActivity::class.java).apply {
                            putExtra("cliente_nombres", cliente.nombres)
                            putExtra("cliente_apellidos", cliente.apellidos)
                            putExtra("cliente_telefono", cliente.telefono)
                            putExtra("cliente_direccion", cliente.direccion)
                            putExtra("cliente_cedula", cliente.cedula)
                        })}
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientListScreen(
    clientes: List<Cliente>,
    onDetail: (Cliente) -> Unit,
    onEdit: (Cliente) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filtrados = remember(query, clientes) {
        if (query.isBlank()) clientes
        else clientes.filter {
            "${it.nombres} ${it.apellidos}".contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Clientes") },
                navigationIcon = {
                    val context = LocalContext.current
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar por nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtrados, key = { it.cedula }) { cliente ->
                    ClienteListItem(
                        cliente = cliente,
                        onDetail = { onDetail(cliente) },
                        onEdit = { onEdit(cliente) }
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
fun ClienteListItem(
    cliente: Cliente,
    onDetail: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onDetail)
        ) {
            Text(
                text = "${cliente.nombres} ${cliente.apellidos}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text("Teléfono: ${cliente.telefono}", style = MaterialTheme.typography.bodySmall)
            Text("Cédula: ${cliente.cedula}", style = MaterialTheme.typography.bodySmall)
        }

        IconButton(onClick = onDetail) {
            Icon(Icons.Default.Visibility, contentDescription = "Ver detalles")
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Editar cliente")
        }
    }
}
