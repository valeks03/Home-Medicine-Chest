@file:OptIn(ExperimentalLayoutApi::class)
package com.example.homemedicinechest.features.medicines


import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.example.homemedicinechest.data.db.MedicationSchedule
import com.example.homemedicinechest.data.db.Medicine
import java.util.Calendar
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.platform.LocalContext

@Composable
fun ScheduleEditDialog(
    userId: Long,
    medicine: Medicine,
    onDismiss: () -> Unit,
    onSave: (MedicationSchedule) -> Unit
) {
    var hour by remember { mutableStateOf(9) }
    var minute by remember { mutableStateOf(0) }
    var daysMask by remember { mutableStateOf(0) }  // 0 = каждый день
    var dose by remember { mutableStateOf(medicine.dosage) }
    val ctx = LocalContext.current

    val dayNames = listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс")

    fun toggleDay(idx: Int) {
        daysMask = daysMask xor (1 shl idx) // 0..6
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("График приёма: ${medicine.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        val now = Calendar.getInstance()
                        TimePickerDialog(
                            ctx,
                            { _, h, m -> hour = h; minute = m },
                            hour, minute, true
                        ).show()
                    }) { Text(String.format("%02d:%02d", hour, minute)) }

                    OutlinedTextField(
                        value = dose,
                        onValueChange = { dose = it },
                        label = { Text("Доза/комм.") },
                        singleLine = true
                    )
                }

                Text("Дни недели (пусто = каждый день)")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    dayNames.forEachIndexed { idx, label ->
                        val selected = (daysMask and (1 shl idx)) != 0
                        FilterChip(
                            selected = selected,
                            onClick = { toggleDay(idx) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    MedicationSchedule(
                        userId = userId,
                        medicineId = medicine.id,
                        hour = hour, minute = minute,
                        daysMask = daysMask,
                        dose = dose.ifBlank { null },
                        enabled = true
                    )
                )
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}