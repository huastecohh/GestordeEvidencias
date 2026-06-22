package com.example.gestordeevidencias.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gestordeevidencias.data.local.dao.ReportDao
import com.example.gestordeevidencias.data.local.entities.EvidenceEntity
import com.example.gestordeevidencias.data.local.entities.ReportEntity

@Database(
    entities = [ReportEntity::class, EvidenceEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
}
