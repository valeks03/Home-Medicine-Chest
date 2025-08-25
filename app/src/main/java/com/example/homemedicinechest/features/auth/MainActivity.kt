package com.example.homemedicinechest.features.auth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.homemedicinechest.App
import com.example.homemedicinechest.data.repo.UserRepository
import com.example.homemedicinechest.features.medicines.MedicinesScreen
import kotlinx.coroutines.launch
import com.example.homemedicinechest.features.home.HomeShell
import com.example.homemedicinechest.data.prefs.UserSession

class MainActivity : ComponentActivity(), AuthView {

    // Auth
    private lateinit var presenter: AuthPresenter
    private var setLoading: ((Boolean) -> Unit)? = null
    private var setMessage: ((String?) -> Unit)? = null
    private var onLoginSuccessCallback: ((Long) -> Unit)? = null
    private lateinit var session: UserSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = AuthPresenter(UserRepository((application as App).db.userDao()))
        presenter.attach(this)
        session = UserSession(this)
        setContent {
            MaterialTheme {
                NotificationsPermissionRequester()
                val nav = rememberNavController()
                val currentUserId by session.userId.collectAsState(initial = null)
                val startDest = if (currentUserId == null) "auth" else "home/${currentUserId}"

                LaunchedEffect(currentUserId) {
                    val uid = currentUserId
                    if (uid != null) {
                        nav.navigate("home/$uid") {
                            popUpTo("auth") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }


                NavHost(navController = nav, startDestination = startDest) {
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
                    composable(
                        "home/{userId}",
                        arguments = listOf(navArgument("userId"){ type = NavType.LongType })
                    ) { backStackEntry ->
                        val uid = backStackEntry.arguments?.getLong("userId") ?: 0L
                        HomeShell(
                            userId = uid,
                            onLogout = {
                                lifecycleScope.launch {
                                    session.clear()
                                    nav.navigate("auth") {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                    composable(
                        route = "medicines/{userId}",
                        arguments = listOf(androidx.navigation.navArgument("userId"){ type = NavType.LongType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
                        MedicinesScreen(userId = userId)
                    }
                }
            }
        }
    }

    // ----- AuthView -----
    override fun showLoading(show: Boolean) { setLoading?.invoke(show) }
    override fun showMessage(msg: String) { setMessage?.invoke(msg) }
    override fun onLoginSuccess(userId: Long) {
        lifecycleScope.launch { session.saveUserId(userId) }
    }

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

@Composable
fun NotificationsPermissionRequester() {
    if (Build.VERSION.SDK_INT < 33) return

    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}