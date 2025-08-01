package com.gonzales.prestadmin.domain.model.user
data class UserUiModel(
    val nombre: String = "Usuario",
    val fotoUri: String? = null,
    val rol: String = "Invitado"
)
