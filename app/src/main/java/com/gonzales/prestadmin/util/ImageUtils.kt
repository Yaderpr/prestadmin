package com.gonzales.prestadmin.util
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle // ¡Importa este!
import androidx.compose.material3.Icon // Asegúrate de usar Icon de Material 3
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

fun createImageUri(context: Context): Uri? {
    val contentResolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Cedulas")
    }
    return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
}


@Composable
fun UserIcon(
    imageUri: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageUri)
            .crossfade(true)
            .build()
    )

    val state = painter.state

    if (state is AsyncImagePainter.State.Error) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Avatar de usuario por defecto (error)",
            modifier = modifier
                .size(size)
                .clickable { onClick() }
        )
    } else {
        Image(
            painter = painter,
            contentDescription = "Foto de perfil del usuario",
            modifier = modifier
                .size(size)
                .clickable { onClick() }
        )
    }

}