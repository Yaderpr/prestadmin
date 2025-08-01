package com.gonzales.prestadmin.presentation.ui.main

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gonzales.prestadmin.domain.model.client.ClienteUI
import com.gonzales.prestadmin.presentation.ui.loan_payment.LoanPaymentActivity
import com.gonzales.prestadmin.presentation.viewmodel.main.ActiveClientListSectionViewModel
import kotlinx.datetime.toJavaLocalDate
import java.time.LocalDate
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActiveClientListSection(
    activeClientListSectionViewModel: ActiveClientListSectionViewModel = viewModel(),
    totalClientsValue: String = "0",
    recaudadoValue: String = "0.0",
    moraValue: String = "0.0",
    isDarkTheme: Boolean
) {
    var query by remember { mutableStateOf("") }
    val clientList by activeClientListSectionViewModel.clients.collectAsState()
    val isLoading by activeClientListSectionViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    val pullRefreshState = rememberPullToRefreshState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        PullToRefreshBox(
            state = pullRefreshState,
            isRefreshing = isLoading,
            onRefresh = { activeClientListSectionViewModel.loadClientsWithLoans() }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ResumenCard(
                        icon = Icons.Default.Group,
                        titulo = "Clientes",
                        valor = totalClientsValue,
                        isDarkTheme = isDarkTheme
                    )
                }
                item {
                    ResumenCard(
                        icon = Icons.Default.AttachMoney,
                        titulo = "Recaudado",
                        valor = recaudadoValue,
                        isDarkTheme = isDarkTheme
                    )
                }
                item {
                    ResumenCard(
                        icon = Icons.Default.Warning,
                        titulo = "Mora",
                        valor = moraValue,
                        isDarkTheme = isDarkTheme
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Mis Clientes", style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(
                            value = query,
                            onValueChange = {
                                query = it
                                activeClientListSectionViewModel.searchClients(it)
                            },
                            label = { Text("Buscar abonado, nombre o apellido") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(MaterialTheme.colorScheme.onSurface)
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Código",
                                modifier = Modifier.width(80.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.surface
                            )
                            Text(
                                "Nombres",
                                modifier = Modifier.width(120.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.surface
                            )
                            Text(
                                "Apellidos",
                                modifier = Modifier.width(120.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.surface
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }

                if (clientList.isEmpty()) {
                    item {
                        Text(
                            "No se encontraron resultados.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    items(clientList) { client ->
                        val backgroundColor = getRowColor(client)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(backgroundColor, shape = MaterialTheme.shapes.extraSmall)
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .clickable {
                                    val intent = Intent(context, LoanPaymentActivity::class.java)
                                    intent.putExtra("CLIENT_ID", client.id)
                                    context.startActivity(intent)
                                },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("P_${client.codigoPrestamo}", modifier = Modifier.width(80.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                            Text(client.nombre, modifier = Modifier.width(120.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                            Text(client.apellido, modifier = Modifier.width(120.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun getRowColor(client: ClienteUI): Color {
    val hoy = LocalDate.now()

    return when {
        // Condition for RED: The due date has passed
        hoy.isAfter(client.fechaVencimiento.toJavaLocalDate()) -> Color(0xFFFF0000)

        // Condition for GREEN: It's a payment day
        client.modality == "Diario" -> {
            // Check if it's a weekday and after the first payment date
            if (hoy.dayOfWeek != DayOfWeek.SATURDAY && hoy.dayOfWeek != DayOfWeek.SUNDAY &&
                !hoy.isBefore(client.fechaPrimeraCuota.toJavaLocalDate())) {
                Color(0xFF4CAF50)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        }
        client.modality == "Semanal" -> {
            val weeklyPaymentDay = when(client.dayOfWeek) {
                "Lunes" -> DayOfWeek.MONDAY
                "Martes" -> DayOfWeek.TUESDAY
                "Miércoles" -> DayOfWeek.WEDNESDAY
                "Jueves" -> DayOfWeek.THURSDAY
                "Viernes" -> DayOfWeek.FRIDAY
                else -> null
            }
            if (hoy.dayOfWeek == weeklyPaymentDay) {
                Color(0xFF4CAF50)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        }
        client.modality == "Mensual" -> {
            if (hoy.dayOfMonth == client.monthlyPaymentDay &&
                !hoy.isBefore(client.fechaPrimeraCuota.toJavaLocalDate())) {
                Color(0xFF4CAF50)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        }

        // Default condition: No payment due today and not overdue
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
}