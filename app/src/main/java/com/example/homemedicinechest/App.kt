package com.example.homemedicinechest

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.room.Room
import com.example.homemedicinechest.data.db.AppDb

class App : Application() {
    lateinit var db: AppDb

    override fun onCreate() {
        super.onCreate()

        db = Room.databaseBuilder(
            applicationContext,
            AppDb::class.java,
            "homemed.db"
        ).build()

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
