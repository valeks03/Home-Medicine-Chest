package com.example.homemedicinechest.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.homemedicinechest.R

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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(stringResource(R.string.home_medicine_chest), style = MaterialTheme.typography.headlineSmall)

                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text(stringResource(R.string.email)) },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pass, onValueChange = { pass = it },
                    label = { Text(stringResource(R.string.password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    enabled = !isLoading, modifier = Modifier.fillMaxWidth()
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onLogin(email.trim(), pass) },
                        enabled = !isLoading, modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.sign_in)) }

                    OutlinedButton(
                        onClick = { onRegister(email.trim(), pass) },
                        enabled = !isLoading, modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.registration)) }
                }

                TextButton(
                    onClick = { showReset = true },
                    enabled = !isLoading
                ) { Text(stringResource(R.string.forget_password)) }

                if (message != null) {
                    Text(message, color = MaterialTheme.colorScheme.error)
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f)),
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
    var secret by remember { mutableStateOf("0000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reset_password)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text(stringResource(R.string.email)) }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPass, onValueChange = { newPass = it },
                    label = { Text(stringResource(R.string.new_password)) }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = secret, onValueChange = { secret = it },
                    label = { Text(stringResource(R.string.secret_code)) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(stringResource(R.string.code_is_0000), style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(email.trim(), newPass, secret.trim()) }) { Text(
                stringResource(R.string.change)
            ) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}