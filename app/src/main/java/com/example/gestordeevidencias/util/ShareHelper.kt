package com.example.gestordeevidencias.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object ShareHelper {
    fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "com.example.gestordeevidencias.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.whatsapp")
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Compartir reporte"))
        } catch (e: Exception) {
            intent.setPackage(null)
            context.startActivity(Intent.createChooser(intent, "Compartir reporte"))
        }
    }
}
