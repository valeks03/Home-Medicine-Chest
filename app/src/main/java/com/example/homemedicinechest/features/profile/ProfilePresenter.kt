package com.example.homemedicinechest.features.profile

import com.example.homemedicinechest.data.db.Profile
import com.example.homemedicinechest.data.repo.ProfileRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class ProfilePresenter(
    private val repo: ProfileRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private var view: ProfileView? = null
    private var observeJob: Job? = null

    fun attach(v: ProfileView) { view = v }
    fun detach() { view = null; observeJob?.cancel() }

    fun observe(userId: Long) {
        observeJob?.cancel()
        observeJob = scope.launch {
            repo.observe(userId).collectLatest { p ->
                // если профиля нет — подгрузим/создадим
                val profile = p ?: repo.loadOrCreate(userId)
                view?.render(profile)
            }
        }
    }

    fun save(profile: Profile) {
        scope.launch {
            view?.showLoading(true)
            repo.save(profile)
            view?.showLoading(false)
            view?.showMessage("Сохранено")
        }
    }
}