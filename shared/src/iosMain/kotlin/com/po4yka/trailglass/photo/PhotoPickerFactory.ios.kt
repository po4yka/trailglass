package com.po4yka.trailglass.photo

/**
 * iOS implementation of PhotoPickerFactory.
 */
actual class PhotoPickerFactory {
    actual fun create(userId: String): PhotoPicker {
        return IOSPhotoPicker(userId)
    }
}
