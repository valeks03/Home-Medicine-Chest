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
                            val picked = c.timeInMillis
                            // null разрешено; если выбрали дату — она должна быть в будущем
                            if (picked <= System.currentTimeMillis()) {
                                expiry = picked
                                expiryErr = true
                            } else {
                                expiry = picked
                                expiryErr = false
                            }
                        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
                    }) { Text("Срок годности") }
                    Spacer(Modifier.width(8.dp))

                    Text(
                        expiry?.let { df.format(Date(it)) } ?: "—",
                        color = if (expiryErr) MaterialTheme.colorScheme.error else LocalContentColor.current,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )

                    // новая кнопка "Очистить" видна только если дата выбрана
                    if (expiry != null) {
                        Spacer(Modifier.width(8.dp))
                        TextButton(
                            onClick = { expiry = null; expiryErr = false }
                        ) { Text("Очистить") }
                    }
                }
                if (expiryErr) {
                    Text(
                        "Дата должна быть позже сегодняшнего дня",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
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