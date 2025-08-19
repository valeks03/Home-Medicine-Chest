package com.example.homemedicinechest.features.auth

import com.example.homemedicinechest.core.BaseView

interface AuthView : BaseView {
    fun onLoginSuccess(userId: Long)
}

interface AuthPresenterContract {
    suspend fun login(email: String, password: String)
    suspend fun register(email: String, password: String)
    suspend fun resetPassword(email: String, newPassword: String, secretCode: String)
}
