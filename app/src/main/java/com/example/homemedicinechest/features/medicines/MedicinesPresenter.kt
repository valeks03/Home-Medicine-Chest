package com.example.homemedicinechest.features.medicines

import com.example.homemedicinechest.core.Presenter
import com.example.homemedicinechest.data.repo.MedicinesRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class MedicinesPresenter(
    private val repo: MedicinesRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : Presenter<MedicinesView>, MedicinesPresenterContract {

    private var view: MedicinesView? = null
    private var job: Job? = null

    override fun attach(view: MedicinesView) { this.view = view }
    override fun detach() { this.view = null; job?.cancel() }

    override fun observe(userId: Long) {
        job?.cancel()
        job = scope.launch {
            repo.observeAll(userId).collectLatest { list ->
                view?.render(list)
            }
        }
    }

    override suspend fun addOrUpdate(m: com.example.homemedicinechest.data.db.Medicine) {
        repo.upsert(m)
    }

    override suspend fun delete(m: com.example.homemedicinechest.data.db.Medicine) {
        repo.delete(m)
    }
}