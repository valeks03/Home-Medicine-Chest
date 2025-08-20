package com.example.homemedicinechest.data.repo

import com.example.homemedicinechest.data.db.Medicine
import com.example.homemedicinechest.data.db.MedicineDao
import kotlinx.coroutines.flow.Flow

class MedicinesRepository(private val dao: MedicineDao) {
    fun observeAll(userId: Long): Flow<List<Medicine>> = dao.observeAll(userId)

    suspend fun upsert(m: Medicine): Long =
        if (m.id == 0L)
            dao.insert(m)
        else { dao.update(m); m.id }

    suspend fun delete(m: Medicine) = dao.delete(m)
}