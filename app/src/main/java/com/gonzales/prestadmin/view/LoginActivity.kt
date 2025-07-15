package com.gonzales.prestadmin.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.gonzales.prestadmin.R
import com.gonzales.prestadmin.data.SessionPreferences
import com.gonzales.prestadmin.ui.theme.PrestAdminTheme
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    private lateinit var sesionPrefs: SessionPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        sesionPrefs = SessionPreferences(applicationContext)

        // Comprobar si la sesión está activa con DataStore
        lifecycleScope.launch {
            if (sesionPrefs.estaSesionActiva()) {
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
                return@launch
            }

            // Mostrar UI si no hay sesión activa
            setContent {
                PrestAdminTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        LoginScreen(sesionPrefs)
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(sesionPrefs: SessionPreferences) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_splash_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(300.dp, 150.dp)
                    .padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuario") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (username == "admin" && password == "1234") {
                        println("Inicio de sesión exitoso")
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)

                        // Guardar sesión en DataStore
                        (context as? ComponentActivity)?.lifecycleScope?.launch {
                            sesionPrefs.guardarSesionActiva(true, username)
                        }

                        (context as? Activity)?.finish()
                    } else {
                        errorMessage = "Credenciales incorrectas"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar sesión")
            }

            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = msg, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}



