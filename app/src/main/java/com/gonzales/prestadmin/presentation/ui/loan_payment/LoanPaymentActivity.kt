// File: com/gonzales/prestadmin/presentation/ui/loan/LoanPaymentActivity.kt (Nuevo Archivo)

package com.gonzales.prestadmin.presentation.ui.loan_payment

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import com.gonzales.prestadmin.presentation.ui.theme.PrestAdminTheme
import com.gonzales.prestadmin.util.ThemeViewModelProvider

class LoanPaymentActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O) // Asegúrate de que las APIs de fecha estén disponibles
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeViewModel = ThemeViewModelProvider.getInstance()
        val clientId = intent.getIntExtra("CLIENT_ID", 0)
        if (clientId == 0) {
            Toast.makeText(this, "Error: ID de cliente no proporcionado.", Toast.LENGTH_LONG).show()
            finish() // Cierra la actividad si no hay ID
            return
        }
        setContent {
            val isDarkMode by themeViewModel.isDarkMode
            // Envuelve tu Composable con el tema de tu app
            PrestAdminTheme(darkTheme = isDarkMode) { // Asumo que tienes un tema definido como PrestAdminTheme
                LoanPaymentScreen(
                    clientId = clientId.toInt(), // Pasa el ID al Composable
                    onBack = { finish() } // Para cerrar esta Activity y volver a la anterior
                )
            }
        }
    }
}