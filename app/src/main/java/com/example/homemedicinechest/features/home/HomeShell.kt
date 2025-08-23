package com.example.homemedicinechest.features.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.homemedicinechest.features.medicines.MedicinesScreen
import com.example.homemedicinechest.features.profile.ProfileScreen

sealed class DrawerItem(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Medicines : DrawerItem("drawer/medicines", "Медикаменты", Icons.Filled.Medication)
    data object Stats     : DrawerItem("drawer/stats",     "Статистика",  Icons.Filled.BarChart)
    data object Profile   : DrawerItem("drawer/profile",   "Профиль",     Icons.Filled.Person)
    data object Logout    : DrawerItem("drawer/logout",    "Выход",       Icons.AutoMirrored.Filled.ExitToApp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeShell(
    userId: Long,
    onLogout: () -> Unit
) {
    val nav = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentTitle by remember { mutableStateOf(DrawerItem.Medicines.title) }

    var searchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }


    val items = listOf(
        DrawerItem.Profile,
        DrawerItem.Medicines,
        DrawerItem.Stats,
        DrawerItem.Logout
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Home Medicine Chest",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.title) },
                        selected = false, // можно подсвечивать по currentRoute, если нужно
                        onClick = {
                            scope.launch { drawerState.close() }

                            if (item is DrawerItem.Logout) {
                                onLogout()
                            } else {
                                currentTitle = item.title

                                if (item !is DrawerItem.Medicines) {
                                    searchMode = false
                                    searchQuery = ""
                                }

                                nav.navigate(item.route) {
                                    popUpTo(DrawerItem.Medicines.route)
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        if (searchMode) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                placeholder = { Text("Поиск лекарств", color = Color.White) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(color = Color.White),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = Color.White
                                )
                            )
                        } else {
                            Text(currentTitle)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu, // можно заменить на меню-иконку
                                contentDescription = "Меню",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        if (searchMode) {
                            IconButton(onClick = { searchQuery = ""; searchMode = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Закрыть поиск", tint = Color.White)
                            }
                        } else {
                            // показываем лупу только на экране медикаментов
                            if (currentTitle == DrawerItem.Medicines.title) {
                                IconButton(onClick = { searchMode = true }) {
                                    Icon(Icons.Filled.Search, contentDescription = "Поиск", tint = Color.White)
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF6650A4),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            HomeNavHost(
                nav = nav,
                userId = userId,
                modifier = Modifier.padding(innerPadding),
                onLogout = onLogout,
                searchQuery = searchQuery
            )
        }
    }
}

@Composable
private fun HomeNavHost(
    nav: NavHostController,
    userId: Long,
    modifier: Modifier,
    onLogout: () -> Unit,
    searchQuery: String
) {
    NavHost(
        navController = nav,
        startDestination = DrawerItem.Medicines.route,
        modifier = modifier
    ) {

        composable(DrawerItem.Profile.route) {
            ProfileScreen(userId = userId)
        }
        composable(DrawerItem.Medicines.route) {
            MedicinesScreen(
                userId = userId,
                onLogout = onLogout,
                showOwnTopBar = false,
                showOwnFab = true,
                searchQuery = searchQuery
            )
        }
        composable(DrawerItem.Stats.route) {
            StatsPlaceholder()
        }
    }
}

@Composable
private fun StatsPlaceholder() {
    Surface(tonalElevation = 1.dp, modifier = Modifier.padding(16.dp)) {
        Text("Статистика — скоро будет график/история", modifier = Modifier.padding(16.dp))
    }
}

//@Composable
//private fun ProfilePlaceholder() {
//    Surface(tonalElevation = 1.dp, modifier = Modifier.padding(16.dp)) {
//        Text("Профиль — редактирование данных пользователя", modifier = Modifier.padding(16.dp))
//    }
//}
