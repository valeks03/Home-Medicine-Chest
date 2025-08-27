@file:OptIn(ExperimentalAnimationApi::class)

package com.example.homemedicinechest.features.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import kotlinx.coroutines.launch
import com.example.homemedicinechest.features.medicines.MedicinesScreen
import com.example.homemedicinechest.features.profile.ProfileScreen
import com.example.homemedicinechest.features.stats.StatsScreen

sealed class DrawerItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
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
    val nav = rememberAnimatedNavController()
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
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }

                            if (item is DrawerItem.Logout) {
                                onLogout()
                            } else {
                                currentTitle = item.title
                                // поиск только на экране медикаментов
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
                                imageVector = Icons.Default.Menu,
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
                        } else if (currentTitle == DrawerItem.Medicines.title) {
                            IconButton(onClick = { searchMode = true }) {
                                Icon(Icons.Filled.Search, contentDescription = "Поиск", tint = Color.White)
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
    AnimatedNavHost(
        navController = nav,
        startDestination = DrawerItem.Medicines.route,
        modifier = modifier,
        enterTransition = { slideInVertically(initialOffsetY = { full -> full }, animationSpec = tween(250)) + fadeIn() },
        exitTransition  = { slideOutVertically(targetOffsetY = { full -> -full/4 }, animationSpec = tween(200)) + fadeOut() },
        popEnterTransition = { slideInVertically(initialOffsetY = { full -> -full/4 }, animationSpec = tween(250)) + fadeIn() },
        popExitTransition  = { slideOutVertically(targetOffsetY = { full -> full }, animationSpec = tween(200)) + fadeOut() }
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
            StatsScreen(userId)
        }
    }
}
