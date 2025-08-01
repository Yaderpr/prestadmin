// File: com/gonzales/prestadmin/presentation/activity/ReloanFormActivity.kt

package com.gonzales.prestadmin.presentation.ui.reloan

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.gonzales.prestadmin.presentation.ui.theme.PrestAdminTheme
import com.gonzales.prestadmin.util.ThemeViewModelProvider

class ReloanFormActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeViewModel = ThemeViewModelProvider.getInstance()
        setContent {
            val isDarkMode by themeViewModel.isDarkMode
            PrestAdminTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ReloanFormScreen(
                        onGuardarReloan = { /*reloanRequest,*/ evaluation, guarantees ->
                            // Aquí iría la lógica final para guardar el represtamo
                            Toast.makeText(applicationContext, "Represtamo guardado con éxito!", Toast.LENGTH_SHORT).show()
                           // println("Reloan Request: $reloanRequest")
                            println("Evaluation: $evaluation")
                            println("Guarantees: $guarantees")

                            finish() // Cierra la actividad después de guardar
                        }
                    )
                }
            }
        }
    }
}