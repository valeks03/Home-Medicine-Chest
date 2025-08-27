package com.example.homemedicinechest.data.repo

import com.example.homemedicinechest.data.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class StatsRepository(
    private val logsDao: IntakeLogDao,
    private val medsDao: MedicineDao
) {
    fun observeStats(
        userId: Long,
        medicineId: Long?,
        from: Long,
        to: Long
    ): Flow<StatsBundle> {
        val logsF = logsDao.observeLogs(userId, medicineId, from, to)
        val dailyF = logsDao.observeDailyStats(userId, medicineId, from, to)
        val medsF = medsDao.observeByUser(userId) // если нет — сделай Flow<List<Medicine>>

        return combine(logsF, dailyF, medsF) { logs, daily, meds ->
            val taken = logs.count { it.status == "TAKEN" }
            val skipped = logs.count { it.status == "SKIPPED" }
            val total = taken + skipped
            val adherence = if (total == 0) 0f else taken.toFloat() / total

            StatsBundle(
                logs = logs,
                daily = daily,
                medicines = meds,
                totals = Totals(taken, skipped, total, adherence)
            )
        }
    }
}

data class Totals(
    val taken: Int,
    val skipped: Int,
    val total: Int,
    val adherence: Float // 0..1
)

data class StatsBundle(
    val logs: List<IntakeLog>,
    val daily: List<DailyRow>,
    val medicines: List<Medicine>,
    val totals: Totals
)