@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.homemedicinechest.features.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.homemedicinechest.App
import com.example.homemedicinechest.data.db.DailyRow
import com.example.homemedicinechest.data.db.IntakeLog
import com.example.homemedicinechest.data.repo.StatsBundle
import com.example.homemedicinechest.data.repo.StatsRepository
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsScreen(userId: Long) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as App
    val repo = remember { StatsRepository(app.db.intakeLogDao(), app.db.medicineDao()) }
    val presenter = remember { StatsPresenter(repo) }
    var visibleCount by remember { mutableStateOf(20) }

    // Фильтры
    var selectedMedicineId by remember { mutableStateOf<Long?>(null) }
    var daysBack by remember { mutableStateOf(30) } // период: 7/30/90
    val now = remember { System.currentTimeMillis() }
    val from = remember(daysBack) { now - daysBack * 24L * 60 * 60 * 1000 }
    val to = now

    var state by remember { mutableStateOf<StatsBundle?>(null) }
    var loading by remember { mutableStateOf(false) }

    val view = remember {
        object : StatsView {
            override fun showLoading(show: Boolean) { loading = show }
            override fun render(bundle: StatsBundle) { state = bundle }
        }
    }

    LaunchedEffect(state?.logs, selectedMedicineId, daysBack) {
        val size = state?.logs?.size ?: 0
        visibleCount = minOf(10, size)
    }


    DisposableEffect(userId, selectedMedicineId, from, to) {
        presenter.attach(view)
        presenter.observe(userId, selectedMedicineId, from, to)
        onDispose { presenter.detach() }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            FiltersBar(
                state = state,
                selectedMedicineId = selectedMedicineId,
                onMedicineSelected = { selectedMedicineId = it },
                daysBack = daysBack,
                onDaysChange = { daysBack = it }
            )
        }


        state?.let { s ->
            item { TotalsRow(s) }
            item { AdherenceLineChart(daily = s.daily) }
            item { DailyBarChart(daily = s.daily, showTitle = true) }
            item { Divider() }
            item { Text("История", style = MaterialTheme.typography.titleMedium) }

            val logsToShow = s.logs.take(visibleCount)

            items(
                items = logsToShow,
                key = { it.id } // если у IntakeLog нет id, удали key или подставь безопасный
            ) { log ->
                LogRow(log)
            }

            // Кнопки управления
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (visibleCount < s.logs.size) {
                        TextButton(onClick = { visibleCount = minOf(visibleCount + 10, s.logs.size) }) {
                            Text("Показать ещё")
                        }
                    } else if (s.logs.size > 10) {
                        TextButton(onClick = { visibleCount = 10 }) {
                            Text("Свернуть")
                        }
                    }
                }
            }
        } ?: run {
            item {
                if (loading) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

    }
}

@Composable
private fun FiltersBar(
    state: StatsBundle?,
    selectedMedicineId: Long?,
    onMedicineSelected: (Long?) -> Unit,
    daysBack: Int,
    onDaysChange: (Int) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Препарат
        var expanded by remember { mutableStateOf(false) }
        val label = state?.let { s ->
            if (selectedMedicineId == null) "Все препараты"
            else s.medicines.find { it.id == selectedMedicineId }?.name ?: "Все препараты"
        } ?: "Все препараты"

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Препарат") },
                modifier = Modifier.menuAnchor().weight(1f)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("Все препараты") }, onClick = { onMedicineSelected(null); expanded = false })
                state?.medicines?.forEach { m ->
                    DropdownMenuItem(text = { Text(m.name) }, onClick = { onMedicineSelected(m.id); expanded = false })
                }
            }
        }

        // Период
        var expandedPeriod by remember { mutableStateOf(false) }
        val periodLabel = when (daysBack) { 7 -> "7 дней"; 30 -> "30 дней"; 90 -> "90 дней"; else -> "$daysBack дн." }

        ExposedDropdownMenuBox(expanded = expandedPeriod, onExpandedChange = { expandedPeriod = !expandedPeriod }) {
            OutlinedTextField(
                value = periodLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Период") },
                modifier = Modifier.menuAnchor().width(130.dp)
            )
            ExposedDropdownMenu(expanded = expandedPeriod, onDismissRequest = { expandedPeriod = false }) {
                listOf(7, 30, 90).forEach { d ->
                    DropdownMenuItem(text = { Text("$d дней") }, onClick = { onDaysChange(d); expandedPeriod = false })
                }
            }
        }
    }
}

@Composable
private fun TotalsRow(state: StatsBundle) {
    val adherencePct = (state.totals.adherence * 100f)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard("Принято", state.totals.taken.toString(), Modifier.weight(1f))
        StatCard("Пропущено", state.totals.skipped.toString(), Modifier.weight(1f))
        StatCard("Соблюдение", String.format(Locale.getDefault(), "%.0f%%", adherencePct), Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun DailyBlock(daily: List<DailyRow>) {
    if (daily.isEmpty()) {
        Text("Нет данных за выбранный период")
        return
    }
    val df = remember { SimpleDateFormat("dd.MM", Locale.getDefault()) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("По дням", style = MaterialTheme.typography.titleMedium)
        daily.forEach { row ->
            val millis = row.day * 86_400_000L
            val label = df.format(Date(millis))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label)
                Text("Принято: ${row.taken} · Пропущено: ${row.skipped}")
            }
        }
    }
}

@Composable
private fun LogRow(log: IntakeLog) {
    val df = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val title = if (log.status == "TAKEN") "Принял" else "Пропустил"
    val color = if (log.status == "TAKEN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = color)
        Text(df.format(Date(log.plannedAt)))
    }
}
