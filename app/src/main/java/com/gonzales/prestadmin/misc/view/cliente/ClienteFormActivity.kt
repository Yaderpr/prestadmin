package com.gonzales.prestadmin.view.cliente

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gonzales.prestadmin.data.DarkThemePreferences
import com.gonzales.prestadmin.viewmodel.Cliente
import com.gonzales.prestadmin.ui.theme.PrestAdminTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ClienteFormActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themePrefs = DarkThemePreferences(applicationContext)
        val isDark = runBlocking { themePrefs.isDarkMode.first() }

        setContent {
            PrestAdminTheme(darkTheme = isDark) {
                ClienteFormScreen(onSave = { cliente ->
                    // Aquí puedes guardar el cliente en una base de datos o mostrarlo temporalmente
                    Toast.makeText(applicationContext, "Cliente guardado: ${cliente.nombres}", Toast.LENGTH_SHORT).show()
                    finish()
                })
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteFormScreen(onSave: (Cliente) -> Unit) {
    val context = LocalContext.current

    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }

    fun validar(): Boolean {
        val regexNombre = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+\$")
        val regexTelefono = Regex("^\\d{8,}\$")
        val regexCedula = Regex("^\\d{3}-\\d{6}-\\d{5}\$|^\\w{13,}\$")

        return when {
            !regexNombre.matches(nombres) -> {
                error = "Nombre inválido"
                false
            }
            !regexNombre.matches(apellidos) -> {
                error = "Apellido inválido"
                false
            }
            !regexTelefono.matches(telefono) -> {
                error = "Teléfono inválido"
                false
            }
            !regexCedula.matches(cedula) -> {
                error = "Cédula inválida"
                false
            }
            else -> {
                error = null
                true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Datos del Cliente") },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = nombres,
                onValueChange = { nombres = it },
                label = { Text("Nombres") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = { Text("Apellidos") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            OutlinedTextField(
                value = cedula,
                onValueChange = { cedula = it },
                label = { Text("Cédula") },
                modifier = Modifier.fillMaxWidth()
            )

            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    if (validar()) {
                        onSave(Cliente(nombres, apellidos, telefono, direccion, cedula))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
}



