package com.example.gestordeevidencias.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.StaticLayout
import android.text.TextPaint
import com.example.gestordeevidencias.data.local.entities.EvidenceEntity
import com.example.gestordeevidencias.data.local.entities.ReportEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExportHelper {

    fun exportToPdf(
        context: Context,
        report: ReportEntity,
        evidences: List<EvidenceEntity>
    ): File? {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val textPaint = TextPaint().apply {
            textSize = 12f
        }

        val pageWidth = 595 // A4 width in points
        val pageHeight = 842 // A4 height in points
        
        // Group evidences by pairs to have 2 per page
        val evidenceChunks = evidences.chunked(2)

        evidenceChunks.forEachIndexed { pageIndex, chunk ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            var currentY = 40f

            // Only draw header on the first page
            if (pageIndex == 0) {
                textPaint.isFakeBoldText = true
                textPaint.textSize = 18f
                canvas.drawText("REPORTE ACADÉMICO DE EVIDENCIAS", 100f, currentY, textPaint)
                currentY += 35f

                textPaint.isFakeBoldText = false
                textPaint.textSize = 11f
                canvas.drawText("Materia: ${report.subject}", 50f, currentY, textPaint)
                currentY += 15f
                canvas.drawText("Estudiante: ${report.studentName}", 50f, currentY, textPaint)
                currentY += 15f
                canvas.drawText("Grado y Grupo: ${report.grade}° ${report.group}", 50f, currentY, textPaint)
                currentY += 15f
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(report.createdAt)
                canvas.drawText("Fecha: $date", 50f, currentY, textPaint)
                currentY += 30f
            }

            // Draw up to 2 evidences per page
            chunk.forEachIndexed { index, evidence ->
                // Title
                textPaint.isFakeBoldText = true
                textPaint.textSize = 12f
                canvas.drawText("Título: ${evidence.label}", 50f, currentY, textPaint)
                currentY += 15f

                // Description
                if (evidence.description.isNotBlank()) {
                    textPaint.isFakeBoldText = false
                    textPaint.textSize = 10f
                    canvas.drawText("Proceso: ${evidence.description}", 50f, currentY, textPaint)
                    currentY += 15f
                }

                // Image - Scale to fit half page approx
                try {
                    val imageUri = Uri.parse(evidence.imagePath)
                    val inputStream = context.contentResolver.openInputStream(imageUri)
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    if (originalBitmap != null) {
                        // Apply rotation
                        val rotatedBitmap = if (evidence.rotation != 0f) {
                            val matrix = android.graphics.Matrix().apply { postRotate(evidence.rotation) }
                            Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
                        } else {
                            originalBitmap
                        }

                        // Scaling logic: Base max width is (pageWidth - 100)
                        // The user's 'scale' factor (0.5 to 1.5) will adjust this.
                        val baseMaxWidth = pageWidth - 100
                        val targetWidth = (baseMaxWidth * evidence.scale).toInt()
                        
                        // Scale keeping aspect ratio
                        val aspectRatio = rotatedBitmap.width.toFloat() / rotatedBitmap.height.toFloat()
                        val targetHeight = (targetWidth / aspectRatio).toInt()
                        
                        val scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap, targetWidth, targetHeight, true)
                        
                        // Center horizontally
                        val xPos = (pageWidth - scaledBitmap.width) / 2f
                        canvas.drawBitmap(scaledBitmap, xPos, currentY, paint)
                        currentY += scaledBitmap.height + 40f
                    }
                    inputStream?.close()
                } catch (e: Exception) {
                    canvas.drawText("[Error al cargar imagen]", 50f, currentY, textPaint)
                    currentY += 20f
                }
            }

            // Footer
            val footerPaint = Paint().apply {
                textSize = 8f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
                color = android.graphics.Color.GRAY
            }
            canvas.drawText("Gestor de Evidencias - Mtro. Salvador Perez Zamoran", 300f, pageHeight - 20f, footerPaint)
            canvas.drawText("Página ${pageIndex + 1}", 50f, pageHeight - 20f, footerPaint)

            pdfDocument.finishPage(page)
        }

        val fileName = "Reporte_${report.studentName.replace(" ", "_")}.pdf"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            pdfDocument.close()
            null
        }
    }
}
