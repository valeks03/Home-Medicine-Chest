package com.example.homemedicinechest.features.profile

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
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

    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var newPass2 by remember { mutableStateOf("") }
    var passError by remember { mutableStateOf<String?>(null) }
    val userRepo = remember { com.example.homemedicinechest.data.repo.UserRepository(app.db.userDao()) }

    var showBirthdayPicker by remember { mutableStateOf(false) }



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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { showBirthdayPicker = true }) { Text("Дата рождения") }
                Text(
                    text = birthday?.let { df.format(Date(it)) } ?: "—",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }


            Spacer(Modifier.height(8.dp))

            Text("Смена пароля", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = oldPass, onValueChange = { oldPass = it; passError = null },
                label = { Text("Старый пароль") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = newPass, onValueChange = { newPass = it; passError = null },
                label = { Text("Новый пароль") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = newPass2, onValueChange = { newPass2 = it; passError = null },
                label = { Text("Повтор нового пароля") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                )
            )

            passError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    if (newPass != newPass2) {
                        passError = "Пароли не совпадают"
                        return@Button
                    }
                    scope.launch {
                        loading = true
                        val res = userRepo.changePassword(userId, oldPass, newPass)
                        loading = false
                        res.onSuccess {
                            oldPass = ""; newPass = ""; newPass2 = ""
                            message = "Пароль изменён"
                        }.onFailure {
                            passError = it.message ?: "Не удалось изменить пароль"
                        }
                    }
                }) { Text("Изменить пароль") }
            }




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
        if (showBirthdayPicker) {
            BirthdayPickerDialog(
                initialMillis = birthday,
                onDismiss = { showBirthdayPicker = false },
                onConfirm = { selected ->
                    birthday = selected
                    showBirthdayPicker = false
                }
            )
        }
    }
}
