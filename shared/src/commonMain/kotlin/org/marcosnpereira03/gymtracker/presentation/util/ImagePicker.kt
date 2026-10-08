package org.marcosnpereira03.gymtracker.presentation.util

import androidx.compose.runtime.Composable

/**
 * Launcher multiplataforma para seleccionar una imagen de la galería.
 * Retorna una función lambda `() -> Unit` que abre el selector del sistema.
 * Al seleccionar una foto válida, invoca `onImagePicked` con los bytes de la imagen (`ByteArray`).
 */
@Composable
expect fun rememberImagePickerLauncher(onImagePicked: (ByteArray) -> Unit): () -> Unit
