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
            textSize = 14f
        }

        // Page settings (A4 size approx in pixels at 72dpi)
        val pageWidth = 595
        val pageHeight = 842
        var currentY = 50f
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Title
        textPaint.isFakeBoldText = true
        textPaint.textSize = 20f
        canvas.drawText("REPORTE ACADÉMICO DE EVIDENCIAS", 100f, currentY, textPaint)
        currentY += 40f

        // Info
        textPaint.isFakeBoldText = false
        textPaint.textSize = 12f
        canvas.drawText("Materia: ${report.subject}", 50f, currentY, textPaint)
        currentY += 20f
        canvas.drawText("Estudiante: ${report.studentName}", 50f, currentY, textPaint)
        currentY += 20f
        canvas.drawText("Grado y Grupo: ${report.grade}° ${report.group}", 50f, currentY, textPaint)
        currentY += 20f
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(report.createdAt)
        canvas.drawText("Fecha: $date", 50f, currentY, textPaint)
        currentY += 40f

        evidences.forEach { evidence ->
            // Check if we need a new page
            if (currentY > pageHeight - 300) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = 50f
            }

            // Evidence Title
            textPaint.isFakeBoldText = true
            canvas.drawText("Título: ${evidence.label}", 50f, currentY, textPaint)
            currentY += 20f

            // Description
            if (evidence.description.isNotBlank()) {
                textPaint.isFakeBoldText = false
                canvas.drawText("Proceso: ${evidence.description}", 50f, currentY, textPaint)
                currentY += 20f
            }

            // Image
            try {
                val imageUri = Uri.parse(evidence.imagePath)
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val scaledBitmap = scaleBitmap(bitmap, pageWidth - 100)
                    canvas.drawBitmap(scaledBitmap, 50f, currentY, paint)
                    currentY += scaledBitmap.height + 30f
                }
                inputStream?.close()
            } catch (e: Exception) {
                canvas.drawText("[Error al cargar imagen]", 50f, currentY, textPaint)
                currentY += 20f
            }
            
            // Footer on every page
            val footerPaint = Paint().apply {
                textSize = 8f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
            }
            canvas.drawText("Gestor de Evidencias - Mtro. Salvador Perez Zamoran", 350f, pageHeight - 20f, footerPaint)
        }

        pdfDocument.finishPage(page)

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

    private fun scaleBitmap(source: Bitmap, maxWidth: Int): Bitmap {
        val aspectRatio = source.width.toFloat() / source.height.toFloat()
        val targetWidth = if (source.width > maxWidth) maxWidth else source.width
        val targetHeight = (targetWidth / aspectRatio).toInt()
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
    }
}
