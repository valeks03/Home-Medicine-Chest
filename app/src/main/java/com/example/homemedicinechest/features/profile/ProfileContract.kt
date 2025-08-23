package com.example.homemedicinechest.features.profile

import com.example.homemedicinechest.data.db.Profile

interface ProfileView {
    fun showLoading(show: Boolean)
    fun render(profile: Profile)
    fun showMessage(msg: String?)
}