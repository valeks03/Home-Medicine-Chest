package com.example.homemedicinechest.features.auth

import com.example.homemedicinechest.core.Presenter
import com.example.homemedicinechest.data.repo.UserRepository

class AuthPresenter(private val repo: UserRepository)
    : Presenter<AuthView>, AuthPresenterContract {

    private var view: AuthView? = null

    override fun attach(view: AuthView) { this.view = view }
    override fun detach() { this.view = null }

    override suspend fun login(email: String, password: String) {
        view?.showLoading(true)
        val user = repo.findByEmail(email)
        if (user != null && repo.verifyPassword(user, password)) {
            view?.onLoginSuccess(user.id)
        } else {
            view?.showMessage("Неверный email или пароль")
        }
        view?.showLoading(false)
    }

    override suspend fun register(email: String, password: String) {
        view?.showLoading(true)
        val exists = repo.findByEmail(email) != null
        if (exists) {
            view?.showMessage("Пользователь уже существует")
        } else {
            val id = repo.create(email, password)
            view?.onLoginSuccess(id)
        }
        view?.showLoading(false)
    }

    override suspend fun resetPassword(email: String, newPassword: String, secretCode: String) {
        val ok = repo.resetPassword(email, newPassword, secretCode)
        view?.showMessage(if (ok) "Пароль обновлён" else "Сброс не удался")
    }
}
