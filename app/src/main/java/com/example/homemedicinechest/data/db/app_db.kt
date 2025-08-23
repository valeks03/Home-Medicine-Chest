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
        IntakeEvent::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDb : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun familyDao(): FamilyDao
    abstract fun medicineDao(): MedicineDao
    abstract fun dosePlanDao(): DosePlanDao
    abstract fun intakeDao(): IntakeDao
}


val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1) Добавляем колонку
        db.execSQL("ALTER TABLE Medicine ADD COLUMN nameNorm TEXT")

        // 2) Первично заполняем на основе name (ASCII-вариант)
        db.execSQL("UPDATE Medicine SET nameNorm = lower(name)")
    }
}