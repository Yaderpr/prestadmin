package com.gonzales.prestadmin.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gonzales.prestadmin.data.DarkThemePreferences
import com.gonzales.prestadmin.data.UsuarioPrefs
import com.gonzales.prestadmin.repository.UserRepository
import com.gonzales.prestadmin.ui.component.UserIcon
import com.gonzales.prestadmin.ui.theme.PrestAdminTheme
import com.gonzales.prestadmin.viewmodel.UserUiModel
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    private lateinit var darkThemePrefs: DarkThemePreferences
    private lateinit var usuarioPrefs: UsuarioPrefs
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        darkThemePrefs = DarkThemePreferences(applicationContext)
        usuarioPrefs = UsuarioPrefs(applicationContext)
        val userRepository = UserRepository(usuarioPrefs)

        setContent {
            val isDarkMode by darkThemePrefs.isDarkMode.collectAsState(initial = false)

            PrestAdminTheme(darkTheme = isDarkMode) {
                SettingsScreen(userRepository, darkThemePrefs)
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(userRepository: UserRepository, darkThemePrefs: DarkThemePreferences) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    val context = LocalContext.current
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
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
                .verticalScroll(rememberScrollState())
        ) {
            UserProfileSection(userRepository)
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            GeneralPreferencesSection(darkThemePrefs)
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            AboutAppSection()
        }
    }
}
@Composable
fun UserProfileSection(userRepository: UserRepository) {
    val userUiModel by userRepository.userFlow.collectAsState(initial = UserUiModel("Cargando...", null))
    val context = LocalContext.current
    Text("Perfil del Usuario", style = MaterialTheme.typography.titleMedium)

    Spacer(Modifier.height(12.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        UserIcon(
            fotoUri = userUiModel.fotoUri,
            onClick = { /* abrir selector de imagen */ },
            modifier = Modifier.size(64.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column {
            Text(text = "Nombre: Juan Pérez")
            Text(text = "Rol: Administrador")
            TextButton(onClick = { context.startActivity(Intent(context, EditProfileActivity::class.java))}) {
                Text("Editar perfil")
            }
        }
    }
}
@Composable
fun GeneralPreferencesSection(darkThemePrefs: DarkThemePreferences) {
    Text("Preferencias Generales", style = MaterialTheme.typography.titleMedium)

    Spacer(Modifier.height(12.dp))

    val isDarkMode by darkThemePrefs.isDarkMode.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Tema oscuro", modifier = Modifier.weight(1f))
        Switch(
            checked = isDarkMode,
            onCheckedChange = { newValue ->
                scope.launch {
                    darkThemePrefs.setDarkMode(newValue)
                }
            }
        )
    }
}


@Composable
fun AboutAppSection() {
    Text("Acerca de la app", style = MaterialTheme.typography.titleMedium)

    Spacer(Modifier.height(12.dp))

    Text("Versión: 1.0.0")
    Text("Desarrollador: Yader Pineda") // Puedes poner tu nombre o el de tu empresa
}


