package com.example.homemedicinechest.core

interface BaseView {
    fun showLoading(show: Boolean)
    fun showMessage(msg: String)
}

interface Presenter<V : BaseView> {
    fun attach(view: V)
    fun detach()
}