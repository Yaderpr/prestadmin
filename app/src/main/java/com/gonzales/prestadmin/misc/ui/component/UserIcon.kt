// com/gonzales/prestadmin/ui/components/UserIcon.kt
package com.gonzales.prestadmin.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun UserIcon(
    fotoUri: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
size: Dp = 36.dp) {
    if (fotoUri != null) {
        AsyncImage(
            model = fotoUri,
            contentDescription = "Avatar",
            modifier = modifier
                .size(size, size)
                .clickable { onClick() }
        )
    } else {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Avatar",
            modifier = modifier
                .size(size, size)
                .clickable { onClick() }
        )
    }
}
