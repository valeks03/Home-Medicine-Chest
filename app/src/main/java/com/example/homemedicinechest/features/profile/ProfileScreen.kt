package com.example.homemedicinechest.features.profile

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.homemedicinechest.App
import com.example.homemedicinechest.data.db.Profile
import com.example.homemedicinechest.data.repo.ProfileRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(userId: Long) {
    val app = LocalContext.current.applicationContext as App
    val repo = remember { ProfileRepository(app.db.profileDao()) }
    val presenter = remember { ProfilePresenter(repo) }

    var profile by remember { mutableStateOf(Profile(userId)) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val view = remember {
        object : ProfileView {
            override fun showLoading(show: Boolean) { loading = show }
            override fun showMessage(msg: String?) { message = msg }
            override fun render(profile_: Profile) { profile = profile_ }
        }
    }

    DisposableEffect(Unit) {
        presenter.attach(view)
        presenter.observe(userId)
        onDispose { presenter.detach() }
    }

    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val df = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    var name by remember { mutableStateOf(profile.name.orEmpty()) }
    var birthday by remember { mutableStateOf(profile.birthdayMillis) }

    // Синхронизация локального UI, когда прилетает новый profile
    LaunchedEffect(profile.userId, profile.name, profile.birthdayMillis) {
        name = profile.name.orEmpty()
        birthday = profile.birthdayMillis
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = remember { SnackbarHostState() }) }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Профиль", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // E-mail можем показать из User, если надо (read-only). Если у тебя есть UserRepo — подтащим позже.

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val now = Calendar.getInstance()
                    DatePickerDialog(ctx, { _, y, m, d ->
                        val c = Calendar.getInstance().apply { set(y, m, d, 12, 0, 0) }
                        birthday = c.timeInMillis
                    }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
                }) { Text("Дата рождения") }

                Text(
                    text = birthday?.let { df.format(Date(it)) } ?: "—",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val updated = profile.copy(
                        name = name.ifBlank { null },
                        birthdayMillis = birthday
                    )
                    scope.launch { presenter.save(updated) }
                }
            ) { Text("Сохранить") }

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            message?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
