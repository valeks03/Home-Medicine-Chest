package com.example.homemedicinechest.features.profile

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
fun BirthdayPickerDialog(
    initialMillis: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (selectedMillis: Long) -> Unit
) {
    val months = remember {
        DateFormatSymbols.getInstance(Locale.getDefault()).months.take(12).toTypedArray()
    }
    val cal = remember { Calendar.getInstance() }
    if (initialMillis != null) cal.timeInMillis = initialMillis

    var year by remember { mutableStateOf(cal.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(cal.get(Calendar.MONTH)) } // 0..11
    var day by remember { mutableStateOf(cal.get(Calendar.DAY_OF_MONTH)) }

    val now = remember { Calendar.getInstance() }
    val minYear = now.get(Calendar.YEAR) - 120
    val maxYear = now.get(Calendar.YEAR)

    fun maxDayOfMonth(y: Int, m0: Int): Int {
        val c = Calendar.getInstance().apply {
            set(Calendar.YEAR, y); set(Calendar.MONTH, m0); set(Calendar.DAY_OF_MONTH, 1)
        }
        return c.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    LaunchedEffect(year, month) {
        val max = maxDayOfMonth(year, month)
        if (day > max) day = max
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Дата рождения") },
        text = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AndroidView(
                    factory = { ctx ->
                        NumberPicker(ctx).apply {
                            minValue = 1
                            maxValue = maxDayOfMonth(year, month)
                            value = day
                            setOnValueChangedListener { _, _, newVal -> day = newVal }
                        }
                    },
                    update = {
                        it.minValue = 1
                        it.maxValue = maxDayOfMonth(year, month)
                        it.value = day
                    },
                    modifier = Modifier.weight(1f)
                )
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
                    modifier = Modifier.weight(1.2f)
                )
                AndroidView(
                    factory = { ctx ->
                        NumberPicker(ctx).apply {
                            minValue = minYear
                            maxValue = maxYear
                            value = year
                            setOnValueChangedListener { _, _, newVal -> year = newVal }
                        }
                    },
                    update = {
                        it.minValue = minYear
                        it.maxValue = maxYear
                        it.value = year
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val c = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                // запретим будущую дату рождения
                if (c.timeInMillis > now.timeInMillis) {
                    // Можно показать ошибку с помощью Snackbar/Toast — на твоё усмотрение.
                } else {
                    onConfirm(c.timeInMillis)
                }
            }) { Text("Готово") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
