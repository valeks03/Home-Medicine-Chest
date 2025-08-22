@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.homemedicinechest.features.medicines

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.homemedicinechest.R
import com.example.homemedicinechest.data.db.Medicine
import com.example.homemedicinechest.ui.theme.*
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
    val forms = listOf("Таблетки", "Мазь", "Сироп", "Спрей", "Капли")
    var expanded by remember { mutableStateOf(false) }
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
                    label = { Text("Название") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dosage, onValueChange = { dosage = it },
                    label = { Text("Дозировка") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = form,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Форма") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        forms.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    form = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = instructions, onValueChange = { instructions = it },
                    label = { Text("Инструкция") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    )
                )
                OutlinedTextField(
                    value = stock, onValueChange = { stock = it },
                    label = { Text("Остаток") }, singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
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

                    Text(
                        expiry?.let { df.format(Date(it)) } ?: "—",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f).padding(start = 12.dp)
                    )
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