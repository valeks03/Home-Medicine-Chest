package com.example.homemedicinechest

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.room.Room
import com.example.homemedicinechest.data.db.AppDb
import com.example.homemedicinechest.data.db.MIGRATION_1_2
import com.example.homemedicinechest.data.db.MIGRATION_2_3
import com.example.homemedicinechest.data.db.MIGRATION_3_4
import com.example.homemedicinechest.data.db.MIGRATION_4_5

class App : Application() {
    lateinit var db: AppDb

    override fun onCreate() {
        super.onCreate()

        db = Room.databaseBuilder(this, AppDb::class.java, "homemed.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .fallbackToDestructiveMigration()
            .build()

        val channel = NotificationChannel(
            "reminders",
            "Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Напоминания о приёме лекарств"
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }
}
