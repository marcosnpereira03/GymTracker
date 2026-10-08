package org.marcosnpereira03.gymtracker.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberImagePickerLauncher(onImagePicked: (ByteArray) -> Unit): () -> Unit {
    return remember(onImagePicked) {
        {
            val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
            if (rootViewController != null) {
                val configuration = PHPickerConfiguration().apply {
                    filter = PHPickerFilter.imagesFilter()
                    selectionLimit = 1
                }
                val picker = PHPickerViewController(configuration)
                val delegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
                    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                        picker.dismissViewControllerAnimated(true, null)
                        val result = didFinishPicking.firstOrNull() as? PHPickerResult ?: return
                        val provider = result.itemProvider
                        if (provider.hasItemConformingToTypeIdentifier("public.image")) {
                            provider.loadDataRepresentationForTypeIdentifier("public.image") { data, _ ->
                                if (data != null && data.length > 0u) {
                                    val bytes = ByteArray(data.length.toInt())
                                    bytes.usePinned { pinned ->
                                        platform.posix.memcpy(pinned.addressOf(0), data.bytes, data.length)
                                    }
                                    onImagePicked(bytes)
                                }
                            }
                        }
                    }
                }
                picker.delegate = delegate
                rootViewController.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}
