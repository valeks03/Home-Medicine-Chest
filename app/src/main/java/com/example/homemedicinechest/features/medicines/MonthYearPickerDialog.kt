package com.example.homemedicinechest.features.medicines

import android.widget.NumberPicker
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

@Composable
fun MonthYearPickerDialog(
    initialMillis: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (selectedMillis: Long) -> Unit
) {
    val months = remember {
        DateFormatSymbols.getInstance(Locale.getDefault()).months.take(12).toTypedArray()
    }
    val cal = remember { Calendar.getInstance() }
    if (initialMillis != null) cal.timeInMillis = initialMillis

    var month by remember { mutableStateOf(cal.get(Calendar.MONTH)) } // 0..11
    var year by remember { mutableStateOf(cal.get(Calendar.YEAR)) }

    val minYear = Calendar.getInstance().get(Calendar.YEAR)   // не даём прошлые годы
    val maxYear = minYear + 20

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Срок годности (месяц, год)") },
        text = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Месяц
                AndroidView(
                    factory = { ctx ->
                        NumberPicker(ctx).apply {
                            minValue = 0
                            maxValue = 11
                            displayedValues = months
                            value = month
                            setOnValueChangedListener { _, _, newVal -> month = newVal }
                        }
                    },
                    update = { it.value = month },
                    modifier = Modifier.weight(1f)
                )

                // Год
                AndroidView(
                    factory = { ctx ->
                        NumberPicker(ctx).apply {
                            minValue = minYear
                            maxValue = maxYear
                            value = year
                            setOnValueChangedListener { _, _, newVal -> year = newVal }
                        }
                    },
                    update = { it.value = year },
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(endOfMonthMillis(year, month))
            }) { Text("Готово") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
