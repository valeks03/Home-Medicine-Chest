package com.example.homemedicinechest.features.profile

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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

    var heightText by remember { mutableStateOf(profile.heightCm?.toString().orEmpty()) }
    var weightText by remember { mutableStateOf(profile.weightKg?.toString().orEmpty()) }


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

    LaunchedEffect(profile.userId, profile.name, profile.birthdayMillis, profile.heightCm, profile.weightKg) {
        name = profile.name.orEmpty()
        birthday = profile.birthdayMillis
        heightText = profile.heightCm?.toString().orEmpty()
        weightText = profile.weightKg?.toString().orEmpty()
    }


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = remember { SnackbarHostState() }) }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
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

            Spacer(Modifier.height(6.dp))

            Text("Вес и рост", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = heightText,
                onValueChange = { heightText = it.filter { ch -> ch.isDigit() } }, // только цифры
                label = { Text("Рост (см)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = weightText,
                onValueChange = { txt ->
                    // разрешим один разделитель . или , -> приведём к '.'
                    val normalized = txt.replace(',', '.')
                    // оставим цифры и максимум одну точку
                    val cleaned = buildString {
                        var dot = false
                        for (c in normalized) {
                            if (c.isDigit()) append(c)
                            else if ((c == '.' || c == ',') && !dot) { append('.'); dot = true }
                        }
                    }
                    weightText = cleaned
                },
                label = { Text("Вес (кг)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )


            val bmi = remember(heightText, weightText) {
                val h = heightText.toFloatOrNull()?.div(100f)
                val w = weightText.toFloatOrNull()
                if (h != null && w != null && h > 0f) (w / (h * h)) else null
            }
            bmi?.let {
                Row (modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End) {
                    Text(
                        text = String.format(Locale.getDefault(), "BMI: %.1f ", it),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }


            Spacer(Modifier.height(6.dp))

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
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = newPass2, onValueChange = { newPass2 = it; passError = null },
                label = { Text("Повтор нового пароля") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
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
                    val height = heightText.toIntOrNull()?.takeIf { it in 30..300 } // разумные пределы
                    val weight = weightText.toFloatOrNull()?.takeIf { it in 2f..500f }

                    val updated = profile.copy(
                        name = name.ifBlank { null },
                        birthdayMillis = birthday,
                        heightCm = height,
                        weightKg = weight
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

            Spacer(modifier = Modifier.height(80.dp))
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
