package com.example.homemedicinechest.features.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.homemedicinechest.App
import com.example.homemedicinechest.data.repo.UserRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), AuthView {

    // Auth
    private lateinit var presenter: AuthPresenter
    private var setLoading: ((Boolean) -> Unit)? = null
    private var setMessage: ((String?) -> Unit)? = null
    private var onLoginSuccessCallback: ((Long) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = AuthPresenter(UserRepository((application as App).db.userDao()))
        presenter.attach(this)

        setContent {
            MaterialTheme {
                val nav = rememberNavController()

                NavHost(navController = nav, startDestination = "auth") {
                    composable("auth") {
                        var loading by remember { mutableStateOf(false) }
                        var message by remember { mutableStateOf<String?>(null) }
                        setLoading = { loading = it }
                        setMessage = { message = it }
                        onLoginSuccessCallback = { userId ->
                            nav.navigate("medicines/$userId") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                        LoginScreen(
                            isLoading = loading,
                            message = message,
                            onLogin = { e, p -> lifecycleScope.launch { presenter.login(e, p) } },
                            onRegister = { e, p -> lifecycleScope.launch { presenter.register(e, p) } },
                            onReset = { e, newP, secret -> lifecycleScope.launch { presenter.resetPassword(e, newP, secret) }
                            }
                        )
                    }

                    // Временная заглушка экрана лекарств — заменим позже на полноценный экран
                    composable(
                        route = "medicines/{userId}",
                        arguments = listOf(
                            androidx.navigation.navArgument("userId") {
                                type = NavType.LongType
                            }
                        )
                    ) {
                        MedicinesPlaceholder()
                    }
                }
            }
        }
    }

    // ----- AuthView -----
    override fun showLoading(show: Boolean) { setLoading?.invoke(show) }
    override fun showMessage(msg: String) { setMessage?.invoke(msg) }
    override fun onLoginSuccess(userId: Long) { onLoginSuccessCallback?.invoke(userId) }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }
}

@Composable
private fun MedicinesPlaceholder() {
    androidx.compose.material3.Scaffold { p ->
        androidx.compose.material3.Text(
            "Экран лекарств (скоро подключим)",
            modifier = androidx.compose.ui.Modifier.padding(p).padding(16.dp)
        )
    }
}
