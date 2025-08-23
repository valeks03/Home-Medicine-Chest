package com.example.homemedicinechest

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.room.Room
import com.example.homemedicinechest.data.db.AppDb
import com.example.homemedicinechest.data.db.MIGRATION_1_2

class App : Application() {
    lateinit var db: AppDb

    override fun onCreate() {
        super.onCreate()

        db = Room.databaseBuilder(this, AppDb::class.java, "homemed.db")
            .fallbackToDestructiveMigration()
            .addMigrations(MIGRATION_1_2)   // ← важно
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
