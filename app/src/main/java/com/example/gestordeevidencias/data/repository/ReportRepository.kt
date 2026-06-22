package com.example.gestordeevidencias.data.repository

import com.example.gestordeevidencias.data.local.dao.ReportDao
import com.example.gestordeevidencias.data.local.entities.EvidenceEntity
import com.example.gestordeevidencias.data.local.entities.ReportEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val reportDao: ReportDao
) {
    fun getAllReports(): Flow<List<ReportEntity>> = reportDao.getAllReports()

    fun getEvidencesForReport(reportId: Long): Flow<List<EvidenceEntity>> = 
        reportDao.getEvidencesForReport(reportId)

    suspend fun insertReport(report: ReportEntity): Long = reportDao.insertReport(report)

    suspend fun insertEvidence(evidence: EvidenceEntity) = reportDao.insertEvidence(evidence)

    suspend fun getReportById(reportId: Long): ReportEntity? = reportDao.getReportById(reportId)

    suspend fun deleteReport(report: ReportEntity) = reportDao.deleteReport(report)
}
