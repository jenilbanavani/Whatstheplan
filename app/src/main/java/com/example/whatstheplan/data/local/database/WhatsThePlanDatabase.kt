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
import com.example.whatstheplan.data.local.database.dao.UserCorrectionDao
import com.example.whatstheplan.data.local.database.entities.CheckInEntity
import com.example.whatstheplan.data.local.database.entities.DailyPlanEntity
import com.example.whatstheplan.data.local.database.entities.DailyReflectionEntity
import com.example.whatstheplan.data.local.database.entities.FunFactEntity
import com.example.whatstheplan.data.local.database.entities.ScreenTimeSnapshotEntity
import com.example.whatstheplan.data.local.database.entities.UserCorrectionEntity

@Database(
    entities = [
        DailyPlanEntity::class,
        CheckInEntity::class,
        DailyReflectionEntity::class,
        FunFactEntity::class,
        ScreenTimeSnapshotEntity::class,
        UserCorrectionEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class WhatsThePlanDatabase : RoomDatabase() {
    abstract fun dailyPlanDao(): DailyPlanDao
    abstract fun checkInDao(): CheckInDao
    abstract fun dailyReflectionDao(): DailyReflectionDao
    abstract fun funFactDao(): FunFactDao
    abstract fun screenTimeDao(): ScreenTimeDao
    abstract fun userCorrectionDao(): UserCorrectionDao

    companion object {
        @Volatile private var instance: WhatsThePlanDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE daily_plans ADD COLUMN firstStep TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE daily_plans ADD COLUMN status TEXT NOT NULL DEFAULT 'ACTIVE'")
                db.execSQL("ALTER TABLE daily_plans ADD COLUMN startedAt INTEGER")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `user_corrections` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `category` TEXT NOT NULL, `note` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)",
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_corrections ADD COLUMN source TEXT NOT NULL DEFAULT 'USER'")
            }
        }

        private val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)

        fun getInstance(context: Context): WhatsThePlanDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WhatsThePlanDatabase::class.java,
                    "whatstheplan.db",
                )
                    .addMigrations(*ALL_MIGRATIONS)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
