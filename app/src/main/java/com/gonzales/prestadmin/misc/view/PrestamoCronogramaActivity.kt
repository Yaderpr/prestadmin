package com.gonzales.prestadmin.view

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gonzales.prestadmin.data.DarkThemePreferences
import com.gonzales.prestadmin.ui.theme.PrestAdminTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

class PaymentScheduleActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themePrefs = DarkThemePreferences(applicationContext)
        val isDark = runBlocking { themePrefs.isDarkMode.first() }

        // Recibimos los datos por Intent (duración opcional)
        val cliente = intent.getStringExtra("cliente") ?: "Cliente desconocido"
        val monto = intent.getFloatExtra("monto", 0f).toDouble()
        val interes = intent.getFloatExtra("interes", 0f).toDouble()
        val plazo = intent.getStringExtra("plazo") ?: "Mensual"
        val duration = intent.getIntExtra(
            "duration",
            when (plazo) {
                "Mensual" -> 12
                "Semanal" -> 52
                "Diario"  -> 30
                else      -> 1
            }
        )

        setContent {
            PrestAdminTheme(darkTheme = isDark) {
                PaymentScheduleScreen(
                    cliente = cliente,
                    monto = monto,
                    interesPct = interes,
                    plazo = plazo,
                    periods = duration
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PaymentScheduleScreen(
    cliente: String,
    monto: Double,
    interesPct: Double,
    plazo: String,
    periods: Int
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Cálculo base
    val i = interesPct / 100.0
    val total = monto * (1 + i)
    val qty = periods
    val installmentAmt = total / qty

    // Generar fechas de pago
    val scheduleDates = remember(plazo, periods) {
        val today = LocalDate.now()
        List(periods) { idx ->
            when (plazo) {
                "Mensual" -> today.plusMonths((idx + 1).toLong())
                "Semanal" -> today.plusWeeks((idx + 1).toLong())
                "Diario"  -> today.plusDays((idx + 1).toLong())
                else      -> today
            }
        }
    }

    // Agrupar por mes
    val byMonth = remember(scheduleDates) {
        scheduleDates.groupBy { YearMonth.from(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cronograma de Pagos") },
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
                .fillMaxSize()
        ) {
            Text("Cliente: $cliente", style = MaterialTheme.typography.titleMedium)
            Text("Total a pagar: C$ %.2f".format(total),
                style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))

            // Calendarios por mes
            LazyColumn {
                byMonth.forEach { (ym, datesInMonth) ->
                    item {
                        Text(
                            text = ym.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                                    + " ${ym.year}",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Construir celdas del mes
                    val firstDow = ym.atDay(1).dayOfWeek.value % 7 // domingo=0
                    val daysInMonth = ym.lengthOfMonth()
                    val cells = buildList<Int?> {
                        repeat(firstDow) { add(null) }
                        for (d in 1..daysInMonth) add(d)
                    }

                    item {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            userScrollEnabled = false,
                            modifier = Modifier.height(((cells.size / 7 + 1) * 40).dp)
                        ) {
                            items(cells) { day ->
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .padding(2.dp)
                                ) {
                                    if (day != null) {
                                        val date = ym.atDay(day)
                                        val isPayment = datesInMonth.contains(date)
                                        Text(
                                            text = day.toString(),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .background(
                                                    if (isPayment) Color(0xFF4CAF50) else Color.Transparent,
                                                    shape = MaterialTheme.shapes.small
                                                )
                                                .fillMaxSize()
                                                .padding(4.dp),
                                            color = if (isPayment) Color.White else LocalContentColor.current
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Lista de cuotas
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Detalle de pagos:", style = MaterialTheme.typography.titleSmall)
                }
                items(scheduleDates.size) { idx ->
                    val date = scheduleDates[idx]
                    Text(
                        "Cuota ${idx + 1}: ${date} — C$ %.2f".format(installmentAmt),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
