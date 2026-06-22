package com.example.gestordeevidencias.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val grade: String,
    val group: String,
    val studentName: String,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable
