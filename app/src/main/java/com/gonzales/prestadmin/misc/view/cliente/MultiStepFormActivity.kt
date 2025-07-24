package com.gonzales.prestadmin.view

import MultiStepFormScreen
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.gonzales.prestadmin.data.DarkThemePreferences
import com.gonzales.prestadmin.ui.theme.PrestAdminTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MultiStepFormActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isDark = runBlocking { DarkThemePreferences(applicationContext).isDarkMode.first() }
        setContent {
            PrestAdminTheme(darkTheme = isDark) {
                MultiStepFormScreen { clienteData, prestamoData, evaluacionData, garantias ->
                    // TODO: persistir todo
                }
            }
        }
    }
}
