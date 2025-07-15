package com.gonzales.prestadmin.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.gonzales.prestadmin.data.DarkThemePreferences
import com.gonzales.prestadmin.data.UsuarioPrefs
import com.gonzales.prestadmin.repository.UserRepository
import com.gonzales.prestadmin.ui.theme.PrestAdminTheme
import com.gonzales.prestadmin.viewmodel.UserUiModel
import kotlinx.coroutines.launch

class EditProfileActivity : ComponentActivity() {
    private lateinit var usuarioPrefs: UsuarioPrefs
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usuarioPrefs = UsuarioPrefs(applicationContext)
        val darkThemePrefs = DarkThemePreferences(applicationContext)
        val userRepository = UserRepository(usuarioPrefs)

        setContent {
            val isDarkMode by darkThemePrefs.isDarkMode.collectAsState(initial = false)

            PrestAdminTheme(darkTheme = isDarkMode) {
                EditProfileScreen(userRepository, usuarioPrefs)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(userRepository: UserRepository, userPrefs: UsuarioPrefs) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val userUiModel by userRepository.userFlow.collectAsState(
        initial = UserUiModel(nombre = "Cargando...", fotoUri = null)
    )

    var nombre by remember { mutableStateOf(userUiModel.nombre) }
    var fotoUriOriginal by remember { mutableStateOf<Uri?>(null) }
    var fotoUriEditando by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(userUiModel) {
        nombre = userUiModel.nombre
        val uri = userUiModel.fotoUri?.toUri()
        fotoUriOriginal = uri
        fotoUriEditando = uri
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            fotoUriEditando = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar perfil") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen
            if (fotoUriEditando != null) {
                AsyncImage(
                    model = fotoUriEditando,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(128.dp)
                        .clickable { imagePickerLauncher.launch("image/*") }
                )
            } else {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .size(128.dp)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        val cambios = nombre != userUiModel.nombre ||
                                fotoUriEditando?.toString() != userUiModel.fotoUri

                        if (cambios) {
                            userPrefs.guardarNombre(nombre)
                            userPrefs.guardarFotoUri(fotoUriEditando?.toString())
                        }

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
