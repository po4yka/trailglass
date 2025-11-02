package com.po4yka.trailglass.photo

import android.content.Context

/**
 * Android implementation of PhotoPickerFactory.
 */
actual class PhotoPickerFactory(private val context: Context) {
    actual fun create(userId: String): PhotoPicker {
        return AndroidPhotoPicker(context, userId)
    }
}
