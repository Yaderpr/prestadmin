package com.gonzales.prestadmin.presentation.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gonzales.prestadmin.App
import com.gonzales.prestadmin.R
import com.gonzales.prestadmin.presentation.ui.main.MainActivity
import com.gonzales.prestadmin.presentation.ui.theme.PrestAdminTheme
import com.gonzales.prestadmin.presentation.viewmodel.login.LoginActivityViewModel
import com.gonzales.prestadmin.presentation.viewmodel.login.LoginState
import com.gonzales.prestadmin.util.AppViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val sessionManager = App.sessionManager

        // Verifica sesión activa desde SessionManager
        lifecycleScope.launch {
            val isActive = sessionManager.isSessionActiveFlow.first()
            if (isActive) {
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
                return@launch
            }

            // Mostrar Login UI
            setContent {
                PrestAdminTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        LoginScreen()
                    }
                }
            }
        }
    }
}


@Composable
fun LoginScreen(
    loginActivityViewModel: LoginActivityViewModel = viewModel(factory = AppViewModelFactory())
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val loginState by loginActivityViewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.Success -> {
                Toast.makeText(context, "Bienvenido ${state.user.firstname}", Toast.LENGTH_SHORT).show()
                context.startActivity(Intent(context, MainActivity::class.java))
                (context as? Activity)?.finish()
                loginActivityViewModel.resetState()
            }
            is LoginState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                loginActivityViewModel.resetState()
            }
            else -> Unit
        }
    }

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
                    loginActivityViewModel.login(username, password)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = loginState !is LoginState.Loading
            ) {
                Text(if (loginState is LoginState.Loading) "Iniciando..." else "Iniciar sesión")
            }
        }
    }
}




