package com.gonzales.prestadmin.util

import com.gonzales.prestadmin.App
import com.gonzales.prestadmin.presentation.viewmodel.theme.ThemeViewModel

object ThemeViewModelProvider {
    private var instance: ThemeViewModel? = null

    fun getInstance(): ThemeViewModel {
        if (instance == null) {
            instance = ThemeViewModel(App.themeManager, App.systemDarkMode)
        }
        return instance!!
    }
}
