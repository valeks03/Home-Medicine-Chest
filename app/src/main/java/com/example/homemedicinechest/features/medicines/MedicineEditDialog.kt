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

    // флаги ошибок
    var nameErr by remember { mutableStateOf(false) }
    var dosageErr by remember { mutableStateOf(false) }
    var formErr by remember { mutableStateOf(false) }
    var stockErr by remember { mutableStateOf(false) }
    var expiryErr by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Новое лекарство" else "Редактирование") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; if (nameErr && it.isNotBlank()) nameErr = false },
                    label = { Text("Название") },
                    isError = nameErr,
                    supportingText = { if (nameErr) Text("Обязательно заполните", color = MaterialTheme.colorScheme.error) },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it; if (dosageErr && it.isNotBlank()) dosageErr = false },
                    label = { Text("Дозировка") },
                    isError = dosageErr,
                    supportingText = { if (dosageErr) Text("Обязательно заполните", color = MaterialTheme.colorScheme.error) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                // Форма — выпадающий список с подсветкой ошибки
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = form,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Форма") },
                        isError = formErr,
                        supportingText = { if (formErr) Text("Выберите форму", color = MaterialTheme.colorScheme.error) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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
                                    formErr = false
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Инструкция") },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it; if (stockErr && it.isNotBlank()) stockErr = false },
                    label = { Text("Остаток") },
                    isError = stockErr,
                    supportingText = { if (stockErr) Text("Обязательно заполните", color = MaterialTheme.colorScheme.error) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Срок годности + подсветка
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {
                            expiry = null
                            expiryErr = false
                        },
                        label = { Text("Без срока") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (expiry == null) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surface,
                            labelColor = if (expiry == null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    )

                    AssistChip(
                        onClick = {
                            val now = Calendar.getInstance()
                            DatePickerDialog(ctx, { _, y, m, d ->
                                val c = Calendar.getInstance()
                                c.set(y, m, d, 12, 0, 0)
                                val picked = c.timeInMillis
                                if (picked <= System.currentTimeMillis()) {
                                    expiry = picked
                                    expiryErr = true
                                } else {
                                    expiry = picked
                                    expiryErr = false
                                }
                            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
                        },
                        label = { Text(expiry?.let { df.format(Date(it)) } ?: "Выбрать дату") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (expiry != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surface,
                            labelColor = if (expiry != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

            }
        },
        confirmButton = {
            TextButton(onClick = {
                nameErr = name.isBlank()
                dosageErr = dosage.isBlank()
                formErr = form.isBlank()
                stockErr = stock.isBlank()

                // проверка даты
                expiryErr = expiry?.let { it <= System.currentTimeMillis() } ?: false

                val ok = !(nameErr || dosageErr || formErr || stockErr || expiryErr)
                if (!ok) return@TextButton

                onSave(
                    Medicine(
                        id = initial?.id ?: 0L,
                        userId = userId,
                        name = name.trim(),
                        nameNorm = name.trim().lowercase(Locale.getDefault()),
                        dosage = dosage.trim(),
                        form = form.trim(),
                        instructions = instructions.ifBlank { null },
                        expiresAt = expiry, // null = «без срока годности»
                        stockQty = stock.toIntOrNull() ?: 0
                    )
                )
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}