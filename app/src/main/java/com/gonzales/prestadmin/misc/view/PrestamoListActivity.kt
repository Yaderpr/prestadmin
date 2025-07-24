package com.gonzales.prestadmin.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import com.gonzales.prestadmin.data.DarkThemePreferences
import com.gonzales.prestadmin.model.Prestamo
import com.gonzales.prestadmin.ui.theme.PrestAdminTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class LoanListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isDark = runBlocking {
            DarkThemePreferences(applicationContext).isDarkMode.first()
        }

        // Datos simulados
        val prestamos = listOf(
            Prestamo("L1","Juan Pérez", 1000.0, "Mensual", 5.0, 1.0),
            Prestamo("L2","Ana Gómez", 500.0, "Semanal", 3.0, 0.5),
            Prestamo("L3","Luis Martínez", 2000.0, "Diario", 7.0, 2.0)
        )

        setContent {
            PrestAdminTheme(darkTheme = isDark) {
                LoanListScreen(
                    prestamos = prestamos,
                    onSchedule = { prestamo ->
                        startActivity(Intent(this, PaymentScheduleActivity::class.java).apply {
                            putExtra("prestamo_id", prestamo.id)
                            putExtra("cliente", prestamo.clienteNombre)
                            putExtra("descripcion", prestamo.descripcion)
                            putExtra("monto", prestamo.monto.toFloat())
                            putExtra("plazo", prestamo.plazo)
                            putExtra("interes", prestamo.tasaInteres.toFloat())
                            putExtra("multa", prestamo.tasaMulta.toFloat())
                            putExtra("duration", 12)
                        })
                    },
                    onDetail = { prestamo ->
                        startActivity(Intent(this, PrestamoDetailActivity::class.java).apply {
                            putExtra("prestamo_id", prestamo.id)
                            putExtra("cliente", prestamo.clienteNombre)
                            putExtra("descripcion", prestamo.descripcion)
                            putExtra("monto", prestamo.monto)
                            putExtra("plazo", prestamo.plazo)
                            putExtra("interes", prestamo.tasaInteres)
                            putExtra("multa", prestamo.tasaMulta)
                        })
                    },
                    onEdit = { prestamo ->
                        startActivity(Intent(this, EditPrestamoActivity::class.java).apply {
                            putExtra("prestamo_id", prestamo.id)
                            putExtra("cliente", prestamo.clienteNombre)
                            putExtra("descripcion", prestamo.descripcion)
                            putExtra("monto", prestamo.monto)
                            putExtra("plazo", prestamo.plazo)
                            putExtra("interes", prestamo.tasaInteres)
                            putExtra("multa", prestamo.tasaMulta)
                        })}
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanListScreen(
    prestamos: List<Prestamo>,
    onSchedule: (Prestamo) -> Unit,
    onDetail: (Prestamo) -> Unit,
    onEdit: (Prestamo) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filtrados = remember(query, prestamos) {
        if (query.isBlank()) prestamos
        else prestamos.filter {
            it.clienteNombre.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listado de Préstamos") },
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
                label = { Text("Buscar por cliente") },
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtrados) { prestamo ->
                    LoanListItem(
                        prestamo = prestamo,
                        onSchedule = { onSchedule(prestamo) },
                        onDetail = { onDetail(prestamo) },
                        onEdit = { onEdit(prestamo) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun LoanListItem(
    prestamo: Prestamo,
    onSchedule: () -> Unit,
    onDetail: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = prestamo.clienteNombre,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.clickable(onClick = onDetail)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Monto: %.2f".format(prestamo.monto),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Plazo: ${prestamo.plazo}",
            style = MaterialTheme.typography.bodyMedium
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            IconButton(onClick = onSchedule) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Cronograma")
            }
            IconButton(onClick = onDetail) {
                Icon(Icons.Default.Visibility, contentDescription = "Detalles")
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
        }
    }
}