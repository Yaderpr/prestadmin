package com.gonzales.prestadmin.presentation.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gonzales.prestadmin.presentation.ui.theme.PrestAdminTheme
import com.gonzales.prestadmin.presentation.viewmodel.settings.EditProfileViewModel
import com.gonzales.prestadmin.util.AppViewModelFactory
import com.gonzales.prestadmin.util.ThemeViewModelProvider
import com.gonzales.prestadmin.util.UserIcon

class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeViewModel = ThemeViewModelProvider.getInstance()
        setContent {
            val isDarkMode by themeViewModel.isDarkMode
            PrestAdminTheme(darkTheme = isDarkMode) {
                EditProfileScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(viewModel: EditProfileViewModel = viewModel(factory = AppViewModelFactory())) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Pasamos la URI al ViewModel
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.onPhotoSelected(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar perfil") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            UserIcon(
                imageUri = uiState.fotoUri.toString(),
                onClick = { imagePickerLauncher.launch("image/*") },
                size = 128.dp
            )

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.onSaveChanges(context) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(if (uiState.isSaving) "Guardando..." else "Guardar cambios")
                if (uiState.successMessage?.isNotEmpty() == true) {
                    Toast.makeText(context, uiState.successMessage, Toast.LENGTH_SHORT).show()
                    (context as? ComponentActivity)?.finish()
                }
            }
        }
    }
}