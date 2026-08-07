package io.github.openflocon.domain.common.files

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import java.io.File

/**
 * Native open/save file dialogs via FileKit (Nucleus-recommended for Tao; no AWT window).
 */
object FilePicker {

    suspend fun pickOpenFile(
        title: String,
        extensions: Collection<String>? = null,
    ): File? {
        val type = if (extensions.isNullOrEmpty()) {
            FileKitType.File()
        } else {
            FileKitType.File(extensions.map { it.removePrefix(".") }.toSet())
        }
        return FileKit.openFilePicker(
            type = type,
            dialogSettings = FileKitDialogSettings(title = title),
        )?.file
    }

    suspend fun pickSaveFile(
        title: String,
        defaultFileName: String,
    ): File? {
        val suggestedName = defaultFileName.substringBeforeLast('.', missingDelimiterValue = defaultFileName)
        val extension = defaultFileName
            .substringAfterLast('.', missingDelimiterValue = "")
            .takeIf { it.isNotEmpty() && it != defaultFileName }

        return FileKit.openFileSaver(
            suggestedName = suggestedName,
            defaultExtension = extension,
            allowedExtensions = extension?.let { setOf(it) },
            dialogSettings = FileKitDialogSettings(title = title),
        )?.file
    }
}
