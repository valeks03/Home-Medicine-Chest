package com.example.homemedicinechest.features.reminders

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.homemedicinechest.App
import com.example.homemedicinechest.R
import com.example.homemedicinechest.data.db.IntakeLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_REMIND  -> showReminder(context, intent)
            ACTION_TAKEN,
            ACTION_SKIPPED -> handleAction(context, intent)
        }
    }

    private fun showReminder(ctx: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra("scheduleId", 0)
        val userId     = intent.getLongExtra("userId", 0)
        val medicineId = intent.getLongExtra("medicineId", 0)

        val nm = ctx.getSystemService(NotificationManager::class.java)

        val takenPi = PendingIntent.getBroadcast(
            ctx, (scheduleId * 10 + 1).toInt(),
            Intent(ctx, ReminderReceiver::class.java).apply {
                action = ACTION_TAKEN
                putExtra("scheduleId", scheduleId)     // важно для перепланирования
                putExtra("userId", userId)
                putExtra("medicineId", medicineId)
                putExtra("plannedAt", System.currentTimeMillis())
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val skippedPi = PendingIntent.getBroadcast(
            ctx, (scheduleId * 10 + 2).toInt(),
            Intent(ctx, ReminderReceiver::class.java).apply {
                action = ACTION_SKIPPED
                putExtra("scheduleId", scheduleId)     // важно для перепланирования
                putExtra("userId", userId)
                putExtra("medicineId", medicineId)
                putExtra("plannedAt", System.currentTimeMillis())
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(ctx, "reminders")
            .setSmallIcon(R.drawable.baseline_medication_24)
            .setContentTitle(ctx.getString(R.string.app_name))
            .setContentText("Время принять лекарство")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(0, "Принял",  takenPi)
            .addAction(0, "Пропустил", skippedPi)
            .setAutoCancel(true)
            .build()

        try {
            nm.notify(scheduleId.toInt(), notif)
        } catch (_: SecurityException) {
            // нет POST_NOTIFICATIONS — просто игнорируем
        }

        // 🔁 ВАЖНО: сразу запланировать следующий раз по этому же расписанию
        val app = ctx.applicationContext as App
        CoroutineScope(Dispatchers.IO).launch {
            app.db.scheduleDao().getById(scheduleId)?.let { s ->
                ReminderScheduler.scheduleNext(ctx, s)
            }
        }
    }

    private fun handleAction(ctx: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra("scheduleId", 0)
        val userId = intent.getLongExtra("userId", 0)
        val medicineId = intent.getLongExtra("medicineId", 0)
        val plannedAt  = intent.getLongExtra("plannedAt", System.currentTimeMillis())
        val status = if (intent.action == ACTION_TAKEN) "TAKEN" else "SKIPPED"

        val app = ctx.applicationContext as App
        CoroutineScope(Dispatchers.IO).launch {
            // пишем лог приёма
            app.db.intakeLogDao().insert(
                IntakeLog(
                    userId = userId,
                    medicineId = medicineId,
                    plannedAt = plannedAt,
                    takenAt = if (status == "TAKEN") System.currentTimeMillis() else null,
                    status = status
                )
            )
            val nm = ctx.getSystemService(NotificationManager::class.java)
            nm.cancel(scheduleId.toInt())

            app.db.scheduleDao().getById(scheduleId)?.let { s ->
                ReminderScheduler.scheduleNext(ctx, s)
            }
        }
    }

    companion object {
        const val ACTION_REMIND  = "remind"
        const val ACTION_TAKEN   = "taken"
        const val ACTION_SKIPPED = "skipped"
    }
}
