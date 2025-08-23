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
        Profile::class
    ],
    version = 4,
    exportSchema = true
)
abstract class AppDb : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun familyDao(): FamilyDao
    abstract fun medicineDao(): MedicineDao
    abstract fun dosePlanDao(): DosePlanDao
    abstract fun intakeDao(): IntakeDao
    abstract fun profileDao(): ProfileDao
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

