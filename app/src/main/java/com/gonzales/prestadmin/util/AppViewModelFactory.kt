// Archivo: SoloUnArchivoDeEjemplo.kt (o el nombre de tu Activity/Composable principal para la prueba)
// Asegúrate de cambiar esto a tu paquete real
package com.gonzales.prestadmin.util


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gonzales.prestadmin.App
import com.gonzales.prestadmin.presentation.viewmodel.client.EvaluationViewModel
import com.gonzales.prestadmin.presentation.viewmodel.client.GuaranteeViewModel
import com.gonzales.prestadmin.presentation.viewmodel.client.LoanRequestViewModel
import com.gonzales.prestadmin.presentation.viewmodel.login.LoginActivityViewModel
import com.gonzales.prestadmin.presentation.viewmodel.main.ActiveClientListSectionViewModel
import com.gonzales.prestadmin.presentation.viewmodel.main.MainActivityViewModel
import com.gonzales.prestadmin.presentation.viewmodel.settings.EditProfileViewModel
import com.gonzales.prestadmin.presentation.viewmodel.settings.SettingsActivityViewModel
import com.gonzales.prestadmin.presentation.viewmodel.theme.ThemeViewModel

class AppViewModelFactory : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ActiveClientListSectionViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ActiveClientListSectionViewModel() as T}
            modelClass.isAssignableFrom(EvaluationViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                EvaluationViewModel() as T // EvaluationViewModel no necesita repositorios
            }
            modelClass.isAssignableFrom(GuaranteeViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                GuaranteeViewModel() as T
            }
            modelClass.isAssignableFrom(LoanRequestViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                LoanRequestViewModel(/*AppContainer.clientRepository*/) as T
            }
            modelClass.isAssignableFrom(LoginActivityViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                LoginActivityViewModel(App.userRepository) as T
            }
            modelClass.isAssignableFrom(SettingsActivityViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                SettingsActivityViewModel(App.userRepository) as T
            }
            modelClass.isAssignableFrom(EditProfileViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                EditProfileViewModel(App.userRepository) as T
            }
            modelClass.isAssignableFrom(MainActivityViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                MainActivityViewModel(App.userRepository) as T
            }
            modelClass.isAssignableFrom(ThemeViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")

                ThemeViewModel(App.themeManager, App.systemDarkMode) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}