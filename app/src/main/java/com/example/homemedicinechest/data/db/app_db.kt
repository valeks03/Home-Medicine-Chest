package com.example.homemedicinechest.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        User::class,
        FamilyMember::class,
        Medicine::class,
        DosePlan::class,
        IntakeEvent::class,
        Profile::class,
        MedicationSchedule::class,
        IntakeLog::class
    ],
    version = 5,
    exportSchema = true
)
abstract class AppDb : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun familyDao(): FamilyDao
    abstract fun medicineDao(): MedicineDao
    abstract fun dosePlanDao(): DosePlanDao
    abstract fun intakeDao(): IntakeDao
    abstract fun profileDao(): ProfileDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun intakeLogDao(): IntakeLogDao
}


val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE Medicine ADD COLUMN nameNorm TEXT")

        db.execSQL("UPDATE Medicine SET nameNorm = lower(name)")
    }
}
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `Profile`(
                `userId` INTEGER NOT NULL PRIMARY KEY,
                `name` TEXT,
                `birthdayMillis` INTEGER
            )
        """.trimIndent())
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `Profile` ADD COLUMN `heightCm` INTEGER")
        db.execSQL("ALTER TABLE `Profile` ADD COLUMN `weightKg` REAL")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `MedicationSchedule`(
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `userId` INTEGER NOT NULL,
                `medicineId` INTEGER NOT NULL,
                `hour` INTEGER NOT NULL,
                `minute` INTEGER NOT NULL,
                `daysMask` INTEGER NOT NULL,
                `dose` TEXT,
                `enabled` INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `IntakeLog`(
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `userId` INTEGER NOT NULL,
                `medicineId` INTEGER NOT NULL,
                `plannedAt` INTEGER NOT NULL,
                `takenAt` INTEGER,
                `status` TEXT NOT NULL
            )
        """.trimIndent())
    }
}

