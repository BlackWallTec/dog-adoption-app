package com.app.android.presentation.view.viewsDonativos

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

// Comprobante (HU46) y constancia (HU45) aceptan lo mismo: capturas o PDF y máximo 10 MB
internal val TIPOS_PERMITIDOS = arrayOf("image/jpeg", "image/png", "application/pdf")
internal const val MAX_BYTES = 10L * 1024 * 1024

// Saca el nombre y el peso del archivo que escogió el usuario
internal fun leerArchivo(
    context: Context,
    uri: Uri,
    nombrePorDefecto: String,
): Pair<String, Long> {
    var nombre = nombrePorDefecto
    var tamano = 0L
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val iNombre = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val iTamano = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst()) {
            if (iNombre >= 0) nombre = cursor.getString(iNombre)
            if (iTamano >= 0) tamano = cursor.getLong(iTamano)
        }
    }
    return nombre to tamano
}
