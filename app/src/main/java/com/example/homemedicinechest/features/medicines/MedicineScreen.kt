package com.example.homemedicinechest.features.medicines

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.homemedicinechest.App
import com.example.homemedicinechest.R
import com.example.homemedicinechest.data.db.Medicine
import com.example.homemedicinechest.data.repo.MedicinesRepository
import com.example.homemedicinechest.data.repo.ScheduleRepository
import com.example.homemedicinechest.features.reminders.ReminderScheduler
import com.example.homemedicinechest.ui.theme.Purple40
import kotlinx.coroutines.launch
import androidx.compose.foundation.combinedClickable
import java.text.SimpleDateFormat
import java.util.*

private enum class SortMode { NAME, EXPIRY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinesScreen(
    userId: Long,
    onLogout: () -> Unit = {},
    showOwnTopBar: Boolean = false,
    showOwnFab: Boolean = true,
    topBarActions: @Composable RowScope.() -> Unit = {},
    searchQuery: String = ""
) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as App
    val repo = remember { MedicinesRepository(app.db.medicineDao()) }
    val presenter = remember { MedicinesPresenter(repo) }
    var list by remember { mutableStateOf(emptyList<Medicine>()) }
    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Medicine?>(null) }
    val scope = rememberCoroutineScope()
    var menuExpanded by remember { mutableStateOf(false) }
    var scheduleFor by remember { mutableStateOf<Medicine?>(null) }
    val scheduleRepo = remember { ScheduleRepository(app.db.scheduleDao()) }
    var sortMode by remember { mutableStateOf(SortMode.NAME) }

    val view = remember {
        object : MedicinesView {
            override fun showLoading(show: Boolean) {}
            override fun showMessage(msg: String) {}
            override fun render(list_: List<Medicine>) { list = list_ }
        }
    }

    scheduleFor?.let { med ->
        ScheduleEditDialog(
            userId = userId,
            medicine = med,
            onDismiss = { scheduleFor = null },
            onSave = { s ->
                scope.launch {
                    val id = scheduleRepo.addSchedule(s)
                    ReminderScheduler.scheduleNext(app, s.copy(id = id))
                    scheduleFor = null
                }
            }
        )
    }

    DisposableEffect(Unit) {
        presenter.attach(view)
        presenter.observe(userId)
        onDispose { presenter.detach() }
    }

    Scaffold(
        topBar = {
            if (showOwnTopBar) {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.home_medicine_chest)) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF6650A4),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Меню")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Выйти") },
                                onClick = { menuExpanded = false; onLogout() }
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (showOwnFab) {
                FloatingActionButton(
                    onClick = { editing = null; showEditor = true },
                    modifier = Modifier.padding(8.dp)
                ) { Text("+", fontSize = 20.sp) }
            }
        }
    ) { innerPadding ->
        val extraFabSpace = 104.dp

        // Фильтр по поиску
        val filtered = remember(list, searchQuery) {
            val q = searchQuery.trim().lowercase()
            if (q.isEmpty()) list
            else list.filter { m ->
                val name = m.name.lowercase()
                val dosage = m.dosage.lowercase()
                val form = m.form?.lowercase() ?: ""
                name.contains(q) || dosage.contains(q) || form.contains(q)
            }
        }

        // Разбиваем на просроченные/актуальные и сортируем; просроченные всегда сверху
        val finalList = remember(filtered, sortMode) {
            val now = System.currentTimeMillis()
            val (expired, active) = filtered.partition { it.expiresAt?.let { ts -> ts < now } == true }

            val expiredSorted = when (sortMode) {
                SortMode.NAME ->
                    expired.sortedBy { it.nameNorm ?: it.name.lowercase(Locale.getDefault()) }
                SortMode.EXPIRY ->
                    expired.sortedWith(
                        compareBy<Medicine> { it.expiresAt } // более ранние просрочки раньше
                            .thenBy { it.nameNorm ?: it.name.lowercase(Locale.getDefault()) }
                    )
            }
            val activeSorted = when (sortMode) {
                SortMode.NAME ->
                    active.sortedBy { it.nameNorm ?: it.name.lowercase(Locale.getDefault()) }
                SortMode.EXPIRY ->
                    active.sortedWith(
                        compareBy<Medicine> { it.expiresAt == null } // null (без срока) — в конец
                            .thenBy { it.expiresAt ?: Long.MAX_VALUE }
                            .thenBy { it.nameNorm ?: it.name.lowercase(Locale.getDefault()) }
                    )
            }
            expiredSorted + activeSorted
        }

        // Обёртка: чипы сортировки + список
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            // Переключатель сортировки
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = innerPadding.calculateTopPadding(), bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { sortMode = SortMode.NAME },
                    label = { Text("По названию") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (sortMode == SortMode.NAME)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surface
                    )
                )
                AssistChip(
                    onClick = { sortMode = SortMode.EXPIRY },
                    label = { Text("По сроку") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (sortMode == SortMode.EXPIRY)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surface
                    )
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + extraFabSpace
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (finalList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (searchQuery.isEmpty()) "Добавьте первое лекарство" else "Ничего не найдено")
                        }
                    }
                } else {
                    items(finalList) { m ->
                        MedicineCard(
                            m = m,
                            onClick = { editing = m; showEditor = true },
                            onDelete = { scope.launch { presenter.delete(m) } },
                            onForceExpire = { forced -> scope.launch { presenter.addOrUpdate(forced) } },
                            onSchedule = { scheduleFor = m }
                        )
                    }
                }
            }
        }
    }

    if (showEditor) {
        MedicineEditDialog(
            initial = editing,
            userId = userId,
            onDismiss = { showEditor = false },
            onSave = { m ->
                scope.launch {
                    presenter.addOrUpdate(m)
                    showEditor = false
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MedicineCard(
    m: Medicine,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onForceExpire: (Medicine) -> Unit = {},
    onSchedule: () -> Unit = {}
) {
    val df = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val expired = m.expiresAt?.let { it < System.currentTimeMillis() } == true

    val containerColor =
        if (expired) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
    val contentColor =
        if (expired) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
    val borderColor =
        if (expired) MaterialTheme.colorScheme.error else Purple40.copy(alpha = 0.30f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000
                    onForceExpire(m.copy(expiresAt = yesterday))
                }
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(m.name, style = MaterialTheme.typography.titleMedium)

                m.form?.takeIf { it.isNotBlank() }?.let { formText ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = if (expired) 0.25f else 0.15f),
                        tonalElevation = 0.dp
                    ) {
                        Text(
                            text = formText,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Text("${m.dosage} · Остаток: ${m.stockQty}", style = MaterialTheme.typography.bodyMedium)

            val expiryText = when {
                m.expiresAt == null -> "Без срока"
                expired -> "Просрочено: ${df.format(Date(m.expiresAt!!))}"
                else -> "Годен до: ${df.format(Date(m.expiresAt!!))}"
            }
            Text(expiryText, style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(4.dp))
            // Блок расписаний: тумблеры отключены для просроченных
            MedicineSchedulesBlock(m.id, expired)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    onClick = { onSchedule() },
                    enabled = !expired // расписание нельзя для просроченных
                ) { Text("Расписание") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDelete) { Text("Удалить") }
            }
        }
    }
}

@Composable
private fun MedicineSchedulesBlock(medicineId: Long, expired: Boolean) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as App
    val dao = remember { app.db.scheduleDao() }
    val scope = rememberCoroutineScope()
    val schedules by dao.observeForMedicine(medicineId).collectAsState(initial = emptyList())

    if (schedules.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Расписание", style = MaterialTheme.typography.labelLarge)
        schedules.forEach { s ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildString {
                        append(String.format("%02d:%02d", s.hour, s.minute))
                        if (s.daysMask != 0) append(" · ").append(daysMaskLabel(s.daysMask))
                        s.dose?.let { append(" · ").append(it) }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = s.enabled,
                    enabled = !expired, // нельзя менять расписание у просроченных
                    onCheckedChange = { checked ->
                        if (!expired) {
                            scope.launch {
                                val upd = s.copy(enabled = checked)
                                dao.update(upd)
                                if (checked) ReminderScheduler.scheduleNext(ctx, upd)
                                else ReminderScheduler.cancel(ctx, s)
                            }
                        }
                    }
                )
                IconButton(onClick = {
                    // Удалять расписание можно всегда
                    scope.launch {
                        ReminderScheduler.cancel(ctx, s)
                        dao.delete(s)
                    }
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить расписание")
                }
            }
        }
    }
}

private fun daysMaskLabel(mask: Int): String {
    val names = listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс")
    return names
        .mapIndexedNotNull { i, n -> if (mask and (1 shl i) != 0) n else null }
        .joinToString(" ")
}
