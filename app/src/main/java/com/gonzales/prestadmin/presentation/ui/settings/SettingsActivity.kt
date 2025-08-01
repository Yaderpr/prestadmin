package com.gonzales.prestadmin.presentation.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gonzales.prestadmin.presentation.ui.theme.PrestAdminTheme
import com.gonzales.prestadmin.presentation.viewmodel.settings.SettingsActivityViewModel
import com.gonzales.prestadmin.presentation.viewmodel.theme.ThemeViewModel
import com.gonzales.prestadmin.util.AppViewModelFactory
import com.gonzales.prestadmin.util.ThemeViewModelProvider
import com.gonzales.prestadmin.util.UserIcon

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themeViewModel = ThemeViewModelProvider.getInstance()
        setContent {
            val isDarkMode by themeViewModel.isDarkMode
            PrestAdminTheme(darkTheme = isDarkMode) {
                SettingsScreen()
            }
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {

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
            UserProfileSection()
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            GeneralPreferencesSection()
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            AboutAppSection()
        }
    }
}
// Dentro de SettingsScreen, el resto del código se mantiene igual...

@Composable
fun UserProfileSection(viewModel: SettingsActivityViewModel = viewModel(factory = AppViewModelFactory())) {
    // Observamos el StateFlow del ViewModel
    val userUiModel by viewModel.user.collectAsState()
    val context = LocalContext.current

    Text("Perfil del Usuario", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Reemplazamos la lógica para usar la URL de la foto del modelo
        val photoUrl = userUiModel.photoUrl
        print("User actual desde UserProfile en Settings: $userUiModel\n")
        UserIcon(
            // La fotoUri ahora es la URL remota que obtuvimos de Supabase
            imageUri = photoUrl?.toUri().toString(),
            onClick = { context.startActivity(Intent(context, EditProfileActivity::class.java)) },
            size = 64.dp
        )

        Spacer(Modifier.width(12.dp))

        Column {
            Text(text = "Nombre: ${userUiModel.fullName}")
            Text(text = "Usuario: ${userUiModel.username}")
            Text(text = "Rol: ${userUiModel.role}")
            TextButton(
                onClick = { context.startActivity(Intent(context, EditProfileActivity::class.java)) }
            ) {
                Text("Editar perfil")
            }
        }
    }
}
@Composable
fun GeneralPreferencesSection(themeViewModel: ThemeViewModel = ThemeViewModelProvider.getInstance()) { // Ahora recibe ThemeViewModel
    Text("Preferencias Generales", style = MaterialTheme.typography.titleMedium)

    Spacer(Modifier.height(12.dp))

    // ¡Aquí es donde obtenemos el estado del Switch!
    // Directamente del ThemeViewModel, que ya hizo todo el trabajo de combinación.
    val isDarkModeChecked by themeViewModel.isDarkMode

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Tema oscuro", modifier = Modifier.weight(1f))
        Switch(
            checked = isDarkModeChecked, // Usamos el estado efectivo del ViewModel
            onCheckedChange = {
                // Cuando el usuario cambia el Switch, le decimos al ViewModel
                // que actualice la preferencia del usuario.
                themeViewModel.toggleDarkMode()
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

