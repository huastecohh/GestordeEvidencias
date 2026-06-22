package com.example.gestordeevidencias.util

import android.content.Context
import android.net.Uri
import com.example.gestordeevidencias.data.local.entities.EvidenceEntity
import com.example.gestordeevidencias.data.local.entities.ReportEntity
import org.apache.poi.util.Units
import org.apache.poi.wp.usermodel.HeaderFooterType
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object WordExportHelper {

    fun exportToWord(
        context: Context,
        report: ReportEntity,
        evidences: List<EvidenceEntity>
    ): File? {
        return try {
            val document = XWPFDocument()

            // Footer (Pie de página)
            val footer = document.createFooter(HeaderFooterType.DEFAULT)
            val footerPara = footer.createParagraph()
            footerPara.alignment = ParagraphAlignment.RIGHT
            val footerRun = footerPara.createRun()
            footerRun.fontSize = 9
            footerRun.isItalic = true
            footerRun.setText("Gestor de Evidencias - Desarrollado por Mtro. Salvador Perez Zamoran")

            // Título
            val title = document.createParagraph()
            title.alignment = ParagraphAlignment.CENTER
            val titleRun = title.createRun()
            titleRun.isBold = true
            titleRun.fontSize = 20
            titleRun.setText("REPORTE ACADÉMICO DE EVIDENCIAS")
            titleRun.addBreak()

            // Datos Generales
            val info = document.createParagraph()
            val infoRun = info.createRun()
            infoRun.fontSize = 12
            infoRun.setText("Materia: ${report.subject}")
            infoRun.addBreak()
            infoRun.setText("Estudiante: ${report.studentName}")
            infoRun.addBreak()
            infoRun.setText("Grado y Grupo: ${report.grade}° ${report.group}")
            infoRun.addBreak()
            infoRun.setText("Fecha de creación: ${java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(report.createdAt)}")
            infoRun.addBreak()

            // Evidencias
            evidences.forEach { evidence ->
                val evidencePara = document.createParagraph()
                evidencePara.alignment = ParagraphAlignment.CENTER
                
                // Etiqueta de la foto
                val labelRun = evidencePara.createRun()
                labelRun.isBold = true
                labelRun.fontSize = 14
                labelRun.setText("Título: ${evidence.label}")
                labelRun.addBreak()

                // Descripción
                if (evidence.description.isNotBlank()) {
                    val descRun = evidencePara.createRun()
                    descRun.fontSize = 11
                    descRun.setText("Proceso: ${evidence.description}")
                    descRun.addBreak()
                }

                // Imagen
                try {
                    val imageUri = Uri.parse(evidence.imagePath)
                    val imageFile = File(imageUri.path ?: "")
                    if (imageFile.exists()) {
                        val fis = FileInputStream(imageFile)
                        labelRun.addPicture(
                            fis,
                            XWPFDocument.PICTURE_TYPE_JPEG,
                            imageFile.name,
                            Units.toEMU(300.0),
                            Units.toEMU(200.0)
                        )
                        fis.close()
                    }
                } catch (e: Exception) {
                    val errorRun = evidencePara.createRun()
                    errorRun.setText("[Error al cargar imagen]")
                }
                labelRun.addBreak()
            }

            // Guardar archivo
            val fileName = "Reporte_${report.studentName.replace(" ", "_")}.docx"
            val file = File(context.getExternalFilesDir(null), fileName)
            val out = FileOutputStream(file)
            document.write(out)
            out.close()
            document.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
