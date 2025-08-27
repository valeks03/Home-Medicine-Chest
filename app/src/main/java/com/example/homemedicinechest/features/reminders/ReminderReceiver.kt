package com.example.homemedicinechest.features.reminders

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.homemedicinechest.App
import com.example.homemedicinechest.R
import com.example.homemedicinechest.data.db.IntakeLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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

        val app = ctx.applicationContext as App

        // Достанем расписание и лекарство синхронно (короткие операции)
        val (s, med) = runBlocking {
            val s = app.db.scheduleDao().getById(scheduleId)
            val m = app.db.medicineDao().get(medicineId)
            s to m
        }

        // Если чего-то нет — выходим
        if (s == null || med == null) return

        // Если лекарство просрочено — ничего не показываем и не перепланируем
        val medicineExpired = med.expiresAt?.let { it < System.currentTimeMillis() } == true
        if (medicineExpired) return

        val takenPi = PendingIntent.getBroadcast(
            ctx, (scheduleId * 10 + 1).toInt(),
            Intent(ctx, ReminderReceiver::class.java).apply {
                action = ACTION_TAKEN
                putExtra("scheduleId", scheduleId)
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
                putExtra("scheduleId", scheduleId)
                putExtra("userId", userId)
                putExtra("medicineId", medicineId)
                putExtra("plannedAt", System.currentTimeMillis())
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Заголовок и текст
        val title = "Время принять ${med.name}"
        val body = "Примите, пожалуйста, лекарство"

        val notif = NotificationCompat.Builder(ctx, "reminders")
            .setSmallIcon(R.drawable.baseline_medication_24) // монохромный vector из res/drawable
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(0, "Принял", takenPi)
            .addAction(0, "Пропустил", skippedPi)
            .build()

        try {
            NotificationManagerCompat.from(ctx).notify(scheduleId.toInt(), notif)
        } catch (_: SecurityException) {
            // Нет разрешения на уведомления — игнорируем
        }

        // Перепланируем только если расписание активно и лекарство не просрочено
        if (s.enabled && !medicineExpired) {
            CoroutineScope(Dispatchers.IO).launch {
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
            // Лог приёма
            app.db.intakeLogDao().insert(
                IntakeLog(
                    userId = userId,
                    medicineId = medicineId,
                    plannedAt = plannedAt,
                    takenAt = if (status == "TAKEN") System.currentTimeMillis() else null,
                    status = status
                )
            )

            // Спрячем уведомление
            try {
                val nm = ctx.getSystemService(NotificationManager::class.java)
                nm.cancel(scheduleId.toInt())
            } catch (_: SecurityException) { }

            // Достанем актуальные s/med для условий перепланирования
            val s = app.db.scheduleDao().getById(scheduleId) ?: return@launch
            val med = app.db.medicineDao().get(medicineId) ?: return@launch
            val medicineExpired = med.expiresAt?.let { it < System.currentTimeMillis() } == true

            // Перепланируем следующий раз только если включено и не просрочено
            if (s.enabled && !medicineExpired) {
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
