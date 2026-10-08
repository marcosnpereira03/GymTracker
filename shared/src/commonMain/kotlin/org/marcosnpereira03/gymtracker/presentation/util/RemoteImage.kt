package org.marcosnpereira03.gymtracker.presentation.util

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * Gestor en memoria y descargador ligero de imágenes remotas para Compose Multiplatform.
 */
object ImageCache {
    private val memoryCache = mutableMapOf<String, ImageBitmap>()
    private val client by lazy { HttpClient() }

    suspend fun getImageBitmap(url: String): ImageBitmap? = withContext(Dispatchers.Default) {
        if (url.isBlank()) return@withContext null
        memoryCache[url]?.let { return@withContext it }

        runCatching {
            val response = client.get(url)
            val bytes = response.body<ByteArray>()
            val bitmap = bytes.decodeToImageBitmap()
            memoryCache[url] = bitmap
            bitmap
        }.getOrNull()
    }
}


/**
 * Composable que recuerda y carga de forma asíncrona una imagen remota a partir de su URL.
 */
@Composable
fun rememberRemoteImage(url: String?): ImageBitmap? {
    if (url.isNullOrBlank()) return null
    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(url) {
        bitmap = ImageCache.getImageBitmap(url)
    }

    return bitmap
}
