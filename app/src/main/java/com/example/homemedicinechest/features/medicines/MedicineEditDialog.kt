package com.example.homemedicinechest.features.medicines

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.homemedicinechest.R
import com.example.homemedicinechest.data.db.Medicine
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MedicineEditDialog(
    initial: Medicine?,
    userId: Long,
    onDismiss: () -> Unit,
    onSave: (Medicine) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var dosage by remember { mutableStateOf(initial?.dosage ?: "") }
    var form by remember { mutableStateOf(initial?.form ?: "") }
    var instructions by remember { mutableStateOf(initial?.instructions ?: "") }
    var stock by remember { mutableStateOf((initial?.stockQty ?: 0).toString()) }
    var expiry by remember { mutableStateOf(initial?.expiresAt) }
    val ctx = LocalContext.current
    val df = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Новое лекарство" else "Редактирование") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Название") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dosage, onValueChange = { dosage = it },
                    label = { Text("Дозировка") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = form, onValueChange = { form = it },
                    label = { Text("Форма") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = instructions, onValueChange = { instructions = it },
                    label = { Text("Инструкция") }, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = stock, onValueChange = { stock = it },
                    label = { Text("Остаток") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        val now = Calendar.getInstance()
                        DatePickerDialog(ctx, { _, y, m, d ->
                            val c = Calendar.getInstance()
                            c.set(Calendar.YEAR, y)
                            c.set(Calendar.MONTH, m)
                            c.set(Calendar.DAY_OF_MONTH, d)
                            c.set(Calendar.HOUR_OF_DAY, 12)
                            c.set(Calendar.MINUTE, 0)
                            c.set(Calendar.SECOND, 0)
                            c.set(Calendar.MILLISECOND, 0)
                            expiry = c.timeInMillis
                        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
                    }) { Text("Срок годности") }
                    Text(expiry?.let { df.format(Date(it)) } ?: "—", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank() || dosage.isBlank()) return@TextButton
                onSave(
                    Medicine(
                        id = initial?.id ?: 0L,
                        userId = userId,
                        name = name.trim(),
                        dosage = dosage.trim(),
                        form = form.ifBlank { null },
                        instructions = instructions.ifBlank { null },
                        expiresAt = expiry,
                        stockQty = stock.toIntOrNull() ?: 0
                    )
                )
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}