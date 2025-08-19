package com.example.homemedicinechest.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    isLoading: Boolean,
    message: String?,
    onLogin: (email: String, pass: String) -> Unit,
    onRegister: (email: String, pass: String) -> Unit,
    onReset: (email: String, newPass: String, secret: String) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var showReset by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Text("Home Medicine Chest", style = MaterialTheme.typography.headlineSmall)

                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") }, singleLine = true,
                    enabled = !isLoading, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pass, onValueChange = { pass = it },
                    label = { Text("Пароль") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !isLoading, modifier = Modifier.fillMaxWidth()
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onLogin(email.trim(), pass) },
                        enabled = !isLoading, modifier = Modifier.weight(1f)
                    ) { Text("Войти") }

                    OutlinedButton(
                        onClick = { onRegister(email.trim(), pass) },
                        enabled = !isLoading, modifier = Modifier.weight(1f)
                    ) { Text("Регистрация") }
                }

                TextButton(
                    onClick = { showReset = true },
                    enabled = !isLoading
                ) { Text("Забыли пароль?") }

                if (message != null) {
                    Text(message, color = MaterialTheme.colorScheme.error)
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
        }
    }

    if (showReset) {
        ResetPasswordDialog(
            emailPrefill = email.trim(),
            onDismiss = { showReset = false },
            onSubmit = { e, newP, secret ->
                onReset(e, newP, secret)
                showReset = false
            }
        )
    }
}

@Composable
private fun ResetPasswordDialog(
    emailPrefill: String,
    onDismiss: () -> Unit,
    onSubmit: (email: String, newPass: String, secret: String) -> Unit
) {
    var email by remember { mutableStateOf(emailPrefill) }
    var newPass by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("0000") } // учебный секрет

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Восстановить пароль") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPass, onValueChange = { newPass = it },
                    label = { Text("Новый пароль") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = secret, onValueChange = { secret = it },
                    label = { Text("Секретный код") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("В учебном проекте код по умолчанию — 0000", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(email.trim(), newPass, secret.trim()) }) { Text("Сменить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}