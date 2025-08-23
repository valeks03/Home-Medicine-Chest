package com.example.homemedicinechest.features.medicines

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.homemedicinechest.App
import com.example.homemedicinechest.R
import com.example.homemedicinechest.data.db.Medicine
import com.example.homemedicinechest.data.repo.MedicinesRepository
import com.example.homemedicinechest.ui.theme.* // Purple40, Lavender, etc.
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinesScreen(userId: Long,
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
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Меню"
                            )
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
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    Text("+", fontSize = 20.sp)
                }
            }
        }
    ) { innerPadding ->
        val extraFabSpace = 104.dp

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


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + extraFabSpace
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (searchQuery.isEmpty()) "Добавьте первое лекарство" else "Ничего не найдено")
                    }
                }
            } else {
                items(filtered) { m ->
                    MedicineCard(
                        m,
                        onClick = { editing = m; showEditor = true },
                        onDelete = { scope.launch { presenter.delete(m) } }
                    )
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, Purple40.copy(alpha = 0.30f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    m.name,
                    style = MaterialTheme.typography.titleMedium
                )

                m.form?.takeIf { it.isNotBlank() }?.let { formText ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
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