package com.example.homemedicinechest.features.reminders

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.homemedicinechest.data.db.MedicationSchedule
import java.util.Calendar

object ReminderScheduler {

    fun scheduleNext(ctx: Context, schedule: MedicationSchedule) {
        val triggerAt = nextTriggerUtc(schedule)
        val am = ctx.getSystemService(AlarmManager::class.java)

        val pi = PendingIntent.getBroadcast(
            ctx,
            schedule.id.toInt(),
            Intent(ctx, ReminderReceiver::class.java).apply {
                action = ReminderReceiver.ACTION_REMIND
                putExtra("scheduleId", schedule.id)
                putExtra("userId", schedule.userId)
                putExtra("medicineId", schedule.medicineId)
                putExtra("hour", schedule.hour)
                putExtra("minute", schedule.minute)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canExact = if (Build.VERSION.SDK_INT >= 31) am.canScheduleExactAlarms() else true

        if (canExact) {
            // пытаемся точный; если система всё равно бросит SecurityException — откатываемся на неточный
            try {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
                return
            } catch (_: SecurityException) {
                // упали — пойдём в неточный
            }
        }

        // нет права на точные, либо поймали SecurityException -> ставим неточный
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }


    /** Возвращает millis следующего срабатывания с учётом daysMask */
    fun nextTriggerUtc(s: MedicationSchedule, nowMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, s.hour)
            set(Calendar.MINUTE, s.minute)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        var addDays = 0
        fun bitFor(dayOfWeek: Int): Int { // Calendar.MONDAY..SUNDAY -> 0..6
            val idx = (dayOfWeek + 5) % 7
            return 1 shl idx
        }
        val mask = s.daysMask
        while (true) {
            val candidate = (target.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, addDays) }
            val dayOk = (mask == 0) || (mask and bitFor(candidate.get(Calendar.DAY_OF_WEEK)) != 0)
            if (dayOk && candidate.timeInMillis > nowMillis) return candidate.timeInMillis
            addDays++
        }
    }

    suspend fun cancelAllForMedicine(ctx: Context, schedules: List<MedicationSchedule>) {
        schedules.forEach { cancel(ctx, it) }
    }

    fun cancel(ctx: Context, schedule: MedicationSchedule) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        val pi = PendingIntent.getBroadcast(
            ctx,
            schedule.id.toInt(), // ДОЛЖЕН совпасть с тем, что ставили в scheduleNext
            Intent(ctx, ReminderReceiver::class.java).apply { action = ReminderReceiver.ACTION_REMIND },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)

        // на всякий случай уберём активное уведомление
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.cancel(schedule.id.toInt())
    }
}