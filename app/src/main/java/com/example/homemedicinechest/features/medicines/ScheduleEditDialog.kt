@file:OptIn(ExperimentalLayoutApi::class)
package com.example.homemedicinechest.features.medicines

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.homemedicinechest.data.db.MedicationSchedule
import com.example.homemedicinechest.data.db.Medicine
import com.example.homemedicinechest.ui.theme.Purple40
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
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
        daysMask = daysMask xor (1 shl idx)
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, Purple40.copy(alpha = 0.3f)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.padding(16.dp)) {
                // Title
                Text("График приёма: ${medicine.name}", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))

                // Content
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {
                        val now = Calendar.getInstance()
                        TimePickerDialog(
                            ctx,
                            { _, h, m -> hour = h; minute = m },
                            hour, minute, true
                        ).show()
                    }, modifier = Modifier.height(56.dp)) {
                        Text(String.format("%02d:%02d", hour, minute))
                    }

                    OutlinedTextField(
                        value = dose,
                        onValueChange = { dose = it },
                        label = { Text("Доза/комм.") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text("Дни недели (пусто = каждый день)")
                Spacer(Modifier.height(8.dp))

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

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Отмена") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        onSave(
                            MedicationSchedule(
                                userId = userId,
                                medicineId = medicine.id,
                                hour = hour,
                                minute = minute,
                                daysMask = daysMask,
                                dose = dose.ifBlank { null },
                                enabled = true
                            )
                        )
                    }) { Text("Сохранить") }
                }
            }
        }
    }
}