package com.example.homemedicinechest.features.medicines

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.homemedicinechest.App
import com.example.homemedicinechest.data.db.Medicine
import com.example.homemedicinechest.data.repo.MedicinesRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinesScreen(userId: Long) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as App
    val repo = remember { MedicinesRepository(app.db.medicineDao()) }
    val presenter = remember { MedicinesPresenter(repo) }
    var list by remember { mutableStateOf(emptyList<Medicine>()) }
    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Medicine?>(null) }
    val scope = rememberCoroutineScope()

    // Имитация View слоя MVP
    val view = remember {
        object : MedicinesView {
            override fun showLoading(show: Boolean) {}
            override fun showMessage(msg: String) {}
            override fun render(list_: List<Medicine>) { list = list_ }
        }
    }

    DisposableEffect(Unit) {
        presenter.attach(view)
        presenter.observe(userId)
        onDispose { presenter.detach() }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; showEditor = true }) {
                Text("+")
            }
        }
    ) { p ->
        Column(Modifier.fillMaxSize().padding(p)) {

            CenterAlignedTopAppBar(
                title = { Text("Лекарства") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
            if (list.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Добавьте первое лекарство")
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(list) { m ->
                        MedicineCard(
                            m,
                            onClick = { editing = m; showEditor = true },
                            onDelete = { scope.launch { presenter.delete(m) } }
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

@Composable
private fun MedicineCard(
    m: Medicine,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val df = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    ElevatedCard(Modifier.fillMaxWidth().clickable { onClick() }) {
        Column(Modifier.padding(12.dp)) {
            Text(m.name, style = MaterialTheme.typography.titleMedium)
            Text("${m.dosage} · Остаток: ${m.stockQty}", style = MaterialTheme.typography.bodyMedium)
            Text(
                m.expiresAt?.let { "Годен до: ${df.format(Date(it))}" } ?: "Без срока",
                style = MaterialTheme.typography.bodySmall
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDelete) { Text("Удалить") }
            }
        }
    }
}
