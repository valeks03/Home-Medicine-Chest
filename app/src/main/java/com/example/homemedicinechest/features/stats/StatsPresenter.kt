package com.example.homemedicinechest.features.stats


import com.example.homemedicinechest.data.repo.StatsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

interface StatsView {
    fun showLoading(show: Boolean)
    fun render(bundle: com.example.homemedicinechest.data.repo.StatsBundle)
}

class StatsPresenter(
    private val repo: StatsRepository
) {
    private var job: Job? = null
    private var view: StatsView? = null

    fun attach(v: StatsView) { view = v }
    fun detach() { view = null; job?.cancel() }

    fun observe(userId: Long, medicineId: Long?, from: Long, to: Long) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            view?.showLoading(true)
            repo.observeStats(userId, medicineId, from, to).collectLatest { bundle ->
                view?.render(bundle)
                view?.showLoading(false)
            }
        }
    }
}
