package com.example.gestordeevidencias.di

import android.content.Context
import androidx.room.Room
import com.example.gestordeevidencias.data.local.dao.ReportDao
import com.example.gestordeevidencias.data.local.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gestor_evidencias_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideReportDao(db: AppDatabase): ReportDao {
        return db.reportDao()
    }
}
