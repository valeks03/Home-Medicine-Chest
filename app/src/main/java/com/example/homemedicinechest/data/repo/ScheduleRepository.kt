package com.example.homemedicinechest.data.repo

import com.example.homemedicinechest.data.db.MedicationSchedule
import com.example.homemedicinechest.data.db.ScheduleDao
import kotlinx.coroutines.flow.Flow

class ScheduleRepository(private val dao: ScheduleDao) {
    fun observeForMedicine(medicineId: Long): Flow<List<MedicationSchedule>> =
        dao.observeForMedicine(medicineId)

    suspend fun addSchedule(s: MedicationSchedule): Long = dao.insert(s)
    suspend fun updateSchedule(s: MedicationSchedule) = dao.update(s)
    suspend fun deleteSchedule(s: MedicationSchedule) = dao.delete(s)
}