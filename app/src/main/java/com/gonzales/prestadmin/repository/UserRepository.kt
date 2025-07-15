package com.gonzales.prestadmin.repository

import com.gonzales.prestadmin.data.UsuarioPrefs
import com.gonzales.prestadmin.viewmodel.UserUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class UserRepository(
    private val usuarioPrefs: UsuarioPrefs
) {
    val userFlow: Flow<UserUiModel> = combine(
        usuarioPrefs.nombre,
        usuarioPrefs.fotoUri
    ) { nombre, fotoUri ->
        UserUiModel(
            nombre = nombre,
            fotoUri = fotoUri,
            rol = "Administrador" // si decides guardar esto también, se puede adaptar
        )
    }

    suspend fun actualizarNombre(nombre: String) {
        usuarioPrefs.guardarNombre(nombre)
    }

    suspend fun actualizarFoto(uri: String?) {
        usuarioPrefs.guardarFotoUri(uri)
    }
    // En UserRepository.kt
    suspend fun actualizarPerfil(nombre: String, uri: String?) {
        usuarioPrefs.guardarNombre(nombre)
        usuarioPrefs.guardarFotoUri(uri)
    }

}
