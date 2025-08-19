package com.example.homemedicinechest.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        FamilyMember::class,
        Medicine::class,
        DosePlan::class,
        IntakeEvent::class
    ],
    version = 1
)
abstract class AppDb : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun familyDao(): FamilyDao
    abstract fun medicineDao(): MedicineDao
    abstract fun dosePlanDao(): DosePlanDao
    abstract fun intakeDao(): IntakeDao
}