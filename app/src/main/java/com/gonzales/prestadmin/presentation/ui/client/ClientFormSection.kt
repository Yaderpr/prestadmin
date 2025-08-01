package com.gonzales.prestadmin.presentation.ui.client

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gonzales.prestadmin.presentation.viewmodel.client.ClientFormViewModel
import com.gonzales.prestadmin.util.createImageUri // Asegúrate de que esta función está en tu proyecto


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFormSection(
    viewModel: ClientFormViewModel = viewModel()
) {
    val context = LocalContext.current

    // --- Observar los estados del ViewModel ---
    val firstName by viewModel.firstName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val address by viewModel.address.collectAsState()
    val identificationNumber by viewModel.identificationNumber.collectAsState()
    val fotoFrontalBitmap by viewModel.fotoFrontalBitmap.collectAsState()
    val fotoReversaBitmap by viewModel.fotoReversaBitmap.collectAsState()
    val nameFrontal by viewModel.nameFrontal.collectAsState()
    val nameReversa by viewModel.nameReversa.collectAsState()

    // --- Observar los estados de error del ViewModel ---
    val errorFirstName by viewModel.errorFirstName.collectAsState()
    val errorLastName by viewModel.errorLastName.collectAsState()
    val errorPhoneNumber by viewModel.errorPhoneNumber.collectAsState()
    val errorAddress by viewModel.errorAddress.collectAsState()
    val errorIdentificationNumber by viewModel.errorIdentificationNumber.collectAsState()
    val errorDniFrontal by viewModel.errorDniFrontal.collectAsState()
    val errorDniReversa by viewModel.errorDniReversa.collectAsState()

    // --- Estados UI locales (no persisten en el ViewModel) ---
    var showDialog by remember { mutableStateOf(false) }
    var isFrontalDialog by remember { mutableStateOf(true) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }


    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            try {
                val bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, tempPhotoUri)
                if (isFrontalDialog) {
                    viewModel.updateFotoFrontal(bmp, tempPhotoUri)
                } else {
                    viewModel.updateFotoReversa(bmp, tempPhotoUri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                if (isFrontalDialog) {
                    viewModel.updateFotoFrontal(bmp, it)
                } else {
                    viewModel.updateFotoReversa(bmp, it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Seleccionar fuente") },
            text = { Text("¿Cámara o Galería?") },
            confirmButton = {
                TextButton(onClick = {
                    tempPhotoUri = createImageUri(context)
                    tempPhotoUri?.let { cameraLauncher.launch(it) }
                    showDialog = false
                }) { Text("Cámara") }
            },
            dismissButton = {
                TextButton(onClick = {
                    galleryLauncher.launch("image/*")
                    showDialog = false
                }) { Text("Galería") }
            }
        )
    }

    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Campos de texto (mantienen la misma estructura)
        OutlinedTextField(
            value = firstName,
            onValueChange = viewModel::updateFirstName,
            label = { Text("Nombres") },
            isError = errorFirstName != null,
            supportingText = {
                errorFirstName?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = lastName,
            onValueChange = viewModel::updateLastName,
            label = { Text("Apellidos") },
            isError = errorLastName != null,
            supportingText = {
                errorLastName?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = viewModel::updatePhoneNumber,
            label = { Text("Teléfono") },
            isError = errorPhoneNumber != null,
            supportingText = {
                errorPhoneNumber?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        OutlinedTextField(
            value = address,
            onValueChange = viewModel::updateAddress,
            label = { Text("Dirección") },
            isError = errorAddress != null,
            supportingText = {
                errorAddress?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )
        OutlinedTextField(
            value = identificationNumber,
            onValueChange = viewModel::updateIdentificationNumber,
            label = { Text("Numero de cédula") },
            isError = errorIdentificationNumber != null,
            supportingText = {
                errorIdentificationNumber?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Sección de Foto frontal del DNI
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Foto de cédula frontal:")
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    isFrontalDialog = true
                    showDialog = true
                }) { Text("Seleccionar") }
            }
            // Mostrar errores y vista previa si existen
            errorDniFrontal?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            nameFrontal?.let { Text("Archivo: $it", Modifier.padding(start = 16.dp)) }
            fotoFrontalBitmap?.let {
                Image(it.asImageBitmap(), contentDescription = "Foto Frontal", Modifier.size(100.dp))
            }
        }

        // Sección de Foto reversa del DNI
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Foto de cédula reversa:")
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    isFrontalDialog = false
                    showDialog = true
                }) { Text("Seleccionar") }
            }
            // Mostrar errores y vista previa si existen
            errorDniReversa?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            nameReversa?.let { Text("Archivo: $it", Modifier.padding(start = 16.dp)) }
            fotoReversaBitmap?.let {
                Image(it.asImageBitmap(), contentDescription = "Foto Reversa", Modifier.size(100.dp))
            }
        }
    }
}