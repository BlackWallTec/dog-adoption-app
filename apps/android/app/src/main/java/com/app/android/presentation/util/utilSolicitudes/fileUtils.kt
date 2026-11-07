package com.app.android.presentation.util.utilSolicitudes

import android.content.Context
import android.provider.OpenableColumns
import com.app.android.domain.entities.entitiesSolicitudes.PickedFile
import android.net.Uri

fun Context.readPickedFile (uri: Uri): PickedFile {
    var name = "documento"
    var size = 0L

    contentResolver.query (uri, null, null, null, null)?.use {cursor ->
        if (cursor.moveToFirst()){
            val nameIdx = cursor.getColumnIndex (OpenableColumns.DISPLAY_NAME)
            val sizeIdx = cursor.getColumnIndex (OpenableColumns.SIZE)
            if (nameIdx >= 0) name = cursor.getString(nameIdx)
            if(sizeIdx >= 0 && !cursor.isNull(sizeIdx)) size = cursor.getLong (sizeIdx)
        }
    }
    return PickedFile (
        uri = uri.toString(),
        name = name,
        sizeBytes = size,
        mimeType = contentResolver.getType(uri) ?: ""

    )

}