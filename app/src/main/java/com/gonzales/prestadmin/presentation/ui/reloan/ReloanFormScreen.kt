// File: com/gonzales/prestadmin/presentation/ui/reloan/ReloanFormScreen.kt

package com.gonzales.prestadmin.presentation.ui.reloan

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gonzales.prestadmin.domain.model.evaluation.Evaluation

import com.gonzales.prestadmin.domain.model.guarantee.Guarantee
import com.gonzales.prestadmin.presentation.ui.client.EvaluationSection
import com.gonzales.prestadmin.presentation.ui.client.GuaranteeSection
import com.gonzales.prestadmin.presentation.viewmodel.client.EvaluationViewModel
import com.gonzales.prestadmin.presentation.viewmodel.client.GuaranteeViewModel
import com.gonzales.prestadmin.presentation.viewmodel.client.LoanRequestViewModel
import com.gonzales.prestadmin.util.AppViewModelFactory

@SuppressLint("ContextCastToActivity")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReloanFormScreen(
    onGuardarReloan: (
        Evaluation?,
        List<Guarantee>?
    ) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    var selectedSection by remember { mutableIntStateOf(0) }
    // Nombres de las secciones para el flujo de Represtamo
    val sections = listOf("Solicitud de Represtamo", "Evaluación", "Garantías")

    // --- Instanciar y recordar los ViewModels de cada sección ---
    // Usamos AppViewModelFactory para inyectar las dependencias de los ViewModels
    val loanRequestViewModel: LoanRequestViewModel = viewModel(factory = AppViewModelFactory())
    val evaluationViewModel: EvaluationViewModel = viewModel(factory = AppViewModelFactory())
    val guaranteeViewModel: GuaranteeViewModel = viewModel(factory = AppViewModelFactory())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Represtamo") },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    // Validar cada sección ANTES de guardar
                    //val isLoanRequestValid = loanRequestViewModel.validateLoanRequest()
                   // val isEvaluationValid = evaluationViewModel.validateEvaluationSection()
                    val isGuaranteesValid = guaranteeViewModel.validateGuarantees()

                    if (/*isLoanRequestValid &&isEvaluationValid && */isGuaranteesValid) {
                        onGuardarReloan(
                            //loanRequestViewModel.getLoanRequest(),
                            evaluationViewModel.getEvaluation(),
                            guaranteeViewModel.getGuaranteeList()
                        )
                    } else {
                        Toast.makeText(
                            activity,
                            "Corregí los errores en el formulario.",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Opcional: Cambiar a la primera sección con errores para guiar al usuario
                        if (true/*!isLoanRequestValid*/) selectedSection = 0
                        else if (true/*!isEvaluationValid*/) selectedSection = 1
                        else if (!isGuaranteesValid) selectedSection = 2
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Guardar Represtamo")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            // Chips de navegación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()) // Permite que los chips se desplacen horizontalmente
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

            // Contenido de la sección seleccionada
            // NOTA: No hay verticalScroll aquí, porque cada sección se encarga de su propio scroll si lo necesita
            when (selectedSection) {
                0 -> ReloanLoanRequestSection()
                1 -> EvaluationSection(viewModel = evaluationViewModel)
                2 -> GuaranteeSection(viewModel = guaranteeViewModel)
            }
        }
    }
}