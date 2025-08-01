package com.gonzales.prestadmin.presentation.ui.client

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gonzales.prestadmin.presentation.ui.theme.PrestAdminTheme
import com.gonzales.prestadmin.presentation.viewmodel.client.ClientFormScreenViewModel
import com.gonzales.prestadmin.presentation.viewmodel.client.ClientFormViewModel
import com.gonzales.prestadmin.presentation.viewmodel.client.EvaluationViewModel
import com.gonzales.prestadmin.presentation.viewmodel.client.GuaranteeViewModel
import com.gonzales.prestadmin.presentation.viewmodel.client.LoanRequestViewModel
import com.gonzales.prestadmin.presentation.viewmodel.client.SaveState
import com.gonzales.prestadmin.util.AppViewModelFactory
import com.gonzales.prestadmin.util.ThemeViewModelProvider
import kotlinx.coroutines.launch

class ClientFormActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeViewModel = ThemeViewModelProvider.getInstance()
        setContent {
            val isDarkMode by themeViewModel.isDarkMode
            PrestAdminTheme(darkTheme = isDarkMode) {
                ClientFormScreen()
            }
        }
    }
}

@SuppressLint("ContextCastToActivity")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFormScreen() {
    val activity = LocalContext.current as? ComponentActivity
    val coroutineScope = rememberCoroutineScope()
    var selectedSection by remember { mutableIntStateOf(0) }
    val sections = listOf("Datos personales", "Solicitud de préstamo", "Evaluación", "Garantías")

    // --- Instanciar ViewModels ---
    val clientFormViewModel: ClientFormViewModel = viewModel()
    val loanRequestViewModel: LoanRequestViewModel = viewModel()
    val evaluationViewModel: EvaluationViewModel = viewModel()
    val guaranteeViewModel: GuaranteeViewModel = viewModel()

    // ¡El nuevo ViewModel orquestador!
    val orchestratorViewModel: ClientFormScreenViewModel = viewModel()
    val saveState by orchestratorViewModel.saveState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de clientes") },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                Button(
                    onClick = {
                        // Validar cada sección ANTES de guardar
                        val isClientDetailsValid = clientFormViewModel.validateAllFields()
                        val isLoanRequestValid = loanRequestViewModel.validateAllFields()
                        val isEvaluationValid = evaluationViewModel.validateAllFields()
                        val isGuaranteesValid = guaranteeViewModel.validateGuarantees()

                        if (isClientDetailsValid && isLoanRequestValid && isEvaluationValid && isGuaranteesValid) {
                            // Lanzar la coroutine para guardar de forma asíncrona
                            coroutineScope.launch {
                                // Recolectar todos los datos de los ViewModels de sección
                                val clientAndDniPhotos = clientFormViewModel.getClient()
                                val loan = loanRequestViewModel.getLoan()
                                val evaluation = evaluationViewModel.getEvaluation()
                                val guarantees = guaranteeViewModel.getGuaranteeList()

                                // Llamar al método del ViewModel orquestador para guardar todo
                                orchestratorViewModel.saveFullClientForm(
                                    clientAndDniPhotos = clientAndDniPhotos,
                                    loan = loan,
                                    evaluation = evaluation,
                                    guarantees = guarantees
                                )
                            }
                        } else {
                            Toast.makeText(activity, "Corrija los errores en el formulario.", Toast.LENGTH_SHORT).show()
                            // Cambiar a la primera sección con errores para guiar al usuario
                            if (!isClientDetailsValid) selectedSection = 0
                            else if (!isLoanRequestValid) selectedSection = 1
                            else if (!isEvaluationValid) selectedSection = 2
                            else if (!isGuaranteesValid) selectedSection = 3
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    enabled = saveState !is SaveState.Loading // Deshabilitar el botón mientras se guarda
                ) {
                    when (saveState) {
                        is SaveState.Loading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.height(24.dp))
                        is SaveState.Success -> Text("Guardado!")
                        is SaveState.Error -> Text("Error")
                        else -> Text("Guardar todo")
                    }
                }

                // Manejar los efectos secundarios de los estados
                LaunchedEffect(saveState) {
                    when (saveState) {
                        is SaveState.Success -> {
                            Toast.makeText(activity, "Cliente guardado con éxito!", Toast.LENGTH_SHORT).show()
                            activity?.finish()
                            orchestratorViewModel.resetSaveState()
                        }
                        is SaveState.Error -> {
                            Toast.makeText(activity, "Error: ${(saveState as SaveState.Error).message}", Toast.LENGTH_SHORT).show()
                            orchestratorViewModel.resetSaveState() // Resetear el estado para reintentar
                        }
                        else -> Unit
                    }
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sections.forEachIndexed { index, title ->
                    FilterChip(
                        selected = selectedSection == index,
                        onClick = { selectedSection = index },
                        label = { Text(title) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            when (selectedSection) {
                0 -> ClientFormSection(viewModel = clientFormViewModel)
                1 -> LoanRequestSection(viewModel = loanRequestViewModel)
                2 -> EvaluationSection(viewModel = evaluationViewModel)
                3 -> GuaranteeSection(viewModel = guaranteeViewModel)
            }
        }
    }
}