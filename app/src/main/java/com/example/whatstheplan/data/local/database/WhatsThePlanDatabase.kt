package com.example.whatstheplan.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.whatstheplan.data.local.database.dao.CheckInDao
import com.example.whatstheplan.data.local.database.dao.DailyPlanDao
import com.example.whatstheplan.data.local.database.dao.DailyReflectionDao
import com.example.whatstheplan.data.local.database.dao.FunFactDao
import com.example.whatstheplan.data.local.database.dao.ScreenTimeDao
import com.example.whatstheplan.data.local.database.entities.CheckInEntity
import com.example.whatstheplan.data.local.database.entities.DailyPlanEntity
import com.example.whatstheplan.data.local.database.entities.DailyReflectionEntity
import com.example.whatstheplan.data.local.database.entities.FunFactEntity
import com.example.whatstheplan.data.local.database.entities.ScreenTimeSnapshotEntity

@Database(
    entities = [
        DailyPlanEntity::class,
        CheckInEntity::class,
        DailyReflectionEntity::class,
        FunFactEntity::class,
        ScreenTimeSnapshotEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class WhatsThePlanDatabase : RoomDatabase() {
    abstract fun dailyPlanDao(): DailyPlanDao
    abstract fun checkInDao(): CheckInDao
    abstract fun dailyReflectionDao(): DailyReflectionDao
    abstract fun funFactDao(): FunFactDao
    abstract fun screenTimeDao(): ScreenTimeDao

    companion object {
        @Volatile private var instance: WhatsThePlanDatabase? = null

        // Placeholder for future database schema migrations to preserve user data
        private val ALL_MIGRATIONS: Array<Migration> = arrayOf()

        fun getInstance(context: Context): WhatsThePlanDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WhatsThePlanDatabase::class.java,
                    "whatstheplan.db",
                )
                    .addMigrations(*ALL_MIGRATIONS)
                    .build()
                    .also { instance = it }
            }
    }
}
