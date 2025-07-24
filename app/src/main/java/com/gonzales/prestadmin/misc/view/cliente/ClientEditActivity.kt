package com.gonzales.prestadmin.view.cliente

import android.app.Activity
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class EditClientActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themePrefs = DarkThemePreferences(applicationContext)
        val isDark = runBlocking { themePrefs.isDarkMode.first() }

        val clienteNombres = intent.getStringExtra("cliente_nombres") ?: ""
        val clienteApellidos = intent.getStringExtra("cliente_apellidos") ?: ""
        val clienteTelefono = intent.getStringExtra("cliente_telefono") ?: ""
        val clienteDireccion = intent.getStringExtra("cliente_direccion") ?: ""
        val clienteCedula = intent.getStringExtra("cliente_cedula") ?: ""

        setContent {
            PrestAdminTheme(darkTheme = isDark) {
                EditClientScreen(
                    initialNombres = clienteNombres,
                    initialApellidos = clienteApellidos,
                    initialTelefono = clienteTelefono,
                    initialDireccion = clienteDireccion,
                    initialCedula = clienteCedula
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditClientScreen(
    initialNombres: String,
    initialApellidos: String,
    initialTelefono: String,
    initialDireccion: String,
    initialCedula: String
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var nombres by remember { mutableStateOf(initialNombres) }
    var apellidos by remember { mutableStateOf(initialApellidos) }
    var telefono by remember { mutableStateOf(initialTelefono) }
    var direccion by remember { mutableStateOf(initialDireccion) }
    var cedula by remember { mutableStateOf(initialCedula) }

    var errores by remember { mutableStateOf(emptyMap<String, String>()) }

    fun validar(): Boolean {
        val nuevosErrores = mutableMapOf<String, String>()

        if (!nombres.matches(Regex("^[\\p{L} .'-]+$")))
            nuevosErrores["nombres"] = "Nombre inválido"

        if (!apellidos.matches(Regex("^[\\p{L} .'-]+$")))
            nuevosErrores["apellidos"] = "Apellido inválido"

        if (!telefono.matches(Regex("^\\d{8,}$")))
            nuevosErrores["telefono"] = "Teléfono debe tener al menos 8 dígitos"

        if (!cedula.matches(Regex("^(\\d{3}-\\d{6}-\\d{5}|[a-zA-Z0-9]{14})$")))
            nuevosErrores["cedula"] = "Formato de cédula inválido"

        errores = nuevosErrores
        return nuevosErrores.isEmpty()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Cliente") },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = nombres,
                onValueChange = { nombres = it },
                label = { Text("Nombres") },
                isError = errores.containsKey("nombres"),
                modifier = Modifier.fillMaxWidth()
            )
            errores["nombres"]?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = { Text("Apellidos") },
                isError = errores.containsKey("apellidos"),
                modifier = Modifier.fillMaxWidth()
            )
            errores["apellidos"]?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                isError = errores.containsKey("telefono"),
                modifier = Modifier.fillMaxWidth()
            )
            errores["telefono"]?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = cedula,
                onValueChange = { cedula = it },
                label = { Text("Cédula") },
                isError = errores.containsKey("cedula"),
                modifier = Modifier.fillMaxWidth()
            )
            errores["cedula"]?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (validar()) {
                        // Aquí se guardarán los datos actualizados o se enviarán a la API
                        activity?.finish()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }
        }
    }
}
