package com.example.gestordeevidencias.data.local.dao

import androidx.room.*
import com.example.gestordeevidencias.data.local.entities.EvidenceEntity
import com.example.gestordeevidencias.data.local.entities.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidence(evidence: EvidenceEntity)

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM evidences WHERE reportId = :reportId ORDER BY orderIndex ASC")
    fun getEvidencesForReport(reportId: Long): Flow<List<EvidenceEntity>>

    @Query("SELECT * FROM reports WHERE id = :reportId")
    suspend fun getReportById(reportId: Long): ReportEntity?

    @Delete
    suspend fun deleteReport(report: ReportEntity)
}
