package com.example.gestordeevidencias.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Intent
import com.example.gestordeevidencias.data.local.entities.ReportEntity
import com.example.gestordeevidencias.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: ReportRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Lista de reportes filtrada
    val reports: StateFlow<List<ReportEntity>> = _searchQuery
        .combine(repository.getAllReports()) { query, list ->
            if (query.isBlank()) list
            else list.filter { 
                it.studentName.contains(query, ignoreCase = true) || 
                it.subject.contains(query, ignoreCase = true) 
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun createReport(subject: String, grade: String, group: String, studentName: String) {
        viewModelScope.launch {
            val newReport = ReportEntity(
                subject = subject,
                grade = grade,
                group = group,
                studentName = studentName
            )
            repository.insertReport(newReport)
        }
    }

    fun getEvidences(reportId: Long) = repository.getEvidencesForReport(reportId)

    fun addEvidence(reportId: Long, imagePath: String, label: String) {
        viewModelScope.launch {
            val evidences = repository.getEvidencesForReport(reportId).stateIn(viewModelScope).value
            val nextIndex = evidences.size
            val newEvidence = com.example.gestordeevidencias.data.local.entities.EvidenceEntity(
                reportId = reportId,
                imagePath = imagePath,
                label = label,
                orderIndex = nextIndex
            )
            repository.insertEvidence(newEvidence)
        }
    }

    fun deleteReport(report: ReportEntity) {
        viewModelScope.launch {
            repository.deleteReport(report)
        }
    }

    fun updateEvidence(evidence: com.example.gestordeevidencias.data.local.entities.EvidenceEntity) {
        viewModelScope.launch {
            repository.insertEvidence(evidence) // insertEvidence usa REPLACE on conflict
        }
    }

    fun backupData(context: android.content.Context) {
        viewModelScope.launch {
            // Lógica de exportación de base de datos
            val dbFile = context.getDatabasePath("gestor_evidencias_db")
            if (dbFile.exists()) {
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "com.example.gestordeevidencias.fileprovider",
                    dbFile
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Guardar copia de seguridad"))
            }
        }
    }

    fun importFromGallery(context: android.content.Context, reportId: Long, uri: android.net.Uri) {
        viewModelScope.launch {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = java.io.File(
                context.getExternalFilesDir(null),
                "Imported_${System.currentTimeMillis()}.jpg"
            )
            val outputStream = java.io.FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            addEvidence(reportId, android.net.Uri.fromFile(file).toString(), "Evidencia Importada")
        }
    }
}
