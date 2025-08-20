package com.example.homemedicinechest.features.medicines

import com.example.homemedicinechest.core.BaseView
import com.example.homemedicinechest.data.db.Medicine

interface MedicinesView : BaseView {
    fun render(list: List<Medicine>)
}

interface MedicinesPresenterContract {
    fun observe(userId: Long)
    suspend fun addOrUpdate(m: Medicine)
    suspend fun delete(m: Medicine)
}